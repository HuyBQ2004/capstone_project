import os
import re
import sys
import copy
import shutil
import openpyxl
from openpyxl.styles import Font, PatternFill, Border, Alignment
from openpyxl.worksheet.datavalidation import DataValidation

sys.stdout.reconfigure(encoding='utf-8')

# Paths
base_dir = os.path.dirname(os.path.abspath(__file__))
excel_path = r"Report5_Web E2E Test.xlsx"
backup_path = r"Report5_Web E2E Test_Backup.xlsx"

print("Starting populate_report_web.py with explicit green colors and pipe parser fix...")
print(f"Base Directory: {base_dir}")
print(f"Excel Path: {excel_path}")

# 1. Restore/Backup Excel file
if os.path.exists(backup_path):
    print(f"Restoring Excel file from backup: {backup_path} -> {excel_path}")
    shutil.copyfile(backup_path, excel_path)
else:
    if os.path.exists(excel_path):
        print(f"Creating backup of Excel file: {excel_path} -> {backup_path}")
        shutil.copyfile(excel_path, backup_path)
    else:
        print(f"Error: Excel file not found at {excel_path}")
        sys.exit(1)

def clean_markdown_symbols(text):
    if not text:
        return ""
    text_str = str(text)
    text_str = re.sub(r'\*\*\*([^*]+)\*\*\*', r'\1', text_str)
    text_str = re.sub(r'\*\*([^*]+)\*\*', r'\1', text_str)
    text_str = re.sub(r'\*([^*]+)\*', r'\1', text_str)
    text_str = re.sub(r'`([^`]+)`', r'\1', text_str)
    return text_str.strip()

# 2. Parse Markdown Test Cases
def parse_md_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    rows = []
    in_test_cases = False
    general_info = {}
    
    for line in lines:
        line_str = line.strip()
        
        # Section detection
        if line_str.startswith('### 3. Test Cases'):
            in_test_cases = True
            continue
        elif line_str.startswith('## ') or line_str.startswith('### '):
            if in_test_cases:
                if 'filling guide' in line_str.lower() or 'general information' in line_str.lower() or 'summary' in line_str.lower():
                    in_test_cases = False
        
        if not in_test_cases:
            if line_str.startswith('|'):
                temp_line = line_str.replace('\\|', '__PIPE__')
                parts = [p.strip() for p in temp_line.split('|')]
                if len(parts) >= 4:
                    field = parts[1].strip().replace('__PIPE__', '|')
                    val = parts[2].strip().replace('__PIPE__', '|')
                    if field in ['Feature', 'Test requirement']:
                        general_info[field] = clean_markdown_symbols(val)
            continue
            
        if not line_str.startswith('|'):
            continue
        
        # 1. Replace pipes inside backticks with placeholder
        temp_line = re.sub(r'`[^`]*`', lambda m: m.group(0).replace('|', '__PIPE__'), line_str)
        # 2. Replace escaped pipes
        temp_line = temp_line.replace('\\|', '__PIPE__')
        
        parts = [p.strip() for p in temp_line.split('|')]
        if parts and parts[0] == '':
            parts = parts[1:]
        if parts and parts[-1] == '':
            parts = parts[:-1]
            
        if not parts:
            continue
            
        # Check if separator row
        if all(re.match(r'^:?-+:?$', p) for p in parts):
            continue
            
        # Check if header row
        if 'test case id' in parts[0].lower():
            continue
            
        # Check if function header row
        non_empty = [p for p in parts[1:] if p != '']
        if not non_empty:
            func_name = parts[0].replace('**', '').strip().replace('__PIPE__', '|')
            rows.append({
                'type': 'function',
                'name': func_name
            })
        else:
            def clean_cell(c):
                c = c.replace('__PIPE__', '|')
                c = re.sub(r'<br\s*/?>', '\n', c, flags=re.IGNORECASE)
                c = c.replace('\\`', '`').replace('\\*', '*')
                c = re.sub(r'\*\*\*([^*]+)\*\*\*', r'\1', c)
                c = re.sub(r'\*\*([^*]+)\*\*', r'\1', c)
                c = re.sub(r'\*([^*]+)\*', r'\1', c)
                c = re.sub(r'`([^`]+)`', r'\1', c)
                return c.strip()
                
            tc_id = parts[0].replace('`', '').strip().replace('__PIPE__', '|')
            tc_desc = clean_cell(parts[1]) if len(parts) > 1 else ''
            tc_proc = clean_cell(parts[2]) if len(parts) > 2 else ''
            tc_expected = clean_cell(parts[3]) if len(parts) > 3 else ''
            tc_pre = clean_cell(parts[4]) if len(parts) > 4 else ''
            
            intermediate = [''] * 9
            for idx in range(5, len(parts) - 1):
                if idx - 5 < len(intermediate):
                    intermediate[idx - 5] = clean_cell(parts[idx])
                    
            tc_note = clean_cell(parts[-1]) if len(parts) > 5 else ''
            
            rows.append({
                'type': 'tc',
                'id': tc_id,
                'desc': tc_desc,
                'proc': tc_proc,
                'expected': tc_expected,
                'pre': tc_pre,
                'round1': intermediate[0], 'date1': intermediate[1], 'tester1': intermediate[2],
                'round2': intermediate[3], 'date2': intermediate[4], 'tester2': intermediate[5],
                'round3': intermediate[6], 'date3': intermediate[7], 'tester3': intermediate[8],
                'note': tc_note
            })
            
    return general_info, rows

