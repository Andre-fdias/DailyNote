from PIL import Image, ImageDraw, ImageFont
import os

img_path = 'C:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/insignias.png'
dest_dir_app = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/res/drawable-nodpi'
dest_dir_artifact = 'C:/Users/andre_we17otv/.gemini/antigravity-ide/brain/43df0005-dfe0-40f6-bdaf-e1714af7e865'

os.makedirs(dest_dir_app, exist_ok=True)

# Format: (id, x1, y1, x2, y2)
boxes = [
    ("cel", 40, 215, 160, 255),
    ("ten_cel", 40, 260, 160, 305),
    ("major", 40, 305, 160, 345),
    ("capitao", 40, 345, 160, 390),
    ("primeiro_ten", 40, 390, 130, 430),
    ("segundo_ten", 40, 435, 90, 470),
    ("aspirante", 40, 480, 90, 520),
    
    ("subten", 270, 90, 330, 135),
    ("primeiro_sgt", 280, 150, 325, 205),
    ("segundo_sgt", 280, 210, 325, 265),
    ("terceiro_sgt", 280, 275, 325, 330),
    ("cabo", 280, 400, 325, 450),
    ("soldado", 280, 460, 325, 510)
]

img = Image.open(img_path).convert('RGBA')

markdown_content = "# Preview das Insígnias Recortadas\n\nVerifique se os recortes estão corretos:\n\n"

for name, x1, y1, x2, y2 in boxes:
    cropped = img.crop((x1, y1, x2, y2))
    
    # Remove white background
    datas = cropped.getdata()
    newData = []
    for item in datas:
        if item[0] > 230 and item[1] > 230 and item[2] > 230:
            newData.append((255, 255, 255, 0))
        else:
            newData.append(item)
    cropped.putdata(newData)
    
    filename = f"insignia_{name}_pm.png"
    path_app = os.path.join(dest_dir_app, filename)
    path_art = os.path.join(dest_dir_artifact, filename)
    
    cropped.save(path_app, "PNG")
    cropped.save(path_art, "PNG")
    
    markdown_content += f"### {name.upper()}\n![{name}]({path_art})\n\n"
    
# Copy for duplicate names (qapm)
import shutil
shutil.copy(os.path.join(dest_dir_app, "insignia_primeiro_ten_pm.png"), os.path.join(dest_dir_app, "insignia_primeiro_ten_qapm.png"))
shutil.copy(os.path.join(dest_dir_app, "insignia_segundo_ten_pm.png"), os.path.join(dest_dir_app, "insignia_segundo_ten_qapm.png"))

with open(os.path.join(dest_dir_artifact, 'preview_insignias.md'), 'w', encoding='utf-8') as f:
    f.write(markdown_content)

print("Cropping completed!")
