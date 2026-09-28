import os
import shutil
import re
import argparse
from docx import Document
from docx.oxml.table import CT_Tbl
from docx.oxml.text.paragraph import CT_P
from docx.text.paragraph import Paragraph

def get_chapter(para, hierarchy):
    text = para.text.strip()
    match = re.match(r'^(\d+)(?:\.[\d]+)*', text)
    if match:
        return int(match.group(1))
    
    for lvl in sorted(hierarchy.keys(), reverse=True):
        p = hierarchy[lvl]
        h_text = p.text.strip()
        h_match = re.match(r'^(\d+)(?:\.[\d]+)*', h_text)
        if h_match:
            return int(h_match.group(1))
            
    for lvl in sorted(hierarchy.keys(), reverse=True):
        p = hierarchy[lvl]
        h_text = p.text.strip().lower()
        if "record of changes" in h_text or "definition" in h_text or "acronym" in h_text:
            return 1
        if "system design" in h_text or "database design" in h_text or "software design document" in h_text:
            return 2
        if "detailed design" in h_text:
            return 3
            
    return 1 # default fallback

def get_clean_heading_text(heading_text):
    cleaned = re.sub(r'^[\d\.]+\s*', '', heading_text).strip()
    cleaned = re.sub(r'[:\.\-–—\s]+$', '', cleaned).strip()
    return cleaned

def is_existing_caption(text, style_name):
    if style_name.startswith("Heading"):
        return False
    if "caption" in style_name.lower():
        return True
    pattern = r'^[\s\-–—]*(?:table|figure|bảng|hình)\s*\d+[-.]\d+'
    if re.match(pattern, text, flags=re.IGNORECASE):
        return True
    return False

def clean_existing_caption(text):
    pattern = r'^[\s\-–—]*(?:table|figure|bảng|hình)\s*\d+[-.]\d+[:\s\-–—]*'
    cleaned = re.sub(pattern, '', text, flags=re.IGNORECASE).strip()
    cleaned = re.sub(r'^[:\s\-–—\.]+', '', cleaned).strip()
    return cleaned

def get_parent_descriptive_title(current_level, hierarchy):
    for lvl in range(current_level - 1, 0, -1):
        if lvl in hierarchy:
            text = hierarchy[lvl].text.strip()
            clean = get_clean_heading_text(text)
            if clean and clean.lower() not in ["class diagram", "sequence diagram", "state diagram", "state machine", "diagram", "detailed design"]:
                return clean
    return None

def update_caption_paragraph(para, label, number, title, style_name='Caption'):
    para.text = ""
    try:
        para.style = style_name
    except KeyError:
        pass
    
    r1 = para.add_run(f"{label} {number}: ")
    r1.bold = True
    
    r2 = para.add_run(title)
    r2.bold = False

def create_caption_paragraph(doc, label, number, title, style_name='Caption'):
    p = doc.add_paragraph()
    try:
        p.style = style_name
    except KeyError:
        pass
        
    r1 = p.add_run(f"{label} {number}: ")
    r1.bold = True
    
    r2 = p.add_run(title)
    r2.bold = False
    return p

