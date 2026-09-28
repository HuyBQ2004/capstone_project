import os
import re
import sys
import copy
import shutil
import openpyxl
from openpyxl.styles import Font, PatternFill, Border, Alignment
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

sys.stdout.reconfigure(encoding='utf-8')

# Paths
base_dir = os.path.dirname(os.path.abspath(__file__))
local_template_path = os.path.join(base_dir, "Report5_Unit Test_Template.xlsx")
local_output_path = os.path.join(base_dir, "Report5_Unit Test.xlsx")
downloads_dir = r""
downloads_output_path = os.path.join(downloads_dir, "Report5_Unit Test.xlsx")

print("Starting populate_ut_report.py with explicit green colors...")
print(f"Base Directory: {base_dir}")
print(f"Local Template Path: {local_template_path}")
print(f"Local Output Path: {local_output_path}")

# 1. Parse Markdown Files
def parse_ui_md_file(path, class_name):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Split by function header
    func_blocks = content.split('\n## Function:')
    if len(func_blocks) < 2 and content.startswith('## Function:'):
        func_blocks = [''] + content.split('## Function:')
    
    parsed_funcs = []
    for block in func_blocks[1:]:
        lines = block.split('\n')
        func_title = lines[0].strip()
        
        general_info = {}
        matrix_rows = []
        result_rows = {}
        note_text = ""
        
        current_section = None
        
        for line in lines[1:]:
            line_str = line.strip()
            if not line_str:
                continue
                
            # Section detection
            if '### 1. General Information' in line_str:
                current_section = 'general'
                continue
            elif '### 2. Result Summary' in line_str:
                current_section = 'summary'
                continue
            elif '### 3. Condition / Confirmation Matrix' in line_str:
                current_section = 'matrix'
                continue
            elif '### 4. Execution Result' in line_str:
                current_section = 'result'
                continue
            elif line_str.startswith('Note:'):
                note_text = line_str[5:].strip()
                continue
                
            if current_section == 'general':
                if line_str.startswith('|'):
                    row_parts = [p.strip() for p in line_str.split('|')]
                    if row_parts and row_parts[0] == '': row_parts = row_parts[1:]
                    if row_parts and row_parts[-1] == '': row_parts = row_parts[:-1]
                    for i in range(len(row_parts) - 1):
                        key = row_parts[i].strip().replace('`', '').replace(':', '').strip()
                        if key in ['Function Code', 'Function Name', 'Created By', 'Executed By', 'Lines of code', 'Lines  of code', 'Lack of test cases', 'Test requirement']:
                            val = row_parts[i+1].strip().replace('`', '').strip()
                            general_info[key] = val
                            
            elif current_section == 'matrix':
                if line_str.startswith('|'):
                    temp_line = re.sub(r'`[^`]*`', lambda m: m.group(0).replace('|', '__PIPE__'), line_str)
                    temp_line = temp_line.replace('\\|', '__PIPE__')
                    
                    parts = [p.strip() for p in temp_line.split('|')]
                    if parts and parts[0] == '': parts = parts[1:]
                    if parts and parts[-1] == '': parts = parts[:-1]
                    
                    if all(re.match(r'^:?-+:?$', p) for p in parts):
                        continue
                        
                    matrix_rows.append(parts)
                    
            elif current_section == 'result':
                if line_str.startswith('|'):
                    row_parts = [p.strip() for p in line_str.split('|')]
                    if row_parts and row_parts[0] == '': row_parts = row_parts[1:]
                    if row_parts and row_parts[-1] == '': row_parts = row_parts[:-1]
                    
                    if all(re.match(r'^:?-+:?$', p) for p in row_parts):
                        continue
                        
                    if 'executed date' in row_parts[0].lower() or 'type' in row_parts[0].lower() or 'passed/failed' in row_parts[0].lower() or 'defect id' in row_parts[0].lower() or 'utcid' in ''.join(row_parts).lower():
                        if 'utcid' in ''.join(row_parts).lower():
                            result_rows['headers'] = [p.replace('`', '').strip() for p in row_parts]
                        else:
                            key = row_parts[0].replace('`', '').strip()
                            result_rows[key] = [p.replace('`', '').strip() for p in row_parts[1:]]
                            
        utcids = []
        for parts in matrix_rows:
            if any('utcid' in p.lower() for p in parts):
                utcids = [p.replace('`', '').strip() for p in parts if p.strip() != '']
                utcids = [u for u in utcids if u.startswith('UTCID')]
                break
                
        conditions = []
        confirmations = []
        mode = 'condition'
        
        for parts in matrix_rows:
            non_empty_parts = [p for p in parts if p != '']
            if len(non_empty_parts) == 1:
                val = non_empty_parts[0].replace('**', '').strip()
                if 'condition' in val.lower():
                    mode = 'condition'
                    continue
                elif 'confirm' in val.lower():
                    mode = 'confirm'
                    continue
            
            if any('utcid' in p.lower() for p in parts):
                continue
                
            if len(parts) >= 2:
                var_name = parts[0].replace('**', '').strip().replace('__PIPE__', '|')
                val_desc = parts[1].replace('__PIPE__', '|').strip()
                selections = [p.strip() for p in parts[2:]]
                
                if len(selections) < len(utcids):
                    selections += [''] * (len(utcids) - len(selections))
                selections = selections[:len(utcids)]
                
                if mode == 'condition':
                    conditions.append((var_name, val_desc, selections))
                else:
                    confirmations.append((var_name, val_desc, selections))
                    
        utc_details = {}
        for u in utcids:
            utc_details[u] = {'type': 'N', 'status': 'Untested', 'date': '', 'defect': ''}
            
        for key, vals in result_rows.items():
            if key == 'headers': continue
            key_lower = key.lower()
            if 'type' in key_lower:
                for idx, val in enumerate(vals):
                    if idx < len(utcids):
                        utc_details[utcids[idx]]['type'] = val
            elif 'passed' in key_lower:
                for idx, val in enumerate(vals):
                    if idx < len(utcids):
                        utc_details[utcids[idx]]['status'] = val
            elif 'date' in key_lower:
                for idx, val in enumerate(vals):
                    if idx < len(utcids):
                        utc_details[utcids[idx]]['date'] = val
            elif 'defect' in key_lower:
                for idx, val in enumerate(vals):
                    if idx < len(utcids):
                        utc_details[utcids[idx]]['defect'] = val
                        
        parsed_funcs.append({
            'code': general_info.get('Function Code', 'UNKNOWN'),
            'name': general_info.get('Function Name', func_title),
            'author': general_info.get('Created By', ''),
            'tester': general_info.get('Executed By', 'QA Team'),
            'loc': general_info.get('Lines of code', general_info.get('Lines  of code', '0')),
            'lack_tcs': general_info.get('Lack of test cases', '0'),
            'requirement': general_info.get('Test requirement', ''),
            'utcids': utcids,
            'conditions': conditions,
            'confirmations': confirmations,
            'utc_details': utc_details,
            'note': note_text,
            'class_name': class_name
        })
        
    return parsed_funcs

