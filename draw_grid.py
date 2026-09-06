from PIL import Image, ImageDraw
import os

img_path = 'C:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/insignias.png'
dest_path = 'C:/Users/andre_we17otv/.gemini/antigravity-ide/brain/43df0005-dfe0-40f6-bdaf-e1714af7e865/insignias_grid.png'

try:
    img = Image.open(img_path).convert('RGB')
    draw = ImageDraw.Draw(img)
    width, height = img.size
    
    # Draw grid
    step = 50
    for x in range(0, width, step):
        draw.line([(x, 0), (x, height)], fill=(255, 0, 0), width=1)
        if x % 100 == 0:
            draw.text((x+2, 2), str(x), fill=(255, 0, 0))
            
    for y in range(0, height, step):
        draw.line([(0, y), (width, y)], fill=(255, 0, 0), width=1)
        if y % 100 == 0:
            draw.text((2, y+2), str(y), fill=(255, 0, 0))

    img.save(dest_path)
    print(f"Grid image saved to {dest_path} with size {width}x{height}")
except Exception as e:
    print(f"Error: {e}")