# Parse all subdirectories starting with MF and their files
folders = sorted([f for f in os.listdir(base_dir) if os.path.isdir(os.path.join(base_dir, f)) and f.startswith('MF')])
all_sheets_data = []
seen_sheet_names = set()

for f in folders:
    code = f.split('_')[0]  # e.g., MF01
    f_path = os.path.join(base_dir, f)
    md_files = sorted([m for m in os.listdir(f_path) if m.endswith('.md')])
    
    for md in md_files:
        filename_no_ext = md[:-3]
        sheet_name = f"{code}_{filename_no_ext}"
        sheet_name = re.sub(r'[^a-zA-Z0-9_]', '_', sheet_name)
        sheet_name = sheet_name[:31].rstrip('_')
        
        info, rows = parse_md_file(os.path.join(f_path, md))
        feature_name = info.get('Feature', filename_no_ext.replace('_', ' '))
        reqs = info.get('Test requirement', '')
        
        all_sheets_data.append({
            'code': code,
            'md_name': filename_no_ext,
            'sheet_name': sheet_name,
            'feature_name': feature_name,
            'requirements': reqs,
            'rows': rows
        })
        print(f"Parsed file {md} -> Sheet {sheet_name} ({len(rows)} TCs/Funcs)")

print(f"Total sheets to create: {len(all_sheets_data)}")

# 3. Load Excel Workbook
wb = openpyxl.load_workbook(excel_path)

# Extract styles from Feature 1 before modifying it
temp_sheet = wb['Feature 1']
func_styles = []
for c in range(1, 16):
    cell = temp_sheet.cell(11, c)
    func_styles.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

tc_styles = []
for c in range(1, 16):
    cell = temp_sheet.cell(12, c)
    tc_styles.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

# Extract summary sheet templates
tc_list_sheet = wb['Test Cases']
tc_list_style = []
for c in range(2, 7):
    cell = tc_list_sheet.cell(9, c)
    tc_list_style.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

stats_sheet = wb['Test Statistics']
stats_data_style = []
for c in range(2, 9):
    cell = stats_sheet.cell(11, c)
    stats_data_style.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

stats_subtotal_style = []
for c in range(2, 9):
    cell = stats_sheet.cell(14, c)
    stats_subtotal_style.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

stats_coverage_style = []
for c in range(2, 7):
    cell = stats_sheet.cell(16, c)
    stats_coverage_style.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

stats_success_style = []
for c in range(2, 7):
    cell = stats_sheet.cell(17, c)
    stats_success_style.append({
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    })

# Define explicit green and white overrides
header_fill = PatternFill(fill_type='solid', fgColor='137A37')
header_font = Font(name='Tahoma', size=10, bold=True, color='FFFFFF')

func_fill = PatternFill(fill_type='solid', fgColor='E2EFDA')
func_font = Font(name='Tahoma', size=10, bold=True, color='000000')

tc_fill = PatternFill(fill_type=None)
tc_font = Font(name='Tahoma', size=10, bold=False, color='000000')

# Define default styles for clearing
default_font = Font()
default_fill = PatternFill(fill_type=None)
default_border = Border()
default_alignment = Alignment()