# Scan folders MF* and parse markdown files
folders = sorted([f for f in os.listdir(base_dir) if os.path.isdir(os.path.join(base_dir, f)) and f.startswith('MF')])
all_functions = []

for f in folders:
    f_path = os.path.join(base_dir, f)
    md_files = sorted([m for m in os.listdir(f_path) if m.endswith('.md')])
    for md in md_files:
        class_name = md[:-3]
        funcs = parse_ui_md_file(os.path.join(f_path, md), class_name)
        mf_prefix = f.split('_')[0] # e.g. "MF01"
        for func in funcs:
            func['code'] = f"{mf_prefix}_{func['code']}"
        all_functions.extend(funcs)
        print(f"Parsed {len(funcs)} functions from {md}")

print(f"Total parsed functions: {len(all_functions)}")

def extract_func_number(func_dict):
    code = func_dict['code']
    parts = code.rsplit('-', 1)
    if len(parts) == 2 and parts[1].isdigit():
        return (parts[0], int(parts[1]))
    return (code, 0)

all_functions.sort(key=extract_func_number)

if not all_functions:
    print("Error: No functions found to process!")
    sys.exit(1)

# 2. Load Template Excel File
wb = openpyxl.load_workbook(local_template_path)

# Extract styles from template sheet Function 1
temp_sheet = wb['Function 1']

def copy_cell_style_dict(cell):
    return {
        'font': copy.copy(cell.font),
        'fill': copy.copy(cell.fill),
        'border': copy.copy(cell.border),
        'alignment': copy.copy(cell.alignment),
        'number_format': cell.number_format,
        'protection': copy.copy(cell.protection)
    }

style_A10 = copy_cell_style_dict(temp_sheet['A10'])
style_B10 = copy_cell_style_dict(temp_sheet['B10'])
style_D11 = copy_cell_style_dict(temp_sheet['D11'])
style_F11 = copy_cell_style_dict(temp_sheet['F11'])
style_B39 = copy_cell_style_dict(temp_sheet['B39'])
style_F39 = copy_cell_style_dict(temp_sheet['F39'])
style_F9 = copy_cell_style_dict(temp_sheet['F9'])
style_B40 = copy_cell_style_dict(temp_sheet['B40'])
style_B41 = copy_cell_style_dict(temp_sheet['B41'])
style_B42 = copy_cell_style_dict(temp_sheet['B42'])
style_F40 = copy_cell_style_dict(temp_sheet['F40'])
style_F41 = copy_cell_style_dict(temp_sheet['F41'])
style_F42 = copy_cell_style_dict(temp_sheet['F42'])

