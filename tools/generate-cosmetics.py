"""Reproducible original pixel-art textures. Requires Pillow; no downloaded art."""
from pathlib import Path
from PIL import Image, ImageDraw
import math
root=Path(__file__).resolve().parents[1]/'client-mod/src/main/resources/assets/tarclient/textures/cosmetics'
root.mkdir(parents=True,exist_ok=True)
Image.new('RGBA',(1,1),'white').save(root/'white.png')
for kind in ['ocean','lightning','blossom','nether','basic']:
    for frame in range(16):
        art=Image.new('RGB',(10,16));d=ImageDraw.Draw(art)
        for y in range(16):
            for x in range(10):
                if kind=='ocean':color=(15,75+y*3,135+y*4)
                elif kind=='lightning':color=(232+y,232+y,236+y)
                elif kind=='blossom':color=(45+y*3,31+y*2,67+y*3)
                elif kind=='nether':
                    k=int(30*(1+math.sin(x*.9+y*.6+frame*math.pi/8)));color=(55+k,12,90+k*2)
                else:color=(79+y*3,157+y*3,213+y*2)
                d.point((x,y),fill=color)
        if kind=='ocean':
            d.polygon([(0,14),(2,13),(4,14),(7,12),(9,14),(9,15),(0,15)],fill='#B6AA81')
            d.rectangle((6,12,8,14),fill='#537582');d.line([(1,14),(1,10),(2,9)],fill='#65AF82');d.line([(4,15),(4,12)],fill='#5CBA91')
            for offset,y,color in [(0,4,'#FFD074'),(5,8,'#F6959B')]:
                x=(frame//2+offset)%13-2;d.rectangle((x,y,x+1,y+1),fill=color);d.point((x-1,y+1),fill=color);d.point((x+1,y),fill='#273644')
        elif kind=='lightning':
            x=3+frame//4%3;d.line([(x,1),(x-2,5),(x+1,5),(x-1,10),(x+2,9),(x,15)],fill='#20232B',width=1)
            if frame%4<2:d.line([(8,0),(6,4),(8,6),(6,10)],fill='#65778B')
        elif kind=='blossom':
            d.line([(5,15),(5,8),(4,5)],fill='#927464',width=2);d.line([(5,9),(8,6)],fill='#927464');d.line([(5,7),(2,5)],fill='#927464')
            for x,y in [(2,4),(4,3),(6,3),(8,5),(4,6)]:d.ellipse((x-2,y-1,x+1,y+1),fill='#F0ADC9')
            d.point((7,7),fill='#FFE3D2');d.point((8,7 if frame%8<4 else 8),fill='#CC829C');d.point((7,6),fill='#353647')
            d.point(((frame//3+1)%10,8+frame%7),fill='#FFCCDF')
        elif kind=='nether':
            d.rectangle((0,0,9,15),outline='#291733');d.rectangle((1,1,8,14),outline='#553574')
            for i in range(4):d.point(((frame+i*3)%8+1,(frame*2+i*4)%14+1),fill='#D992FF')
        else:
            # Tiny Tar mark, drawn from its three-pronged geometric design.
            d.polygon([(1,5),(3,5),(5,7),(7,5),(9,5),(5,9)],fill='white')
            d.polygon([(1,7),(4,9),(4,13),(3,11),(3,9)],fill='white')
            d.polygon([(9,7),(6,9),(6,13),(7,11),(7,9)],fill='white')
        texture=Image.new('RGB',(64,32),art.getpixel((0,0)))
        texture.paste(art,(1,1));texture.paste(art.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(12,1))
        texture.save(root/f'{kind}cape_{frame}.png')
