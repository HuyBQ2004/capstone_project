import os
import shutil
import re
import tempfile
import sys
import docx
from docx import Document
from docx.shared import Inches, Pt
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

sys.stdout.reconfigure(encoding='utf-8')

def main():
    default_docx_path = r""
    
    import argparse
    parser = argparse.ArgumentParser(description="Auto-fitting tables and figures in DOCX.")
    parser.add_argument("--path", default=default_docx_path, help="Absolute path to the Word (.docx) document")
    args = parser.parse_args()
    
    docx_path = args.path
    backup_path = docx_path.replace(".docx", "_Before_Autofit_Backup.docx")
    
    if not os.path.exists(docx_path):
        print(f"Error: File not found at '{docx_path}'")
        return
        
    # Auto Backup
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
        shutil.copy2(docx_path, backup_path)
        print(f"Backup copy created at: '{backup_path}'")

    # Copy to temp to bypass MS Word lock
    temp_dir = tempfile.gettempdir()
    temp_docx_path = os.path.join(temp_dir, "temp_autofit_run.docx")
    shutil.copy2(docx_path, temp_docx_path)

    doc = Document(temp_docx_path)
    
    # Define safe printable width (6.0 inches = 5943600 EMUs)
    # This leaves a nice 0.25-inch padding on both sides within the 6.5-inch printable area
    max_width_emus = int(6.0 * 914400) 
    
    print("\nProcessing Tables...")
    tables_processed = 0
    for table in doc.tables:
        table.allow_autofit = True
        tblPr = table._tbl.tblPr
        
        # Set table width to 100% of the margins
        tblW = tblPr.find(qn('w:tblW'))
        if tblW is None:
            tblW = OxmlElement('w:tblW')
            tblPr.append(tblW)
        tblW.set(qn('w:w'), '5000') # 100% width
        tblW.set(qn('w:type'), 'pct') # percentage representation
        
        # Remove left indentation on the table itself
        tblInd = tblPr.find(qn('w:tblInd'))
        if tblInd is not None:
            tblPr.remove(tblInd)
            
        tables_processed += 1
        
    print(f"Set {tables_processed} tables to AutoFit to Window and cleared table indents.")

    print("\nProcessing Figures and Captions...")
    images_scaled = 0
    paragraphs_aligned = 0
    
    for p in doc.paragraphs:
        # Check if paragraph contains drawings (figures/images)
        has_drawing = bool(p._element.xpath('.//w:drawing') or p._element.xpath('.//w:pict'))
        
        # Check if paragraph is Caption, Table Caption, or Figure Link (style Normal and text starts with Image Detail)
        is_caption_or_link = p.style.name in ["Caption", "Table Caption"] or (p.style.name == "Normal" and p.text.strip().startswith("Image Detail"))
        
        if has_drawing or is_caption_or_link:
            # Center align and clear indentations to prevent sliding off the page
            p.alignment = docx.enum.text.WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.left_indent = Inches(0)
            p.paragraph_format.right_indent = Inches(0)
            p.paragraph_format.first_line_indent = Inches(0)
            paragraphs_aligned += 1
            
        if has_drawing:
            # Find and scale any inline shapes inside the paragraph
            for inline_shape in p._element.xpath('.//wp:inline'):
                extent = inline_shape.find(qn('wp:extent'))
                if extent is not None:
                    cx = int(extent.get('cx'))
                    cy = int(extent.get('cy'))
                    if cx > max_width_emus:
                        ratio = cy / cx
                        new_cx = max_width_emus
                        new_cy = int(max_width_emus * ratio)
                        extent.set('cx', str(new_cx))
                        extent.set('cy', str(new_cy))
                        
                        # Apply to sub-elements representing graphic dimensions
                        for pic in inline_shape.xpath('.//pic:spPr/a:xfrm/a:ext'):
                            pic.set('cx', str(new_cx))
                            pic.set('cy', str(new_cy))
                        images_scaled += 1
                        
    print(f"Centered and cleared indentations for {paragraphs_aligned} paragraphs.")
    print(f"Scaled {images_scaled} wide images down to 6.0 inches to fit margins.")

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
                print("Operation cancelled. Changes were not saved.")
                break
                
    try:
        os.remove(temp_docx_path)
    except:
        pass

    if success:
        print("\n" + "="*50)
        print("SUCCESSFULLY COMPLETED AUTO-FITTING TABLES AND FIGURES!")
        print("="*50)
        print(f"Source Document: {docx_path}")
        print(f"Backup Document: {backup_path}")
        print(f"Tables Autofitted: {tables_processed}")
        print(f"Images Scaled: {images_scaled}")
        print(f"Paragraphs Centered: {paragraphs_aligned}")
        print("="*50 + "\n")

if __name__ == "__main__":
    main()