# Extract styles from summary sheets
tc_list_sheet = wb['Functions']
tc_list_style = []
for c in range(1, 9):
    cell = tc_list_sheet.cell(11, c)
    tc_list_style.append(copy_cell_style_dict(cell))

stats_sheet = wb['Statistics']
stats_data_style = []
for c in range(1, 10):
    cell = stats_sheet.cell(12, c)
    stats_data_style.append(copy_cell_style_dict(cell))

stats_subtotal_style = []
for c in range(1, 10):
    cell = stats_sheet.cell(17, c)
    stats_subtotal_style.append(copy_cell_style_dict(cell))

stats_coverage_style = []
for c in range(1, 6):
    cell = stats_sheet.cell(19, c)
    stats_coverage_style.append(copy_cell_style_dict(cell))

stats_success_style = []
for c in range(1, 6):
    cell = stats_sheet.cell(20, c)
    stats_success_style.append(copy_cell_style_dict(cell))

# Define green theme overrides
header_fill = PatternFill(fill_type='solid', fgColor='137A37')
header_font = Font(name='Tahoma', size=10, bold=True, color='FFFFFF')

section_fill = PatternFill(fill_type='solid', fgColor='E2EFDA')
section_font = Font(name='Tahoma', size=10, bold=True, color='000000')

tc_fill = PatternFill(fill_type=None)
tc_font = Font(name='Tahoma', size=10, bold=False, color='000000')

default_font = Font()
default_fill = PatternFill(fill_type=None)
default_border = Border()
default_alignment = Alignment()

def apply_saved_style(cell, style_dict, fill_override, font_override):
    if style_dict['border']: cell.border = copy.copy(style_dict['border'])
    if style_dict['alignment']: cell.alignment = copy.copy(style_dict['alignment'])
    cell.number_format = style_dict['number_format']
    if style_dict['protection']: cell.protection = copy.copy(style_dict['protection'])
    cell.fill = fill_override
    cell.font = font_override

def apply_cell_style_dict(cell, style_dict):
    if style_dict['font']: cell.font = copy.copy(style_dict['font'])
    if style_dict['fill']: cell.fill = copy.copy(style_dict['fill'])
    if style_dict['border']: cell.border = copy.copy(style_dict['border'])
    if style_dict['alignment']: cell.alignment = copy.copy(style_dict['alignment'])
    cell.number_format = style_dict['number_format']
    if style_dict['protection']: cell.protection = copy.copy(style_dict['protection'])

def merge_and_style_cells(sheet, start_row, start_col, end_row, end_col, style_dict, fill_override=None, font_override=None, alignment_override=None):
    sheet.merge_cells(start_row=start_row, start_column=start_col, end_row=end_row, end_column=end_col)
    for r in range(start_row, end_row + 1):
        for c in range(start_col, end_col + 1):
            cell = sheet.cell(r, c)
            apply_cell_style_dict(cell, style_dict)
            if fill_override is not None:
                cell.fill = fill_override
            if font_override is not None:
                cell.font = font_override
            if alignment_override is not None:
                cell.alignment = alignment_override

import math
def estimate_row_height(text, char_limit=45, line_height=14, padding=8):
    if not text:
        return 20.0
    text_str = str(text)
    lines = text_str.split('\n')
    total_lines = 0
    for line in lines:
        total_lines += max(1, math.ceil(len(line) / char_limit))
    return max(20.0, total_lines * line_height + padding)

def clean_markdown_symbols(text):
    if not text:
        return ""
    text_str = str(text)
    # Remove bold/italic markers
    text_str = re.sub(r'\*\*\*([^*]+)\*\*\*', r'\1', text_str)
    text_str = re.sub(r'\*\*([^*]+)\*\*', r'\1', text_str)
    text_str = re.sub(r'\*([^*]+)\*', r'\1', text_str)
    # Remove inline code backticks
    text_str = re.sub(r'`([^`]+)`', r'\1', text_str)
    return text_str.strip()

# 3. Create worksheets for all functions as clean copies of Function 1
print("Creating worksheets for functions...")
for fdata in all_functions:
    code = fdata['code']
    print(f"Copying Function 1 to create {code}...")
    new_sheet = wb.copy_worksheet(wb['Function 1'])
    new_sheet.title = code

# Remove the original template sheets
for t_name in ['Function 1', 'Function 2', 'Function3']:
    if t_name in wb.sheetnames:
        wb.remove(wb[t_name])

# Reorder worksheets
sheets_order = ['Guideline', 'Cover', 'Functions', 'Statistics'] + [f['code'] for f in all_functions] + ['Example']
wb._sheets = [wb[s] for s in sheets_order if s in wb.sheetnames]

