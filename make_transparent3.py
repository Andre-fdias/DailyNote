from PIL import Image
import os

images = [
    'viatura_telegrafia_white_1787935729992.png'
]

targets = [
    'viatura_telegrafia.png'
]

workspace_dir = 'C:/Users/andre_we17otv/.gemini/antigravity-ide/brain/43df0005-dfe0-40f6-bdaf-e1714af7e865'
dest_dir = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/res/drawable-nodpi'

for i, img_name in enumerate(images):
    src = os.path.join(workspace_dir, img_name)
    dst = os.path.join(dest_dir, targets[i])
    
    img = Image.open(src).convert("RGBA")
    datas = img.getdata()
    
    newData = []
    for item in datas:
        # If pixel is close to white (background), make it transparent
        if item[0] > 230 and item[1] > 230 and item[2] > 230:
            newData.append((255, 255, 255, 0))
        else:
            newData.append(item)
            
    img.putdata(newData)
    img.save(dst, "PNG")

