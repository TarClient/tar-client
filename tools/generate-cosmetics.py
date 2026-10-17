"""Original illustrated cosmetics. Pillow only; deterministic, seamless 32-frame loops.

Cape UVs remain vanilla 64x32, sampled at 8x resolution (80x128 per face).
Artwork is drawn at 2x that resolution and filtered down, not enlarged pixel art.
Each articulated wing panel uses a two-sided 8x32 atlas at 16x resolution.
Run with --preview <directory> to also export a contact sheet and animated previews.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageChops
import argparse
import math
import random

ROOT = Path(__file__).resolve().parents[1] / 'client-mod/src/main/resources/assets/tarclient/textures/cosmetics'
FRAMES = 32
TAU = math.tau
W, H = 160, 256


def mix(a, b, t):
    return tuple(round(x + (y-x)*max(0, min(1, t))) for x, y in zip(a, b))


def curve(points, steps=24):
    a,b,c,d=points
    return [tuple((1-t)**3*a[k]+3*(1-t)**2*t*b[k]+3*(1-t)*t*t*c[k]+t**3*d[k] for k in (0,1)) for t in (i/steps for i in range(steps+1))]


def glow(image, paint, radius=5):
    layer=Image.new('RGBA', image.size)
    paint(ImageDraw.Draw(layer))
    image.alpha_composite(layer.filter(ImageFilter.GaussianBlur(radius)))
    image.alpha_composite(layer)


def background(top, bottom):
    image=Image.new('RGBA',(W,H))
    draw=ImageDraw.Draw(image)
    for y in range(H):
        draw.line((0,y,W,y),fill=mix(top,bottom,y/(H-1))+(255,))
    return image


def border(image, color):
    d=ImageDraw.Draw(image)
    d.rounded_rectangle((3,3,156,252),radius=4,outline=color,width=2)
    for y in range(9,247,7):
        d.line((7,y,7,y+2),fill=color)
        d.line((152,y,152,y+2),fill=color)
    for x in range(10,151,7):
        d.line((x,7,x+2,7),fill=color)
        d.line((x,248,x+2,248),fill=color)


def ocean(t):
    im=background((13,100,137),(5,18,48)).convert('RGB');d=ImageDraw.Draw(im,'RGBA')
    # Shafts of sunlight and rolling underwater caustics.
    for i in range(5):
        x=-50+i*53+math.sin(t+i)*7
        d.polygon([(x,0),(x+8,0),(x+74,195),(x+31,195)],fill=(123,233,242,14))
    for y in range(18,215,17):
        points=[(x,y+math.sin(x/17+t+y)*3) for x in range(0,W,2)]
        d.line(points,fill=(107,223,226,24),width=1)
    d.polygon([(0,211),(24,200),(48,215),(93,193),(119,210),(160,197),(160,256),(0,256)],fill=(24,54,65))
    for x,y,r in [(18,225,19),(134,218,24),(108,240,14),(45,246,14)]:
        d.polygon([(x-r,y),(x-r*.6,y-r),(x+r*.35,y-r*.8),(x+r,y-3),(x+r*.8,y+9),(x-r,y+8)],fill='#39566A')
        d.line([(x-r*.6,y-r),(x+r*.35,y-r*.8),(x+r,y-3)],fill='#64878C',width=2)
    d.polygon([(0,248),(30,237),(64,247),(92,240),(130,250),(160,238),(160,256),(0,256)],fill='#A49874')
    for x in (13,29,145,117):
        for j in range(3):
            pts=curve([(x,245),(x-12+j*9,217),(x+8*math.sin(t+j),204),(x-4+j*5+math.sin(t)*3,185+j*5)])
            d.line(pts,fill=('#28766C','#43A08C','#71BD9A')[j],width=3)
    for x,y in [(52,237),(96,244)]:
        for j in range(5):
            dx=(j-2)*6
            d.line([(x,y),(x+dx/2,y-13),(x+dx,y-22-abs(dx)/2)],fill='#DF7F8D',width=3)
            d.ellipse((x+dx-2,y-24-abs(dx)/2,x+dx+2,y-20-abs(dx)/2),fill='#F6BCAB')
    # Fish remain within the scene, turn smoothly, and return to their starting pose.
    for i,(cy,col) in enumerate([(70,'#FFC76C'),(112,'#F49DAD'),(157,'#8FD7E5')]):
        phase=t+i*2.1;cx=80+44*math.sin(phase);cy+=5*math.cos(phase*2)
        facing=1 if math.cos(phase)>=0 else -1
        tail=3*math.sin(t*4+i)
        d.polygon([(cx-facing*10,cy),(cx-facing*19,cy-8+tail),(cx-facing*19,cy+8+tail)],fill=col)
        d.ellipse((cx-12,cy-6,cx+12,cy+6),fill=col)
        d.line((cx-7,cy+2,cx+5,cy+3),fill='#E8F6D9',width=2)
        d.polygon([(cx-2,cy-5),(cx+4,cy-11),(cx+8,cy-4)],fill=col)
        ex=cx+facing*8;d.ellipse((ex-1.5,cy-2,ex+1.5,cy+1),fill='#102C42')
    for i in range(12):
        x=18+(i*37)%125+math.sin(t+i)*3;y=20+(i*29-t/TAU*216)%216;r=1+i%3
        d.ellipse((x-r,y-r,x+r,y+r),outline=(170,239,246,150),width=1)
    border(im,'#598D9D');return im.convert('RGBA')


def blossom(t):
    im=background((26,24,58),(126,73,112));d=ImageDraw.Draw(im)
    d.ellipse((91,19,132,60),fill='#F5DBC9');d.ellipse((86,15,123,52),fill='#29213E')
    for i in range(40):
        x=(i*67)%147+7;y=(i*43)%145+8
        d.point((x,y),fill='#CDAECF')
    d.polygon([(0,212),(26,180),(50,200),(87,170),(120,191),(160,163),(160,256),(0,256)],fill='#674663')
    d.polygon([(0,230),(50,213),(89,225),(127,207),(160,223),(160,256),(0,256)],fill='#392D48')
    trunk=curve([(55,248),(79,202),(43,152),(70,95)])
    d.line(trunk,fill='#392631',width=14);d.line(trunk,fill='#8A5A63',width=5)
    branches=[[(61,185),(40,146),(27,148),(18,113)],[(61,162),(102,147),(103,116),(135,105)],[(61,143),(47,108),(30,103),(24,83)],[(64,116),(91,97),(94,79),(111,68)]]
    for pts in branches:
        d.line(curve(pts),fill='#694352',width=6);d.line(curve(pts),fill='#AC7278',width=2)
    rng=random.Random(641)
    clusters=[(23,91),(38,110),(22,124),(61,93),(88,82),(112,73),(102,118),(133,106)]
    for cx,cy in clusters:
        for _ in range(19):
            x=cx+rng.uniform(-20,20)+math.sin(t+cy)*1.2;y=cy+rng.uniform(-12,12);r=rng.uniform(2,5)
            color=rng.choice(['#D47D9C','#E89AB2','#F4BDD0','#FFE0DB'])
            d.ellipse((x-r,y-r*.65,x+r,y+r*.65),fill=color)
            if r>3:d.point((x,y),fill='#FFF2E2')
    # This perch joins the trunk and passes directly under both fixed feet.
    # Only the body breathes; moving the feet made the bird float off its branch.
    perch=curve([(62,172),(75,161),(98,165),(116,153)])
    d.line(perch,fill='#563646',width=7)
    d.line(perch,fill='#B37D83',width=2)
    d.line([(106,159),(114,148),(126,146)],fill='#8A5A63',width=3)
    for x,y in [(117,148),(124,145),(129,149)]:
        d.ellipse((x-4,y-3,x+4,y+3),fill='#F4BDD0')
        d.point((x,y),fill='#FFF2E2')
    by=148+math.sin(t*2)*.7
    for foot in (84,90):
        d.line((foot+1,by+8,foot,163),fill='#EFC18E',width=2)
        d.line((foot-2,164,foot+2,164),fill='#EFC18E',width=1)
    d.polygon([(79,by+5),(70,by+12),(81,by+9)],fill='#2A2C47')
    d.ellipse((77,by-2,94,by+10),fill='#8DB5C2');d.ellipse((84,by-7,95,by+3),fill='#D1E5DC')
    d.ellipse((80,by+1,89,by+7),fill='#526B94');d.polygon([(94,by-3),(100,by-1),(94,by+1)],fill='#EFB980')
    d.point((92,by-3),fill='#202334')
    for i in range(15):
        y=70+(i*37+t/TAU*180)%180;x=15+(i*23)%130+math.sin(t+i)*9
        d.ellipse((x-2,y-1,x+2,y+2),fill='#F6BCD0')
    border(im,'#C794AA');return im


_storm_rng=random.Random(4841)
_storm_grid=[[_storm_rng.random() for _ in range(32)] for _ in range(32)]


def storm_noise(x,y,cell):
    x/=cell;y/=cell;ix=math.floor(x);iy=math.floor(y);u=x-ix;v=y-iy
    u=u*u*(3-2*u);v=v*v*(3-2*v)
    a=_storm_grid[iy%32][ix%32]*(1-u)+_storm_grid[iy%32][(ix+1)%32]*u
    b=_storm_grid[(iy+1)%32][ix%32]*(1-u)+_storm_grid[(iy+1)%32][(ix+1)%32]*u
    return a*(1-v)+b*v


def lightning(t):
    # Four-second loop: downward leader, white flash, fading afterglow, then darkness.
    phase=(t/TAU)%1
    active=.06<=phase<.65
    reach=max(0,min(1,(phase-.06)/.16)) if active else 0
    strength=1 if active and phase<=.28 else max(0,1-(phase-.28)/.37)**2 if active else 0
    flash=strength*(.35 if reach<1 else 1)
    sky=Image.new('L',(W,H));pixels=sky.load()
    dx=math.sin(t)*9;dy=math.cos(t)*7
    for y in range(H):
        for x in range(W):
            noise=.55*storm_noise(x+dx,y+dy,43)+.3*storm_noise(x-dy,y+dx,19)+.15*storm_noise(x+dy,y-dx,8)
            cloud=max(0,(noise-.30)/.70)**1.7
            edge=abs(x-W/2)/(W/2)
            pixels[x,y]=min(95,round(5+cloud*(42+flash*44)+edge*cloud*13))
    bolt=[(84,5),(94,18),(77,27),(101,42),(72,53),(85,65),(55,79),
          (73,92),(64,109),(90,124),(69,139),(83,154),(62,169),(76,184),
          (54,200),(78,217),(68,233),(83,251)]
    branches=[[(72,53),(112,62),(104,78),(129,92)],
              [(64,109),(40,125),(47,140),(27,157)],
              [(62,169),(99,179),(92,196),(117,211)]]
    mask=Image.new('L',(W,H));draw=ImageDraw.Draw(mask)
    def leader(points,width):
        cutoff=5+246*reach
        for a,b in zip(points,points[1:]):
            if a[1]>=cutoff:break
            end=b if b[1]<=cutoff else (a[0]+(b[0]-a[0])*(cutoff-a[1])/(b[1]-a[1]),cutoff)
            draw.line([a,end],fill=255,width=width)
    if active:
        leader(bolt,3)
        for branch in branches:leader(branch,2)
        haze=mask.filter(ImageFilter.GaussianBlur(9)).point(lambda v:min(255,round(v*1.8*strength)))
        halo=mask.filter(ImageFilter.GaussianBlur(2.5)).point(lambda v:min(255,round(v*.9*strength)))
        core=mask.point(lambda v:round(v*strength))
        sky=ImageChops.add(sky,haze);sky=ImageChops.add(sky,halo);sky=ImageChops.add(sky,core)
    # A fine irregular white edge echoes the reference without hiding the dark interval.
    edge=Image.new('L',(W,H));e=ImageDraw.Draw(edge)
    for side in (5,154):
        e.line([(side+math.sin(y*.34)*.7,y) for y in range(5,251,3)],fill=120+round(flash*60),width=1)
    for y in (5,250):e.line([(x,y+math.sin(x*.39)*.6) for x in range(5,155,3)],fill=140+round(flash*50),width=1)
    sky=ImageChops.add(sky,edge.filter(ImageFilter.GaussianBlur(1.6)));sky=ImageChops.lighter(sky,edge)
    return sky.convert('RGBA')


def nether(t):
    im=Image.new('RGBA',(W,H),'#171222');d=ImageDraw.Draw(im)
    # An upright obsidian frame with a rippling purple surface, not a radial vortex.
    for y in range(18,238,3):
        for x in range(19,141,3):
            v=(math.sin(x*.085+t+.45*math.sin(y*.035-t))+
               math.sin(y*.08-t+math.sin(x*.08))+
               .35*math.sin((x+y)*.16+t*2))/2.35
            color=mix((66,22,112),(158,67,207),(v+1)/2)
            d.rectangle((x,y,min(x+2,140),min(y+2,237)),fill=color)
    # Irregular facets imply obsidian blocks without a bright tiled grid.
    rng=random.Random(809)
    for x,y,w,h in ([(x,y,19,32) for x in (0,141) for y in range(0,256,32)]+
                    [(x,y,32,18) for y in (0,238) for x in range(19,141,32)]):
        d.rectangle((x,y,x+w-1,y+h-1),fill=rng.choice(['#21182F','#261D36','#1D172A']))
        d.polygon([(x+2,y+3),(x+w-3,y+1),(x+w-6,y+8),(x+3,y+11)],fill='#3B2B50')
        d.line([(x+3,y+h-4),(x+w-5,y+h-8),(x+w-2,y+h-5)],fill='#4A365F',width=1)
    d.line((19,18,19,237),fill='#9A4CC6',width=1)
    d.line((140,18,140,237),fill='#9A4CC6',width=1)
    def sparks(g):
        for i in range(12):
            x=26+(i*37)%108+math.sin(t+i)*3;y=23+(i*59-t/TAU*208)%208;r=1+i%2
            g.rectangle((x-r,y-r,x+r,y+r),fill=(212,143,239,160))
    glow(im,sparks,2);return im


def basic(_):
    im=background((76,173,220),(21,66,115)).convert('RGB');d=ImageDraw.Draw(im,'RGBA')
    for y in range(H):
        for x in range(0,W,2):
            if (x+y)%4==0:d.point((x,y),fill=(255,255,255,17))
    for x in range(8,160,16):d.line((x,10,x,246),fill=(4,24,54,15))
    # Original three-prong Tar mark, with an embroidered edge.
    polys=[[(26,85),(63,85),(78,98),(78,108),(83,108),(83,98),(99,85),(136,85),(81,121)],[(27,94),(76,129),(78,194),(63,172),(63,141),(45,134)],[(134,94),(87,129),(85,194),(101,172),(101,141),(119,134)]]
    for p in polys:
        d.polygon([(x,y+2) for x,y in p],fill='#143D62');d.polygon(p,fill='#EEF8FC')
        d.line(p+[p[0]],fill='#AFD9EC',width=1)
    d.line((26,214,136,214),fill='#80C7E6',width=1)
    d.line((53,220,109,220),fill='#80C7E6',width=1)
    border(im,'#A6DDF1');return im.convert('RGBA')


def cape_atlas(art):
    face=art.resize((80,128),Image.Resampling.LANCZOS)
    atlas=Image.new('RGBA',(512,256),(17,25,36,255))
    atlas.paste(face,(8,8));atlas.paste(face.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(96,8))
    # Thickness, top and bottom UVs; avoid brightly colored unused atlas texels.
    atlas.paste(face.crop((0,0,1,128)).resize((8,128)),(0,8))
    atlas.paste(face.crop((79,0,80,128)).resize((8,128)),(88,8))
    atlas.paste(face.crop((0,0,80,1)).resize((80,8)),(8,0))
    atlas.paste(face.crop((0,127,80,128)).resize((80,8)),(88,0))
    return atlas


def wing(kind):
    im=Image.new('RGBA',(256,352));d=ImageDraw.Draw(im)
    if kind=='angel':
        # Overlapping coverts, long primaries, individual feather shafts/barbs.
        for row in range(3):
            for i in range(12):
                x=13+i*17;y=127-i*7+row*19
                length=105+math.sin(i/11*math.pi)*56-row*29
                tip=(x+22-row*2,y+length)
                outline=curve([(x,y),(x+24,y+length*.25),(tip[0]+7,tip[1]-17),tip])+curve([tip,(x-8,tip[1]-26),(x-9,y+28),(x,y)])
                color=mix((182,204,226),(251,251,243),row/2)
                d.polygon(outline,fill=color,outline='#91ACC7')
                shaft=curve([(x+3,y+11),(x+7,y+55),(tip[0]-3,tip[1]-20),tip])
                d.line(shaft,fill='#FFFFFF',width=2)
                for k in range(4,13):
                    q=k/14;cy=y+length*q;cx=x+(tip[0]-x)*q
                    d.line((cx-6,cy-6,cx+5,cy+3),fill='#C4D5E4',width=1)
        d.line(curve([(6,157),(47,73),(136,81),(212,34)]),fill='#F7F8F2',width=10)
    elif kind=='demon':
        root=(8,170);tips=[(78,37),(167,14),(239,77),(232,191),(171,298)]
        edge=[root]+curve([root,(26,80),(51,50),tips[0]])+[tips[1]]+curve([tips[1],(189,63),(198,75),tips[2]])
        edge+=curve([tips[2],(198,143),(201,157),tips[3]])+curve([tips[3],(165,204),(152,257),tips[4]])+curve([tips[4],(97,264),(51,265),root])
        d.polygon(edge,fill='#5C253F')
        for i,tip in enumerate(tips):
            pts=curve([root,(70,119+i*12),(tip[0]-15,tip[1]+30),tip])
            d.line(pts,fill='#241E31',width=7);d.line(pts,fill='#B77783',width=2)
        d.line(edge+[edge[0]],fill='#231A2D',width=4)
        for i in range(45):
            x=20+(i*29)%95;y=154+(i*17)%38
            d.ellipse((x,y,x+2,y+2),fill='#A15569')
    else:
        fairy=kind=='fairy'
        upper=curve([(8,172),(36,55),(209,-5),(241,38)])+curve([(241,38),(272,119),(119,204),(8,172)])
        lower=curve([(8,178),(93,166),(222,190),(204,268)])+curve([(204,268),(152,351),(25,286),(8,178)])
        for j,shape in enumerate((upper,lower)):
            d.polygon(shape,fill='#80C9D0' if fairy else '#40335F',outline='#C2F4EB' if fairy else '#211B33')
            # Inset stained-glass panels follow radial wing veins.
            tips=[(48,110),(79,76),(116,51),(160,39),(202,48),(207,80),(166,121)] if j==0 else [(44,217),(67,258),(99,286),(136,296),(172,270),(186,236),(150,204)]
            for k,tip in enumerate(tips):
                pts=curve([(12,177),(74,160 if j==0 else 206),(tip[0]-12,tip[1]+5),tip])
                d.line(pts,fill='#DDFDF1' if fairy else '#E6B1E9',width=3 if fairy else 6)
                if not fairy:
                    d.ellipse((tip[0]-8,tip[1]-8,tip[0]+8,tip[1]+8),fill='#A879C6')
                    d.ellipse((tip[0]-4,tip[1]-4,tip[0]+4,tip[1]+4),fill='#F5D7BB')
            d.line(shape+[shape[0]],fill='#E0F7E9' if fairy else '#BC93DA',width=3)
        if fairy:
            for i in range(8):
                x=35+i*20;y=124-i*9;d.ellipse((x-2,y-2,x+2,y+2),fill='#F6FFF1')
    # Soft directional shading and fine scale texture add volume without geometry spam.
    p=im.load()
    for y in range(im.height):
        for x in range(im.width):
            r,g,b,a=p[x,y]
            if a:
                light=.84+.16*math.sin(x/256*math.pi)+.07*math.cos(y/352*math.pi)
                grain=((x*13+y*7)%11-5)*.45
                p[x,y]=tuple(max(0,min(255,round(c*light+grain))) for c in (r,g,b))+(a,)
    atlas=Image.new('RGBA',(512,512));atlas.paste(im,(0,0));atlas.paste(im.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(256,0))
    return atlas,im


def bandana(kind):
    base=(23,26,34) if kind=='black' else (231,236,239) if kind=='white' else (103,186,220)
    detail=(220,235,246) if kind=='black' else (41,48,63) if kind=='white' else (185,229,245)
    im=Image.new('RGBA',(512,256));p=im.load()
    for y in range(256):
        for x in range(512):
            weave=((x+y)%4==0)*.04+(math.sin(x*.055)*.5+.5)*.08
            p[x,y]=mix(base,detail,weave)+(255,)
    d=ImageDraw.Draw(im)
    for i in range(16):
        x=i*32
        d.line([(x+19,132),(x+9,145),(x+17,144),(x+7,164)],fill=detail,width=2)
    for y in (130,173):
        d.line((0,y,512,y),fill=detail,width=1)
        for x in range(0,512,8):d.line((x,y+3,x+3,y+3),fill=detail,width=1)
    return im


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--preview',type=Path);args=parser.parse_args()
    ROOT.mkdir(parents=True,exist_ok=True);Image.new('RGBA',(1,1),'white').save(ROOT/'white.png')
    samples=[];animations={}
    for kind,draw in [('ocean',ocean),('lightning',lightning),('blossom',blossom),('nether',nether),('basic',basic)]:
        frames=[]
        for frame in range(1 if kind=='basic' else FRAMES):
            art=draw(frame*TAU/FRAMES);cape_atlas(art).save(ROOT/f'{kind}cape_{frame}.png',optimize=True);frames.append(art)
        samples.append((kind.title()+' cape',frames[0]));animations[kind]=frames
    # Remove only obsolete generated duplicates of the static basic cape.
    for frame in range(1,FRAMES):
        (ROOT/f'basiccape_{frame}.png').unlink(missing_ok=True)
    for kind in ('angel','demon','fairy','butterfly'):
        _,art=wing(kind)
        for segment in range(4):
            piece=art.crop((segment*64,0,(segment+1)*64,352))
            atlas=Image.new('RGBA',(128,512));atlas.paste(piece,(0,0));atlas.paste(piece.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(64,0))
            atlas.save(ROOT/f'{kind}wings_{segment}.png',optimize=True)
        (ROOT/f'{kind}wings.png').unlink(missing_ok=True)
        samples.append((kind.title()+' wings',art))
    for kind in ('black','white','basic'):
        bandana(kind).save(ROOT/f'{kind}bandana.png',optimize=True)
    if args.preview:
        args.preview.mkdir(parents=True,exist_ok=True)
        sheet=Image.new('RGB',(1100,790),'#0C0D11');d=ImageDraw.Draw(sheet)
        try:font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',18)
        except OSError:
            try:font=ImageFont.truetype('DejaVuSans.ttf',18)
            except OSError:font=ImageFont.load_default(size=18)
        for i,(label,art) in enumerate(samples):
            x=20+(i%5)*218;y=18+(i//5)*390
            d.text((x,y),label,fill='#ECEBF0',font=font)
            thumb=art.copy();thumb.thumbnail((200,330),Image.Resampling.LANCZOS)
            sheet.paste(thumb,(x+(200-thumb.width)//2,y+38),thumb)
        sheet.save(args.preview/'cosmetics-detail-sheet.png')
        for name,frames in animations.items():
            if len(frames)>1:
                frames[0].save(args.preview/f'{name}-animation.webp',save_all=True,append_images=frames[1:],duration=125,loop=0,lossless=True)
    print('Generated 129 cape textures (512x256), 16 articulated wing panels (128x512), 3 woven bandana textures (512x256).')


if __name__=='__main__':main()