def apply_saved_style(cell, style_dict, fill_override, font_override):
    if style_dict['border']:
         cell.border = copy.copy(style_dict['border'])
    if style_dict['alignment']:
         cell.alignment = copy.copy(style_dict['alignment'])
    cell.number_format = style_dict['number_format']
    if style_dict['protection']:
         cell.protection = copy.copy(style_dict['protection'])
    
    # Overwrite colors explicitly
    cell.fill = fill_override
    cell.font = font_override

# 4. Create and Rename Sheets
print("Creating and renaming sheets...")
first_sheet_name = all_sheets_data[0]['sheet_name']
second_sheet_name = all_sheets_data[1]['sheet_name']

wb['Feature 1'].title = first_sheet_name
wb['Feature 2'].title = second_sheet_name

for sdata in all_sheets_data[2:]:
    name = sdata['sheet_name']
    print(f"Copying {first_sheet_name} to create {name}...")
    new_sheet = wb.copy_worksheet(wb[first_sheet_name])
    new_sheet.title = name

# Reorder sheets
sheets_order = ['Cover', 'Test Cases', 'Test Statistics'] + [s['sheet_name'] for s in all_sheets_data]
wb._sheets = [wb[s] for s in sheets_order]

all_functions_list = [] # Store tuple of (func_name, sheet_name)

# 5. Populate Sheets
for sdata in all_sheets_data:
    name = sdata['sheet_name']
    print(f"Populating sheet {name}...")
    sheet = wb[name]
    
    # Create Data Validation for Round 1, Round 2, Round 3 columns
    dv_round = DataValidation(type="list", formula1='"Passed,Failed,Pending,N/A,\xa0"', allow_blank=True)
    dv_round.error ='Only "Passed", "Failed", "Pending", "N/A", or space is allowed.'
    dv_round.errorTitle = 'Invalid Entry'
    dv_round.prompt = 'Select round status from the dropdown.'
    dv_round.promptTitle = 'Select Status'
    sheet.add_data_validation(dv_round)
    
    # Create Data Validation for Date 1, Date 2, Date 3 columns
    dv_date = DataValidation(type="date", operator="between", formula1="1", formula2="401769", allow_blank=True)
    dv_date.error ='Only valid dates (YYYY-MM-DD) between 1900-01-01 and 2999-12-31 are allowed.'
    dv_date.errorTitle = 'Invalid Date'
    dv_date.prompt = 'Enter a valid date (YYYY-MM-DD).'
    dv_date.promptTitle = 'Enter Date'
    sheet.add_data_validation(dv_date)
    
    # Write general info
    sheet.cell(2, 2).value = sdata['feature_name']
    sheet.cell(3, 2).value = sdata['requirements']
    
    # Explicitly color Row 10 headers to dark green (#137A37)
    for col_idx in range(1, 16):
        h_cell = sheet.cell(10, col_idx)
        h_cell.fill = header_fill
        h_cell.font = header_font
        
    max_template_row = sheet.max_row
    
    current_row = 11
    rows_data = sdata['rows']
    
    for rdata in rows_data:
        if rdata['type'] == 'function':
            sheet.cell(current_row, 1).value = rdata['name']
            all_functions_list.append((rdata['name'], name))
            
            for col_idx in range(1, 16):
                sheet.cell(current_row, col_idx).value = rdata['name'] if col_idx == 1 else None
                apply_saved_style(sheet.cell(current_row, col_idx), func_styles[col_idx-1], func_fill, func_font)
            sheet.row_dimensions[current_row].height = 15.75
            
        else: # test case row
            vals = [
                rdata['id'], rdata['desc'], rdata['proc'], rdata['expected'], rdata['pre'],
                rdata['round1'], rdata['date1'], rdata['tester1'],
                rdata['round2'], rdata['date2'], rdata['tester2'],
                rdata['round3'], rdata['date3'], rdata['tester3'],
                rdata['note']
            ]
            for col_idx, val in enumerate(vals, start=1):
                cell = sheet.cell(current_row, col_idx)
                cell.value = val
                if val and str(val).startswith('='):
                    cell.data_type = 's'
            
            for col_idx in range(1, 16):
                apply_saved_style(sheet.cell(current_row, col_idx), tc_styles[col_idx-1], tc_fill, tc_font)
            sheet.row_dimensions[current_row].height = None
            
            # Add Round cells to dropdown validation
            dv_round.add(sheet.cell(current_row, 6))
            dv_round.add(sheet.cell(current_row, 9))
            dv_round.add(sheet.cell(current_row, 12))
            
            # Add Date cells to date validation
            dv_date.add(sheet.cell(current_row, 7))
            dv_date.add(sheet.cell(current_row, 10))
            dv_date.add(sheet.cell(current_row, 13))
            
        current_row += 1
        
    last_row = current_row - 1
    if last_row < 11:
        sheet.cell(4, 2).value = 0
        for r_stat in [6, 7, 8]:
            sheet.cell(r_stat, 2).value = 0
            sheet.cell(r_stat, 3).value = 0
            sheet.cell(r_stat, 4).value = 0
            sheet.cell(r_stat, 5).value = 0
    else:
        sheet.cell(4, 2).value = f'=COUNTIF(A11:A{last_row}, "*-TC-*")'
        
        # Round 1
        sheet.cell(6, 2).value = f'=COUNTIF(F11:F{last_row}, B5)'
        sheet.cell(6, 3).value = f'=COUNTIF(F11:F{last_row}, C5)'
        sheet.cell(6, 4).value = f'=COUNTIF(F11:F{last_row}, D5)'
        sheet.cell(6, 5).value = f'=COUNTIF(F11:F{last_row}, E5)'
        
        # Round 2
        sheet.cell(7, 2).value = f'=COUNTIF(I11:I{last_row}, B5)'
        sheet.cell(7, 3).value = f'=COUNTIF(I11:I{last_row}, C5)'
        sheet.cell(7, 4).value = f'=COUNTIF(I11:I{last_row}, D5)'
        sheet.cell(7, 5).value = f'=COUNTIF(I11:I{last_row}, E5)'
        
        # Round 3
        sheet.cell(8, 2).value = f'=COUNTIF(L11:L{last_row}, B5)'
        sheet.cell(8, 3).value = f'=COUNTIF(L11:L{last_row}, C5)'
        sheet.cell(8, 4).value = f'=COUNTIF(L11:L{last_row}, D5)'
        sheet.cell(8, 5).value = f'=COUNTIF(L11:L{last_row}, E5)'
        
    # Clear any leftover rows from original template using safe default style objects
    if last_row < max_template_row:
        for r in range(last_row + 1, max_template_row + 1):
            for c in range(1, 16):
                cell = sheet.cell(r, c)
                cell.value = None
                cell.font = default_font
                cell.fill = default_fill
                cell.border = default_border
                cell.alignment = default_alignment