# 4. Populate each Function sheet
for func_idx, fdata in enumerate(all_functions, start=11): # starting at row 11 in Functions list sheet
    code = fdata['code']
    print(f"Populating worksheet {code}...")
    sheet = wb[code]
    
    # Write general info
    sheet.cell(2, 3).value = f"=Functions!E{func_idx}"
    sheet.cell(2, 12).value = fdata['name']
    sheet.cell(3, 3).value = fdata['author']
    sheet.cell(3, 12).value = fdata['tester']
    
    try:
        sheet.cell(4, 3).value = int(fdata['loc'])
    except:
        sheet.cell(4, 3).value = 0
        
    try:
        sheet.cell(4, 12).value = int(fdata['lack_tcs'])
    except:
        sheet.cell(4, 12).value = 0
        
    sheet.cell(5, 3).value = clean_markdown_symbols(fdata['requirement'])
    req_h = estimate_row_height(fdata['requirement'], char_limit=130, line_height=14, padding=8)
    sheet.row_dimensions[5].height = req_h

    # Style Row 9 Columns A to E to dark green (header separator)
    for col_idx in range(1, 6):
        sheet.cell(9, col_idx).fill = header_fill

    # Fill UTCID headers in Row 9
    for idx, utcid in enumerate(fdata['utcids']):
        col_idx = 6 + idx
        cell = sheet.cell(9, col_idx)
        cell.value = utcid
        cell.fill = header_fill
        cell.font = header_font
        cell.alignment = copy.copy(style_F9['alignment'])
        cell.border = copy.copy(style_F9['border'])
        
    # Clear any extra columns in Row 9
    for col_idx in range(6 + len(fdata['utcids']), sheet.max_column + 1):
        sheet.cell(9, col_idx).value = None
        sheet.cell(9, col_idx).fill = default_fill
        sheet.cell(9, col_idx).font = default_font
        sheet.cell(9, col_idx).border = default_border
        
    # Remove all merged ranges at or below row 10 to prevent overlapping/corrupt merges
    ranges_to_remove = []
    for rng in list(sheet.merged_cells.ranges):
        if rng.min_row >= 10:
            ranges_to_remove.append(rng)
    for rng in ranges_to_remove:
        sheet.merged_cells.remove(rng)

    # Delete matrix rows from 10 to 42 in template copy
    sheet.delete_rows(10, 33)
    
    # Insert rows for our matrix
    num_cond = len(fdata['conditions'])
    num_conf = len(fdata['confirmations'])
    total_matrix_rows = num_cond + num_conf + 7 # conditions + confirms + ResultHeader + 4 result rows + note row
    sheet.insert_rows(10, total_matrix_rows)
    
    # Create Data Validation for selection cells in the matrix
    dv = DataValidation(type="list", formula1='"O,\xa0"', allow_blank=True)
    dv.error ='Only "O" or space is allowed.'
    dv.errorTitle = 'Invalid Entry'
    dv.prompt = 'Select "O" or space from the dropdown, or delete to clear.'
    dv.promptTitle = 'Select Mark'
    sheet.add_data_validation(dv)
    
    # Write conditions
    current_row = 10
    for idx, (var_name, val_desc, selections) in enumerate(fdata['conditions']):
        r = current_row + idx
        
        # Col B: Variable name
        cell_b = sheet.cell(r, 2)
        val_clean_b = clean_markdown_symbols(var_name) if var_name else None
        cell_b.value = val_clean_b
        if val_clean_b and val_clean_b.startswith('='):
            cell_b.data_type = 's'
        apply_cell_style_dict(cell_b, style_B10)
        cell_b.fill = default_fill
        
        # Merge Column C and D for Value description
        val_clean_d = clean_markdown_symbols(val_desc)
        sheet.cell(r, 3).value = val_clean_d
        if val_clean_d and val_clean_d.startswith('='):
            sheet.cell(r, 3).data_type = 's'
        merge_and_style_cells(
            sheet, 
            start_row=r, start_col=3, 
            end_row=r, end_col=4, 
            style_dict=style_D11, 
            fill_override=default_fill,
            alignment_override=Alignment(horizontal='left', vertical='top', wrap_text=True)
        )
        
        # Col E: empty
        cell_e = sheet.cell(r, 5)
        cell_e.value = None
        cell_e.border = copy.copy(style_D11['border'])
        cell_e.fill = default_fill
        
        for u_idx, sel in enumerate(selections):
            c_idx = 6 + u_idx
            cell_sel = sheet.cell(r, c_idx)
            sel_val = sel.strip() if sel else ""
            if sel_val:
                if sel_val.upper().startswith('O'):
                    sel_val = 'O'
                elif sel_val.upper().startswith('X'):
                    sel_val = 'X'
            cell_sel.value = sel_val if sel_val else None
            apply_cell_style_dict(cell_sel, style_F11)
            cell_sel.fill = default_fill
            dv.add(cell_sel)
            
        for c_idx in range(6 + len(selections), sheet.max_column + 1):
            sheet.cell(r, c_idx).value = None
            sheet.cell(r, c_idx).fill = default_fill
            sheet.cell(r, c_idx).font = default_font
            sheet.cell(r, c_idx).border = default_border
            
        sheet.row_dimensions[r].height = estimate_row_height(val_desc, char_limit=45, line_height=14, padding=8)
        
    # Merge and style Column A for Condition section
    sheet.cell(10, 1).value = "Condition"
    merge_and_style_cells(
        sheet, 
        start_row=10, start_col=1, 
        end_row=10 + num_cond - 1, end_col=1, 
        style_dict=style_A10, 
        fill_override=section_fill, 
        font_override=section_font, 
        alignment_override=Alignment(horizontal='center', vertical='center', text_rotation=90)
    )

    # Write confirmations
    current_row = 10 + num_cond
    for idx, (var_name, val_desc, selections) in enumerate(fdata['confirmations']):
        r = current_row + idx
        
        # Col B: Variable name
        cell_b = sheet.cell(r, 2)
        val_clean_b = clean_markdown_symbols(var_name) if var_name else None
        cell_b.value = val_clean_b
        if val_clean_b and val_clean_b.startswith('='):
            cell_b.data_type = 's'
        apply_cell_style_dict(cell_b, style_B10)
        cell_b.fill = default_fill
        
        # Merge Column C and D for Value description
        val_clean_d = clean_markdown_symbols(val_desc)
        sheet.cell(r, 3).value = val_clean_d
        if val_clean_d and val_clean_d.startswith('='):
            sheet.cell(r, 3).data_type = 's'
        merge_and_style_cells(
            sheet, 
            start_row=r, start_col=3, 
            end_row=r, end_col=4, 
            style_dict=style_D11, 
            fill_override=default_fill,
            alignment_override=Alignment(horizontal='left', vertical='top', wrap_text=True)
        )
        
        # Col E: empty
        cell_e = sheet.cell(r, 5)
        cell_e.value = None
        cell_e.border = copy.copy(style_D11['border'])
        cell_e.fill = default_fill
        
        for u_idx, sel in enumerate(selections):
            c_idx = 6 + u_idx
            cell_sel = sheet.cell(r, c_idx)
            sel_val = sel.strip() if sel else ""
            if sel_val:
                if sel_val.upper().startswith('O'):
                    sel_val = 'O'
                elif sel_val.upper().startswith('X'):
                    sel_val = 'X'
            cell_sel.value = sel_val if sel_val else None
            apply_cell_style_dict(cell_sel, style_F11)
            cell_sel.fill = default_fill
            dv.add(cell_sel)
            
        for c_idx in range(6 + len(selections), sheet.max_column + 1):
            sheet.cell(r, c_idx).value = None
            sheet.cell(r, c_idx).fill = default_fill
            sheet.cell(r, c_idx).font = default_font
            sheet.cell(r, c_idx).border = default_border
            
        sheet.row_dimensions[r].height = estimate_row_height(val_desc, char_limit=45, line_height=14, padding=8)
        
    # Merge and style Column A for Confirm section
    confirm_start = 10 + num_cond
    confirm_end = confirm_start + num_conf - 1
    sheet.cell(confirm_start, 1).value = "Confirm"
    merge_and_style_cells(
        sheet, 
        start_row=confirm_start, start_col=1, 
        end_row=confirm_end, end_col=1, 
        style_dict=style_A10, 
        fill_override=section_fill, 
        font_override=section_font, 
        alignment_override=Alignment(horizontal='center', vertical='center', text_rotation=90)
    )

    # Write Results
    current_row = 10 + num_cond + num_conf
    result_labels = [
        ("Type(N : Normal, A : Abnormal, B : Boundary)", 'type', style_B39, style_F39),
        ("Passed/Failed", 'status', style_B40, style_F40),
        ("Executed Date", 'date', style_B41, style_F41),
        ("Defect ID", 'defect', style_B42, style_F42)
    ]
    
    import datetime
    
    # Create Data Validation for Type and Status cells
    dv_type = DataValidation(type="list", formula1='"N,A,B,\xa0"', allow_blank=True)
    dv_type.error ='Only "N", "A", "B", or space is allowed.'
    dv_type.errorTitle = 'Invalid Entry'
    dv_type.prompt = 'Select "N", "A", or "B" from the dropdown.'
    dv_type.promptTitle = 'Select Type'
    sheet.add_data_validation(dv_type)
    
    dv_status = DataValidation(type="list", formula1='"P,F,\xa0"', allow_blank=True)
    dv_status.error ='Only "P", "F", or space is allowed.'
    dv_status.errorTitle = 'Invalid Entry'
    dv_status.prompt = 'Select "P" or "F" from the dropdown.'
    dv_status.promptTitle = 'Select Status'
    sheet.add_data_validation(dv_status)
    
    # Create Data Validation for Date cells
    dv_date = DataValidation(type="date", operator="between", formula1="1", formula2="401769", allow_blank=True)
    dv_date.error ='Only valid dates (YYYY-MM-DD) between 1900-01-01 and 2999-12-31 are allowed.'
    dv_date.errorTitle = 'Invalid Date'
    dv_date.prompt = 'Enter a valid date (YYYY-MM-DD).'
    dv_date.promptTitle = 'Enter Date'
    sheet.add_data_validation(dv_date)
    
    for idx, (label, key, label_style, val_style) in enumerate(result_labels):
        r = current_row + idx
        
        # Merge Column B to D for Label
        sheet.cell(r, 2).value = label
        merge_and_style_cells(
            sheet, 
            start_row=r, start_col=2, 
            end_row=r, end_col=4, 
            style_dict=label_style, 
            fill_override=default_fill
        )
        
        # Col E: empty
        cell_e = sheet.cell(r, 5)
        cell_e.value = None
        cell_e.border = copy.copy(label_style['border'])
        cell_e.fill = default_fill
        
        # Col F onwards: UTCID values
        for u_idx, utcid in enumerate(fdata['utcids']):
            c_idx = 6 + u_idx
            cell_val = sheet.cell(r, c_idx)
            
            val = fdata['utc_details'][utcid][key]
            if key == 'status':
                if val.lower().startswith('pass') or val.lower() == 'p':
                    val = 'P'
                elif val.lower().startswith('fail') or val.lower() == 'f':
                    val = 'F'
                else:
                    val = ''
                cell_val.value = val if val else None
            elif key == 'date':
                if val:
                    try:
                        # Parse "YYYY-MM-DD"
                        date_obj = datetime.datetime.strptime(val, "%Y-%m-%d").date()
                        cell_val.value = date_obj
                    except Exception:
                        cell_val.value = val
                else:
                    cell_val.value = None
            else:
                cell_val.value = val if val else None
                
            apply_cell_style_dict(cell_val, val_style)
            cell_val.fill = default_fill
            if key == 'type':
                dv_type.add(cell_val)
            elif key == 'status':
                dv_status.add(cell_val)
            elif key == 'date':
                dv_date.add(cell_val)
            
        for c_idx in range(6 + len(fdata['utcids']), sheet.max_column + 1):
            sheet.cell(r, c_idx).value = None
            sheet.cell(r, c_idx).fill = default_fill
            sheet.cell(r, c_idx).font = default_font
            sheet.cell(r, c_idx).border = default_border
        if key == 'date':
            sheet.row_dimensions[r].height = 42.0
        else:
            sheet.row_dimensions[r].height = 20.0

    # Merge and style Column A for Result section
    result_start = current_row
    result_end = result_start + 3
    sheet.cell(result_start, 1).value = "Result"
    merge_and_style_cells(
        sheet, 
        start_row=result_start, start_col=1, 
        end_row=result_end, end_col=1, 
        style_dict=style_A10, 
        fill_override=section_fill, 
        font_override=section_font, 
        alignment_override=Alignment(horizontal='center', vertical='center', text_rotation=90)
    )

    # Note Row
    r_note = current_row + 4
    cell_a = sheet.cell(r_note, 1)
    cell_a.value = None
    cell_a.fill = default_fill
    cell_a.border = default_border
    
    if fdata['note']:
        sheet.merge_cells(start_row=r_note, start_column=2, end_row=r_note, end_column=15)
        cell_b = sheet.cell(r_note, 2)
        cleaned_note = clean_markdown_symbols(fdata['note'])
        cell_b.value = f"Note: {cleaned_note}"
        cell_b.font = Font(name='Tahoma', size=9, italic=True)
        cell_b.alignment = Alignment(horizontal='left', vertical='top', wrap_text=True)
        sheet.row_dimensions[r_note].height = estimate_row_height(f"Note: {cleaned_note}", char_limit=110, line_height=13, padding=10)
    else:
        sheet.row_dimensions[r_note].height = 15.0
        
    # Formulas in Row 7
    type_row = current_row
    status_row = current_row + 1
    
    sheet.cell(7, 1).value = f'=COUNTIF(F{status_row}:HQ{status_row},"P")'
    sheet.cell(7, 3).value = f'=COUNTIF(F{status_row}:HQ{status_row},"F")'
    sheet.cell(7, 6).value = f'=SUM(O7,- A7,- C7)'
    sheet.cell(7, 12).value = f'=COUNTIF(F{type_row}:HQ{type_row},"N")'
    sheet.cell(7, 13).value = f'=COUNTIF(F{type_row}:HQ{type_row},"A")'
    sheet.cell(7, 14).value = f'=COUNTIF(F{type_row}:HQ{type_row},"B")'
    sheet.cell(7, 15).value = f'=COUNTA(F9:HT9)'