def main():
    default_docx_path = r""
    
    parser = argparse.ArgumentParser(description="Auto-number tables and figures in DOCX with captions below tables.")
    parser.add_argument("--path", default=default_docx_path, help="Absolute path to the Word (.docx) document")
    parser.add_argument("--lang", default="en", choices=["en", "vi"], help="Language: 'en' or 'vi'")
    args = parser.parse_args()
    
    docx_path = args.path
    backup_path = docx_path.replace(".docx", "_Backup.docx")
    
    # Restore from clean backup
    if os.path.exists(backup_path):
        print(f"Restoring clean document from backup: '{backup_path}'")
        shutil.copy2(backup_path, docx_path)
    else:
        if os.path.exists(docx_path):
            shutil.copy2(docx_path, backup_path)
            print(f"Backup copy created at: '{backup_path}'")
        else:
            print(f"Error: File not found at '{docx_path}'")
            return
            
    # Load Document
    doc = Document(docx_path)
    body_elements = doc.element.body
    
    label_table = "Table" if args.lang == "en" else "Bảng"
    label_figure = "Figure" if args.lang == "en" else "Hình"
    
    heading_hierarchy = {}
    chapter_tables = {}
    chapter_figures = {}
    
    elements_list = list(body_elements)
    actions = []
    
    print("\nScanning document structure and planning captions...")
    
    for idx, child in enumerate(elements_list):
        if isinstance(child, CT_P):
            para = Paragraph(child, doc)
            text = para.text.strip()
            style = para.style.name
            
            if style.startswith("Heading"):
                try:
                    level = int(style.split()[-1]) if style.split()[-1].isdigit() else 1
                except:
                    level = 1
                heading_hierarchy[level] = para
                for lvl in list(heading_hierarchy.keys()):
                    if lvl > level:
                        del heading_hierarchy[lvl]
                        
        elif isinstance(child, CT_Tbl):
            nearest_heading = None
            current_level = 1
            for lvl in sorted(heading_hierarchy.keys(), reverse=True):
                nearest_heading = heading_hierarchy[lvl]
                current_level = lvl
                break
                
            chapter = get_chapter(nearest_heading, heading_hierarchy) if nearest_heading else 1
            chapter_tables[chapter] = chapter_tables.get(chapter, 0) + 1
            table_num = f"{chapter}.{chapter_tables[chapter]}"
            
            title = nearest_heading.text.strip() if nearest_heading else "Untitled Table"
            title = get_clean_heading_text(title)
            
            # 1. Check preceding paragraph for short title label (we want to clean it up)
            prev_idx = idx - 1
            if prev_idx >= 0 and isinstance(elements_list[prev_idx], CT_P):
                prev_para_obj = Paragraph(elements_list[prev_idx], doc)
                prev_text = prev_para_obj.text.strip()
                prev_style = prev_para_obj.style.name
                
                # If it's a short label above the table, use its text as title and remove it
                if prev_text and not prev_style.startswith("Heading") and not is_existing_caption(prev_text, prev_style) and len(prev_text) < 100:
                    title = prev_text
                    actions.append({
                        "action": "clear",
                        "para": prev_para_obj
                    })
            
            # Resolve generic title names
            if title.lower() in ["table", "tables", "bảng"]:
                parent_title = get_parent_descriptive_title(current_level, heading_hierarchy)
                if parent_title:
                    title = f"{title} for {parent_title}"
            
            # 2. Check following paragraph for placing the caption (below the table)
            next_idx = idx + 1
            next_para = None
            if next_idx < len(elements_list) and isinstance(elements_list[next_idx], CT_P):
                next_para_obj = Paragraph(elements_list[next_idx], doc)
                next_text = next_para_obj.text.strip()
                next_style = next_para_obj.style.name
                
                # If the paragraph after the table is an existing caption or is empty, we update it in-place
                if not next_style.startswith("Heading") and (is_existing_caption(next_text, next_style) or not next_text):
                    if is_existing_caption(next_text, next_style):
                        title = clean_existing_caption(next_text)
                    next_para = next_para_obj
            
            if next_para:
                actions.append({
                    "action": "update",
                    "para": next_para,
                    "title": title,
                    "num": table_num,
                    "type": "Table"
                })
            else:
                actions.append({
                    "action": "insert_after",
                    "target": child,
                    "title": title,
                    "num": table_num,
                    "type": "Table"
                })
                
        # Check if Figure
        if isinstance(child, CT_P):
            has_drawing = bool(child.xpath('.//w:drawing') or child.xpath('.//w:pict'))
            if has_drawing:
                nearest_heading = None
                current_level = 1
                for lvl in sorted(heading_hierarchy.keys(), reverse=True):
                    nearest_heading = heading_hierarchy[lvl]
                    current_level = lvl
                    break
                    
                chapter = get_chapter(nearest_heading, heading_hierarchy) if nearest_heading else 1
                chapter_figures[chapter] = chapter_figures.get(chapter, 0) + 1
                fig_num = f"{chapter}.{chapter_figures[chapter]}"
                
                title = nearest_heading.text.strip() if nearest_heading else "Untitled Figure"
                title = get_clean_heading_text(title)
                title_source = "heading"
                
                next_idx = idx + 1
                next_para = None
                if next_idx < len(elements_list) and isinstance(elements_list[next_idx], CT_P):
                    next_para_obj = Paragraph(elements_list[next_idx], doc)
                    next_text = next_para_obj.text.strip()
                    next_style = next_para_obj.style.name
                    
                    if next_text and is_existing_caption(next_text, next_style):
                        title = clean_existing_caption(next_text)
                        next_para = next_para_obj
                        title_source = "existing_caption"
                
                if title_source == "heading" and title.lower() in ["class diagram", "sequence diagram", "state diagram", "state machine", "diagram"]:
                    found_prev = False
                    for offset in range(1, 4):
                        prev_idx = idx - offset
                        if prev_idx >= 0 and isinstance(elements_list[prev_idx], CT_P):
                            prev_para_obj = Paragraph(elements_list[prev_idx], doc)
                            prev_text = prev_para_obj.text.strip()
                            prev_style = prev_para_obj.style.name
                            
                            if prev_text and len(prev_text) < 120 and not prev_style.startswith("Heading"):
                                if re.match(r'^[\d\.]+', prev_text) or "diagram" in prev_text.lower():
                                    title = re.sub(r'^[\d\.]+\s*', '', prev_text).strip()
                                    found_prev = True
                                    break
                    if not found_prev:
                        parent_title = get_parent_descriptive_title(current_level, heading_hierarchy)
                        if parent_title:
                            title = f"{title} for {parent_title}"
                
                if next_para:
                    actions.append({
                        "action": "update",
                        "para": next_para,
                        "title": title,
                        "num": fig_num,
                        "type": "Figure"
                    })
                else:
                    actions.append({
                        "action": "insert_after",
                        "target": child,
                        "title": title,
                        "num": fig_num,
                        "type": "Figure"
                    })

    # Ensure 'Table Caption' style exists, inheriting from 'Caption' formatting
    try:
        table_style = doc.styles['Table Caption']
    except KeyError:
        from docx.enum.style import WD_STYLE_TYPE
        table_style = doc.styles.add_style('Table Caption', WD_STYLE_TYPE.PARAGRAPH)
        if 'Caption' in doc.styles:
            caption_style = doc.styles['Caption']
            table_style.font.name = caption_style.font.name
            table_style.font.size = caption_style.font.size
            table_style.font.bold = caption_style.font.bold
            table_style.font.italic = caption_style.font.italic
            table_style.font.color.rgb = caption_style.font.color.rgb
            table_style.paragraph_format.alignment = caption_style.paragraph_format.alignment

    print(f"Applying {len(actions)} caption numbering actions in-place...")
    
    tables_updated = 0
    tables_inserted = 0
    figures_updated = 0
    figures_inserted = 0
    cleared_paragraphs = 0
    
    for act in actions:
        if act["action"] == "clear":
            act["para"].text = ""
            cleared_paragraphs += 1
            continue
            
        style_to_use = 'Table Caption' if act.get("type") == "Table" else 'Caption'
        
        if act["action"] == "update":
            update_caption_paragraph(act["para"], label_table if act["type"] == "Table" else label_figure, act["num"], act["title"], style_name=style_to_use)
            if act["type"] == "Table":
                tables_updated += 1
            else:
                figures_updated += 1
        elif act["action"] == "insert_after":
            new_p = create_caption_paragraph(doc, label_table if act["type"] == "Table" else label_figure, act["num"], act["title"], style_name=style_to_use)
            act["target"].addnext(new_p._element)
            if act["type"] == "Table":
                tables_inserted += 1
            else:
                figures_inserted += 1
            
    doc.save(docx_path)
    
    print("\n" + "="*50)
    print("SUCCESSFULLY COMPLETED AUTO-NUMBERING (CAPTIONS BELOW TABLES)!")
    print("="*50)
    print(f"Source Document: {docx_path}")
    print(f"Backup Document: {backup_path}")
    print(f"Tables Processed: {tables_updated} updated, {tables_inserted} inserted.")
    print(f"Figures Processed: {figures_updated} updated, {figures_inserted} inserted.")
    print(f"Cleared Preceding Labels: {cleared_paragraphs}")
    print("="*50 + "\n")

if __name__ == "__main__":
    main()
