import os
import shutil
import re
import tempfile
import sys
import docx
from docx import Document
from docx.oxml.shared import OxmlElement, qn
from docx.opc.constants import RELATIONSHIP_TYPE
from docx.text.paragraph import Paragraph
from docx.shared import Pt

sys.stdout.reconfigure(encoding='utf-8')

def add_hyperlink(paragraph, text, url, color="0000FF", underline=True):
    part = paragraph.part
    r_id = part.relate_to(url, RELATIONSHIP_TYPE.HYPERLINK, is_external=True)

    hyperlink = OxmlElement('w:hyperlink')
    hyperlink.set(qn('r:id'), r_id)

    new_run = OxmlElement('w:r')
    rPr = OxmlElement('w:rPr')

    # Force styling to match caption style (Italic, 10pt, Times New Roman)
    i = OxmlElement('w:i')
    rPr.append(i)
    
    sz = OxmlElement('w:sz')
    sz.set(qn('w:val'), '20') # 10pt in half-points
    rPr.append(sz)
    
    rFonts = OxmlElement('w:rFonts')
    rFonts.set(qn('w:ascii'), 'Times New Roman')
    rFonts.set(qn('w:hAnsi'), 'Times New Roman')
    rPr.append(rFonts)

    if color:
        c = OxmlElement('w:color')
        c.set(qn('w:val'), color)
        rPr.append(c)

    if underline:
        u = OxmlElement('w:u')
        u.set(qn('w:val'), 'single')
        rPr.append(u)

    new_run.append(rPr)
    
    text_node = OxmlElement('w:t')
    text_node.text = text
    new_run.append(text_node)
    
    hyperlink.append(new_run)
    paragraph._p.append(hyperlink)
    return hyperlink

def main():
    docx_path = r""
    
    # Locate image_links.txt relative to the script's directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    links_file_path = os.path.join(script_dir, "image_links.txt")
    
    if not os.path.exists(links_file_path):
        print(f"Error: '{links_file_path}' not found.")
        print("\nPlease create 'image_links.txt' in the same folder as this script, then copy-paste the")
        print("filename and Google Drive links columns from your Google Sheet or Excel. Example format:")
        print("Figure_2_1.png\thttps://drive.google.com/file/d/xxxxxx/view")
        print("Figure_3_1.png\thttps://drive.google.com/file/d/yyyyyy/view")
        return
        
    # Read links map
    links_map = {}
    with open(links_file_path, "r", encoding="utf-8") as f:
        for line in f:
            parts = line.strip().split("\t")
            if len(parts) >= 2:
                filename = parts[0].strip()
                url = parts[1].strip()
                # Parse Figure number out of filename, e.g. "Figure_3_1.png" -> "3_1" -> "3.1"
                match = re.search(r'Figure_(\d+)_(\d+)', filename, re.IGNORECASE)
                if match:
                    fig_key = f"{match.group(1)}.{match.group(2)}"
                    links_map[fig_key] = url

    if not links_map:
        print("Error: Could not parse any valid figure links from 'image_links.txt'.")
        print("Make sure files are named like 'Figure_3_1.png' and the URL is tab-separated.")
        return
        
    print(f"Parsed {len(links_map)} figure links from '{links_file_path}'.")

    # Restore from clean backup if it exists to get a fresh copy without old links
    backup_path = docx_path.replace(".docx", "_Before_Links_Backup.docx")
    if os.path.exists(backup_path):
        print(f"Restoring clean document from backup: '{backup_path}'")
        success = False
        while not success:
            try:
                shutil.copy2(backup_path, docx_path)
                success = True
            except PermissionError:
                print(f"\n[!] WARNING: File '{docx_path}' is open in Microsoft Word.")
                print("Please CLOSE Microsoft Word and press ENTER to retry (or type 'q' and press ENTER to quit)...")
                choice = input().strip().lower()
                if choice == 'q':
                    print("Operation cancelled.")
                    return
    else:
        # Create backup copy if it doesn't exist
        shutil.copy2(docx_path, backup_path)
        print(f"Backup copy created at: '{backup_path}'")

    # Copy to temp file to bypass MS Word locks
    temp_dir = tempfile.gettempdir()
    temp_docx_path = os.path.join(temp_dir, "temp_report_links.docx")
    shutil.copy2(docx_path, temp_docx_path)

    doc = Document(temp_docx_path)
    
    links_added = 0
    print("\nScanning document for figure captions...")
    for p in doc.paragraphs:
        text = p.text.strip()
        style = p.style.name
        
        if (style == "Caption" or "caption" in style.lower()) and (text.startswith("Figure ") or text.startswith("Hình ")):
            match = re.match(r'^(?:Figure|Hình)\s+(\d+\.\d+)[:\s]*', text, re.IGNORECASE)
            if match:
                fig_num = match.group(1)
                if fig_num in links_map:
                    # Check if hyperlink already exists
                    has_hyperlink = False
                    for child in p._p:
                        if child.tag.endswith('hyperlink'):
                            has_hyperlink = True
                            break
                            
                    if not has_hyperlink:
                        # Create a separate paragraph for the hyperlink (Style: Normal)
                        # so that it is excluded from the Table of Figures index in Word
                        p_link = doc.add_paragraph()
                        try:
                            p_link.style = 'Normal'
                        except:
                            pass
                        p_link.alignment = p.alignment
                        p_link.paragraph_format.space_before = Pt(0)
                        
                        original_after = p.paragraph_format.space_after
                        p_link.paragraph_format.space_after = original_after if original_after else Pt(6)
                        
                        # Keep it tight under the caption
                        p.paragraph_format.space_after = Pt(2)
                        
                        # Add the hyperlink
                        add_hyperlink(p_link, "Image Detail", links_map[fig_num])
                        
                        # Move it in the XML tree to be immediately after the caption
                        p._element.addnext(p_link._element)
                        
                        print(f"Added link to Figure {fig_num}: {links_map[fig_num]}")
                        links_added += 1
                    else:
                        print(f"Figure {fig_num} already has a hyperlink. Skipping.")

    doc.save(temp_docx_path)

    # Copy back with retry loop
    success = False
    while not success:
        try:
            shutil.copy2(temp_docx_path, docx_path)
            success = True
        except PermissionError:
            print(f"\n[!] WARNING: File '{docx_path}' is open in Microsoft Word.")
            print("Please CLOSE Microsoft Word and press ENTER to retry (or type 'q' and press ENTER to quit)...")
            choice = input().strip().lower()
            if choice == 'q':
                print("Operation cancelled. File was not saved.")
                break

    try:
        os.remove(temp_docx_path)
    except:
        pass
        
    if success:
        print("\n" + "="*50)
        print("SUCCESSFULLY APPENDED HYPERLINKS TO FIGURE CAPTIONS!")
        print("="*50)
        print(f"Total links added: {links_added}")
        print(f"Updated document saved to: '{docx_path}'")
        print("="*50 + "\n")

if __name__ == "__main__":
    main()
