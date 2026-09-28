import os
import sys
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls, qn

if hasattr(sys.stdout, 'reconfigure'):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

# Standard Document Border Color: Light Mint / Teal Green (84E291)
BORDER_COLOR = "84E291"
BROKEN_STYLES = {"BangLi1Nhat-Nhnmanh6", "BangLi1Nhat-Nhnmanh3", "LiBang"}

def apply_standard_borders(table):
    """Applies the document's standard 84E291 light green border to table."""
    tblPr = table._tbl.tblPr
    for b in tblPr.xpath('w:tblBorders'):
        tblPr.remove(b)
    
    tblBorders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        f'<w:top w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'<w:left w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'<w:right w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'<w:insideH w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'<w:insideV w:val="single" w:sz="4" w:space="0" w:color="{BORDER_COLOR}"/>'
        f'</w:tblBorders>'
    )
    tblPr.append(tblBorders)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    """Sets cell padding (margins) in dxa."""
    tcPr = cell._tc.get_or_add_tcPr()
    for mar in tcPr.xpath('w:tcMar'):
        tcPr.remove(mar)
    tcPr.append(parse_xml(
        f'<w:tcMar {nsdecls("w")}>'
        f'<w:top w:w="{top}" w:type="dxa"/>'
        f'<w:bottom w:w="{bottom}" w:type="dxa"/>'
        f'<w:left w:w="{left}" w:type="dxa"/>'
        f'<w:right w:w="{right}" w:type="dxa"/>'
        f'</w:tcMar>'
    ))

def ensure_row_props(row, is_header=False):
    """Ensures row pagination properties: cantSplit and tblHeader."""
    trPr = row._tr.get_or_add_trPr()
    if not trPr.xpath('w:cantSplit'):
        trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
    if is_header and not trPr.xpath('w:tblHeader'):
        trPr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))

def rebuild_broken_uc_table(doc, table):
    """
    Rebuilds a 2-column broken Use Case table into a standard 4-column UC Specification table.
    """
    kv_data = {}
    for row in table.rows:
        if len(row.cells) >= 2:
            key = row.cells[0].text.strip()
            val = row.cells[1].text.strip()
            if key:
                kv_data[key] = val
                
    uc_fields = [
        ('UC ID and Name', kv_data.get('UC ID and Name', ''), True),
        ('Created By', kv_data.get('Created By', ''), 'Date Created', kv_data.get('Date Created', '')),
        ('Primary Actor', kv_data.get('Primary Actor', ''), 'Secondary Actors', kv_data.get('Secondary Actors', '')),
        ('Trigger', kv_data.get('Trigger', ''), True),
        ('Description', kv_data.get('Description', ''), True),
        ('Preconditions', kv_data.get('Preconditions', ''), True),
        ('Postconditions', kv_data.get('Postconditions', ''), True),
        ('Normal Flow', kv_data.get('Normal Flow', ''), True),
        ('Alternative Flows', kv_data.get('Alternative Flows', ''), True),
        ('Exceptions', kv_data.get('Exceptions', ''), True),
        ('Priority', kv_data.get('Priority', ''), 'Frequency of Use', kv_data.get('Frequency of Use', '')),
        ('Business Rules', kv_data.get('Business Rules', ''), True),
        ('Other Information', kv_data.get('Other Information', ''), True),
        ('Assumptions', kv_data.get('Assumptions', ''), True)
    ]
    
    parent = table._tbl.getparent()
    tbl_elem = table._tbl
    
    new_table = doc.add_table(rows=0, cols=4)
    apply_standard_borders(new_table)
    new_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    
    for r_idx, field in enumerate(uc_fields):
        row = new_table.add_row()
        ensure_row_props(row, is_header=(r_idx == 0))
        
        if len(field) == 3 and field[2] is True:
            cell_k = row.cells[0]
            cell_v = row.cells[1]
            cell_k.text = field[0]
            cell_v.text = field[1]
            cell_v.merge(row.cells[2])
            cell_v.merge(row.cells[3])
            
            set_cell_margins(cell_k)
            set_cell_margins(cell_v)
            
            if cell_k.paragraphs[0].runs:
                r = cell_k.paragraphs[0].runs[0]
                r.bold = True
                r.font.name = 'Times New Roman'
                r.font.size = Pt(10)
            if cell_v.paragraphs[0].runs:
                for r in cell_v.paragraphs[0].runs:
                    r.font.name = 'Times New Roman'
                    r.font.size = Pt(10)
        else:
            k1, v1, k2, v2 = field
            c0, c1, c2, c3 = row.cells[0], row.cells[1], row.cells[2], row.cells[3]
            c0.text = k1
            c1.text = v1
            c2.text = k2
            c3.text = v2
            
            for c in (c0, c1, c2, c3):
                set_cell_margins(c)
                
            if c0.paragraphs[0].runs:
                c0.paragraphs[0].runs[0].bold = True
                c0.paragraphs[0].runs[0].font.name = 'Times New Roman'
                c0.paragraphs[0].runs[0].font.size = Pt(10)
            if c2.paragraphs[0].runs:
                c2.paragraphs[0].runs[0].bold = True
                c2.paragraphs[0].runs[0].font.name = 'Times New Roman'
                c2.paragraphs[0].runs[0].font.size = Pt(10)
            for c in (c1, c3):
                if c.paragraphs[0].runs:
                    for r in c.paragraphs[0].runs:
                        r.font.name = 'Times New Roman'
                        r.font.size = Pt(10)

    parent.insert(parent.index(tbl_elem), new_table._tbl)
    parent.remove(tbl_elem)
    return new_table