# 6. Populate "Test Cases" summary sheet
print("Populating Test Cases list...")
max_tc_list_row = tc_list_sheet.max_row
for r in range(9, max_tc_list_row + 1):
    for c in range(2, 7):
        cell = tc_list_sheet.cell(r, c)
        cell.value = None
        cell.font = default_font
        cell.fill = default_fill
        cell.border = default_border
        cell.alignment = default_alignment

# Explicitly style Test Cases headers in Row 8 to dark green (#137A37)
for col_idx in range(2, 7):
    h_cell = tc_list_sheet.cell(8, col_idx)
    h_cell.fill = header_fill
    h_cell.font = header_font

current_tc_row = 9
for idx, (func_name, s_name) in enumerate(all_functions_list, start=1):
    tc_list_sheet.cell(current_tc_row, 2).value = idx
    tc_list_sheet.cell(current_tc_row, 3).value = func_name
    tc_list_sheet.cell(current_tc_row, 4).value = s_name
    tc_list_sheet.cell(current_tc_row, 4).hyperlink = f"#'{s_name}'!A1"
    tc_list_sheet.cell(current_tc_row, 5).value = None
    tc_list_sheet.cell(current_tc_row, 6).value = None
    
    for c_idx in range(2, 7):
        apply_saved_style(tc_list_sheet.cell(current_tc_row, c_idx), tc_list_style[c_idx-2], tc_fill, tc_font)
    tc_list_sheet.cell(current_tc_row, 4).font = Font(name='Tahoma', size=10, color='0563C1', underline='single')
    tc_list_sheet.row_dimensions[current_tc_row].height = 18.0
    current_tc_row += 1

