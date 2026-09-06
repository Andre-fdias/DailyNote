from PIL import Image
import os

source_dir = 'C:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/insignias'
dest_dir = 'C:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/res/drawable-nodpi'

mapping = {
    'coronel-pm.jpg': 'insignia_cel_pm.png',
    'tenente-coronel-pm.jpg': 'insignia_ten_cel_pm.png',
    'major-pm.jpg': 'insignia_major_pm.png',
    'capitao-pm.jpg': 'insignia_capitao_pm.png',
    '1-tenente-pm.jpg': 'insignia_primeiro_ten_pm.png',
    '2-tenente-pm.jpg': 'insignia_segundo_ten_pm.png',
    'aspirante-oficial-pm.jpg': 'insignia_aspirante_pm.png',
    'subtenente-pm.jpg': 'insignia_subten_pm.png',
    '1-sargento-pm.jpg': 'insignia_primeiro_sgt_pm.png',
    '2-sargento-pm.jpg': 'insignia_segundo_sgt_pm.png',
    '3-sargento-pm.jpg': 'insignia_terceiro_sgt_pm.png',
    'cabo-pm.jpg': 'insignia_cabo_pm.png',
    'soldado-pm.jpg': 'insignia_soldado_pm.png'
}

for src_name, dest_name in mapping.items():
    src_path = os.path.join(source_dir, src_name)
    if os.path.exists(src_path):
        img = Image.open(src_path).convert('RGBA')
        datas = img.getdata()
        newData = []
        for item in datas:
            # White background removal (threshold 230)
            if item[0] > 230 and item[1] > 230 and item[2] > 230:
                newData.append((255, 255, 255, 0))
            else:
                newData.append(item)
        img.putdata(newData)
        
        dest_path = os.path.join(dest_dir, dest_name)
        img.save(dest_path, 'PNG')
        print(f"Processed {src_name} -> {dest_name}")
    else:
        print(f"File not found: {src_name}")

# Also copy 1-tenente-pm and 2-tenente-pm for QAPM mapping
import shutil
shutil.copy(os.path.join(dest_dir, 'insignia_primeiro_ten_pm.png'), os.path.join(dest_dir, 'insignia_primeiro_ten_qapm.png'))
shutil.copy(os.path.join(dest_dir, 'insignia_segundo_ten_pm.png'), os.path.join(dest_dir, 'insignia_segundo_ten_qapm.png'))
print("Copied QAPM duplicates.")