def synchronize_table(doc, table, idx):
    """
    Inspects table properties and synchronizes only tables with design flaws.
    """
    tblPr = table._tbl.tblPr
    borders = tblPr.xpath('w:tblBorders/*')
    border_colors = set(b.get('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}color') for b in borders)
    
    styles = tblPr.xpath('w:tblStyle/@w:val')
    style_name = styles[0] if styles else ""
    
    r0_text = table.rows[0].cells[0].text.strip() if len(table.rows) > 0 and len(table.columns) > 0 else ""
    
    is_uc_table = ("UC ID and Name" in r0_text or r0_text.startswith("UC "))
    is_broken_uc = is_uc_table and (len(table.columns) != 4 or style_name in BROKEN_STYLES)
    is_missing_borders = (not border_colors or border_colors != {BORDER_COLOR})
    has_broken_style = style_name in BROKEN_STYLES
    
    needs_sync = is_broken_uc or is_missing_borders or has_broken_style
    
    if not needs_sync:
        return False
        
    print(f"  -> Synchronizing Table {idx+1:3d} (First cell: '{r0_text[:25]}')")
          
    if is_broken_uc:
        rebuild_broken_uc_table(doc, table)
    else:
        apply_standard_borders(table)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        
        tblStyle_nodes = tblPr.xpath('w:tblStyle')
        for s in tblStyle_nodes:
            s.set(qn('w:val'), 'TableGrid')
            
        for r_idx, row in enumerate(table.rows):
            ensure_row_props(row, is_header=(r_idx == 0))
            for cell in row.cells:
                set_cell_margins(cell)
                for p in cell.paragraphs:
                    for r in p.runs:
                        if not r.font.name:
                            r.font.name = 'Times New Roman'
                            
    return True

def format_docx(input_path, output_path):
    print(f"Loading document: {input_path}")
    doc = docx.Document(input_path)
    
    total_tables = len(doc.tables)
    print(f"Preserving original docx styles while synchronizing flawed tables across {total_tables} total tables...")
    
    sync_count = 0
    for idx in range(total_tables - 1, -1, -1):
        if synchronize_table(doc, doc.tables[idx], idx):
            sync_count += 1
            
    print(f"Successfully synchronized {sync_count} tables with design flaws.")
    print(f"Saving formatted document to: {output_path}")
    doc.save(output_path)
    print("Done!")

if __name__ == '__main__':
    src = r"C:\Users\phuoc\Downloads\Report 3_v7 - Copy.docx"
    dst = r"C:\Users\phuoc\Downloads\Report 3_v7_Formatted.docx"
    if len(sys.argv) > 1:
        src = sys.argv[1]
    if len(sys.argv) > 2:
        dst = sys.argv[2]
    format_docx(src, dst)