# 5. Populate "Functions" list sheet
print("Populating Functions list...")
max_func_list_row = tc_list_sheet.max_row
for r in range(11, max_func_list_row + 1):
    for c in range(1, 9):
        cell = tc_list_sheet.cell(r, c)
        cell.value = None
        cell.font = default_font
        cell.fill = default_fill
        cell.border = default_border
        cell.alignment = default_alignment

# Style headers in Row 10 to dark green
for col_idx in range(1, 9):
    h_cell = tc_list_sheet.cell(10, col_idx)
    h_cell.fill = header_fill
    h_cell.font = header_font

current_func_row = 11
for idx, fdata in enumerate(all_functions, start=1):
    tc_list_sheet.cell(current_func_row, 1).value = idx
    tc_list_sheet.cell(current_func_row, 2).value = None
    tc_list_sheet.cell(current_func_row, 3).value = clean_markdown_symbols(fdata['class_name'])
    tc_list_sheet.cell(current_func_row, 4).value = clean_markdown_symbols(fdata['name'])
    tc_list_sheet.cell(current_func_row, 5).value = clean_markdown_symbols(fdata['code'])
    
    sheet_name = clean_markdown_symbols(fdata['code'])
    tc_list_sheet.cell(current_func_row, 6).value = sheet_name
    tc_list_sheet.cell(current_func_row, 6).hyperlink = f"#'{sheet_name}'!A1"
    
    tc_list_sheet.cell(current_func_row, 7).value = clean_markdown_symbols(fdata['requirement'])
    tc_list_sheet.cell(current_func_row, 8).value = None
    
    for c_idx in range(1, 9):
        apply_saved_style(tc_list_sheet.cell(current_func_row, c_idx), tc_list_style[c_idx-1], tc_fill, tc_font)
    tc_list_sheet.cell(current_func_row, 6).font = Font(name='Tahoma', size=10, color='0563C1', underline='single')
    tc_list_sheet.row_dimensions[current_func_row].height = estimate_row_height(fdata['requirement'], char_limit=70, line_height=13, padding=6)
    current_func_row += 1

