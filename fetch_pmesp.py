import urllib.request
import re
from bs4 import BeautifulSoup
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

try:
    url = "https://www.policiamilitar.sp.gov.br/institucional/insignias"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    html = urllib.request.urlopen(req, context=ctx).read().decode('utf-8')
    soup = BeautifulSoup(html, 'html.parser')
    images = soup.find_all('img')
    print("Found images on PMESP site:")
    for img in images:
        src = img.get('src')
        if src and ('insignia' in src.lower() or 'posto' in src.lower() or 'grad' in src.lower() or 'png' in src.lower()):
            print(src)
except Exception as e:
    print(f"Failed to fetch: {e}")