# 7. Populate "Test Statistics" summary sheet
print("Populating Test Statistics...")
max_stats_row = stats_sheet.max_row
for r in range(11, max_stats_row + 1):
    for c in range(2, 9):
        cell = stats_sheet.cell(r, c)
        cell.value = None
        cell.font = default_font
        cell.fill = default_fill
        cell.border = default_border
        cell.alignment = default_alignment

# Explicitly style Test Statistics headers in Row 10 to dark green (#137A37)
for col_idx in range(2, 9):
    h_cell = stats_sheet.cell(10, col_idx)
    h_cell.fill = header_fill
    h_cell.font = header_font

current_stats_row = 11
for idx, sdata in enumerate(all_sheets_data, start=1):
    name = sdata['sheet_name']
    
    stats_sheet.cell(current_stats_row, 2).value = idx
    stats_sheet.cell(current_stats_row, 3).value = f"='{name}'!B2"
    stats_sheet.cell(current_stats_row, 3).hyperlink = f"#'{name}'!A1"
    stats_sheet.cell(current_stats_row, 4).value = f"='{name}'!B6"
    stats_sheet.cell(current_stats_row, 5).value = f"='{name}'!C6"
    stats_sheet.cell(current_stats_row, 6).value = f"='{name}'!D6"
    stats_sheet.cell(current_stats_row, 7).value = f"='{name}'!E6"
    stats_sheet.cell(current_stats_row, 8).value = f"='{name}'!B4"
    
    for c_idx in range(2, 9):
        apply_saved_style(stats_sheet.cell(current_stats_row, c_idx), stats_data_style[c_idx-2], tc_fill, tc_font)
    stats_sheet.cell(current_stats_row, 3).font = Font(name='Tahoma', size=10, color='0563C1', underline='single')
    stats_sheet.row_dimensions[current_stats_row].height = 15.0
    current_stats_row += 1

# Empty row
current_stats_row += 1

# Sub total row
subtotal_row = current_stats_row
stats_sheet.cell(subtotal_row, 2).value = None
stats_sheet.cell(subtotal_row, 3).value = 'Sub total'
stats_sheet.cell(subtotal_row, 4).value = f"=SUM(D11:D{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 5).value = f"=SUM(E11:E{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 6).value = f"=SUM(F11:F{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 7).value = f"=SUM(G11:G{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 8).value = f"=SUM(H11:H{subtotal_row-2})"

for c_idx in range(2, 9):
    apply_saved_style(stats_sheet.cell(subtotal_row, c_idx), stats_subtotal_style[c_idx-2], tc_fill, func_font)
stats_sheet.row_dimensions[subtotal_row].height = 15.0

# Empty row
current_stats_row += 2

# Test coverage row
coverage_row = current_stats_row
stats_sheet.cell(coverage_row, 2).value = None
stats_sheet.cell(coverage_row, 3).value = 'Test coverage'
stats_sheet.cell(coverage_row, 4).value = None
stats_sheet.cell(coverage_row, 5).value = f"=(D{subtotal_row}+E{subtotal_row})*100/(H{subtotal_row}-G{subtotal_row})"
stats_sheet.cell(coverage_row, 6).value = '%'

for c_idx in range(2, 7):
    apply_saved_style(stats_sheet.cell(coverage_row, c_idx), stats_coverage_style[c_idx-2], tc_fill, func_font)
stats_sheet.row_dimensions[coverage_row].height = 15.0

# Test successful coverage row
current_stats_row += 1
success_row = current_stats_row
stats_sheet.cell(success_row, 2).value = None
stats_sheet.cell(success_row, 3).value = 'Test successful coverage'
stats_sheet.cell(success_row, 4).value = None
stats_sheet.cell(success_row, 5).value = f"=D{subtotal_row}*100/(H{subtotal_row}-G{subtotal_row})"
stats_sheet.cell(success_row, 6).value = '%'

for c_idx in range(2, 7):
    apply_saved_style(stats_sheet.cell(success_row, c_idx), stats_success_style[c_idx-2], tc_fill, func_font)
stats_sheet.row_dimensions[success_row].height = 15.0

# 8. Save modified workbook
print("Saving modified Excel file...")
wb.save(excel_path)
print("Finished populating Excel file successfully!")