# 6. Populate "Statistics" sheet
print("Populating Statistics sheet...")
max_stats_row = stats_sheet.max_row
for r in range(12, max_stats_row + 1):
    for c in range(1, 10):
        cell = stats_sheet.cell(r, c)
        cell.value = None
        cell.font = default_font
        cell.fill = default_fill
        cell.border = default_border
        cell.alignment = default_alignment

# Style headers in Row 11 to dark green
for col_idx in range(1, 10):
    h_cell = stats_sheet.cell(11, col_idx)
    h_cell.fill = header_fill
    h_cell.font = header_font

current_stats_row = 12
for idx, fdata in enumerate(all_functions, start=1):
    code = fdata['code']
    stats_sheet.cell(current_stats_row, 1).value = idx
    stats_sheet.cell(current_stats_row, 2).value = code
    stats_sheet.cell(current_stats_row, 2).hyperlink = f"#'{code}'!A1"
    stats_sheet.cell(current_stats_row, 3).value = f"='{code}'!A7"
    stats_sheet.cell(current_stats_row, 4).value = f"='{code}'!C7"
    stats_sheet.cell(current_stats_row, 5).value = f"='{code}'!F7"
    stats_sheet.cell(current_stats_row, 6).value = f"='{code}'!L7"
    stats_sheet.cell(current_stats_row, 7).value = f"='{code}'!M7"
    stats_sheet.cell(current_stats_row, 8).value = f"='{code}'!N7"
    stats_sheet.cell(current_stats_row, 9).value = f"='{code}'!O7"
    
    for c_idx in range(1, 10):
        apply_saved_style(stats_sheet.cell(current_stats_row, c_idx), stats_data_style[c_idx-1], tc_fill, tc_font)
    stats_sheet.cell(current_stats_row, 2).font = Font(name='Tahoma', size=10, color='0563C1', underline='single')
    stats_sheet.row_dimensions[current_stats_row].height = 15.0
    current_stats_row += 1

