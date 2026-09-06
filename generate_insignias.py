from PIL import Image, ImageDraw, ImageFont
import os

ranks = [
    ("CEL_PM", "CEL", "gold"),
    ("TEN_CEL_PM", "TEN\nCEL", "gold"),
    ("MAJOR_PM", "MAJ", "gold"),
    ("CAPITAO_PM", "CAP", "silver"),
    ("PRIMEIRO_TEN_PM", "1º TEN", "silver"),
    ("PRIMEIRO_TEN_QAPM", "1º TEN\nQAPM", "silver"),
    ("SEGUNDO_TEN_PM", "2º TEN", "silver"),
    ("SEGUNDO_TEN_QAPM", "2º TEN\nQAPM", "silver"),
    ("ASPIRANTE_PM", "ASP", "silver"),
    ("SUBTEN_PM", "SUB\nTEN", "gold"),
    ("PRIMEIRO_SGT_PM", "1º SGT", "silver"),
    ("SEGUNDO_SGT_PM", "2º SGT", "silver"),
    ("TERCEIRO_SGT_PM", "3º SGT", "silver"),
    ("CABO_PM", "CB", "silver"),
    ("SOLDADO_PM", "SD", "silver")
]

dest_dir = 'c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/res/drawable-nodpi'
os.makedirs(dest_dir, exist_ok=True)

width, height = 120, 120

for rank_id, text, color_type in ranks:
    # Create transparent image
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    color = (255, 215, 0, 255) if color_type == "gold" else (192, 192, 192, 255)
    bg_color = (40, 40, 40, 255)
    
    # Draw rounded rectangle (shield/patch)
    draw.rounded_rectangle([10, 10, width-10, height-10], radius=15, fill=bg_color, outline=color, width=4)
    
    # Load a default font (if available) or use basic
    # We will draw the text in the center
    # Pillow default font is very small, we'll try to use a truetype font
    try:
        font = ImageFont.truetype("arialbd.ttf", 24)
    except:
        font = ImageFont.load_default()
    
    # Calculate text bounding box to center it
    lines = text.split('\n')
    y_text = height / 2 - (len(lines) * 28) / 2
    for line in lines:
        left, top, right, bottom = draw.textbbox((0, 0), line, font=font)
        w, h = right - left, bottom - top
        draw.text(((width - w) / 2, y_text), line, font=font, fill=color)
        y_text += 28

    filename = f"insignia_{rank_id.lower()}.png"
    img.save(os.path.join(dest_dir, filename), "PNG")

print("Insignias generated successfully.")