# Empty row
current_stats_row += 1

# Sub total row
subtotal_row = current_stats_row
stats_sheet.cell(subtotal_row, 1).value = None
stats_sheet.cell(subtotal_row, 2).value = 'Sub total'
stats_sheet.cell(subtotal_row, 3).value = f"=SUM(C12:C{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 4).value = f"=SUM(D12:D{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 5).value = f"=SUM(E12:E{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 6).value = f"=SUM(F12:F{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 7).value = f"=SUM(G12:G{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 8).value = f"=SUM(H12:H{subtotal_row-2})"
stats_sheet.cell(subtotal_row, 9).value = f"=SUM(I12:I{subtotal_row-2})"

for c_idx in range(1, 10):
    apply_saved_style(stats_sheet.cell(subtotal_row, c_idx), stats_subtotal_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[subtotal_row].height = 15.0

# Empty row
current_stats_row += 2

# Test coverage row
coverage_row = current_stats_row
stats_sheet.cell(coverage_row, 1).value = None
stats_sheet.cell(coverage_row, 2).value = 'Test coverage'
stats_sheet.cell(coverage_row, 3).value = None
stats_sheet.cell(coverage_row, 4).value = f"=(C{subtotal_row}+D{subtotal_row})*100/(I{subtotal_row})"
stats_sheet.cell(coverage_row, 5).value = '%'

for c_idx in range(1, 6):
    apply_saved_style(stats_sheet.cell(coverage_row, c_idx), stats_coverage_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[coverage_row].height = 15.0

# Test successful coverage row
current_stats_row += 1
success_row = current_stats_row
stats_sheet.cell(success_row, 1).value = None
stats_sheet.cell(success_row, 2).value = 'Test successful coverage'
stats_sheet.cell(success_row, 3).value = None
stats_sheet.cell(success_row, 4).value = f"=C{subtotal_row}*100/(I{subtotal_row})"
stats_sheet.cell(success_row, 5).value = '%'

for c_idx in range(1, 6):
    apply_saved_style(stats_sheet.cell(success_row, c_idx), stats_success_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[success_row].height = 15.0

# Normal case coverage row
current_stats_row += 1
normal_row = current_stats_row
stats_sheet.cell(normal_row, 1).value = None
stats_sheet.cell(normal_row, 2).value = 'Normal case'
stats_sheet.cell(normal_row, 3).value = None
stats_sheet.cell(normal_row, 4).value = f"=F{subtotal_row}*100/(I{subtotal_row})"
stats_sheet.cell(normal_row, 5).value = '%'

for c_idx in range(1, 6):
    apply_saved_style(stats_sheet.cell(normal_row, c_idx), stats_coverage_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[normal_row].height = 15.0

# Abnormal case coverage row
current_stats_row += 1
abnormal_row = current_stats_row
stats_sheet.cell(abnormal_row, 1).value = None
stats_sheet.cell(abnormal_row, 2).value = 'Abnormal case'
stats_sheet.cell(abnormal_row, 3).value = None
stats_sheet.cell(abnormal_row, 4).value = f"=G{subtotal_row}*100/(I{subtotal_row})"
stats_sheet.cell(abnormal_row, 5).value = '%'

for c_idx in range(1, 6):
    apply_saved_style(stats_sheet.cell(abnormal_row, c_idx), stats_coverage_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[abnormal_row].height = 15.0

# Boundary case coverage row
current_stats_row += 1
boundary_row = current_stats_row
stats_sheet.cell(boundary_row, 1).value = None
stats_sheet.cell(boundary_row, 2).value = 'Boundary case'
stats_sheet.cell(boundary_row, 3).value = None
stats_sheet.cell(boundary_row, 4).value = f"=H{subtotal_row}*100/(I{subtotal_row})"
stats_sheet.cell(boundary_row, 5).value = '%'

for c_idx in range(1, 6):
    apply_saved_style(stats_sheet.cell(boundary_row, c_idx), stats_coverage_style[c_idx-1], tc_fill, section_font)
stats_sheet.row_dimensions[boundary_row].height = 15.0

# Adjust chart series references to point to the new subtotal row index
for chart in stats_sheet._charts:
    for series in chart.series:
        if series.val and series.val.numRef:
            series.val.numRef.f = series.val.numRef.f.replace('$17', f'${subtotal_row}').replace('$16', f'${subtotal_row-1}')
        if hasattr(series, 'xVal') and series.xVal:
            if series.xVal.numRef:
                series.xVal.numRef.f = series.xVal.numRef.f.replace('$17', f'${subtotal_row}').replace('$16', f'${subtotal_row-1}')
            elif series.xVal.strRef:
                series.xVal.strRef.f = series.xVal.strRef.f.replace('$17', f'${subtotal_row}').replace('$16', f'${subtotal_row-1}')

# 7. Save Workbook locally
print("Saving modified Excel file locally...")
wb.save(local_output_path)
print("Finished populating Excel file locally successfully!")

# 8. Copy to User's Downloads directory if present
if os.path.exists(downloads_dir):
    print(f"Syncing Excel file to Downloads folder: {local_output_path} -> {downloads_output_path}")
    shutil.copyfile(local_output_path, downloads_output_path)
    print("Excel file synced successfully!")
