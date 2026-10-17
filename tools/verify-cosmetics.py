"""Validate the texture/renderer contract and seamless animation endpoints."""
import importlib.util
from PIL import Image, ImageChops, ImageStat

from pathlib import Path
spec=importlib.util.spec_from_file_location('art',Path(__file__).with_name('generate-cosmetics.py'))
art=importlib.util.module_from_spec(spec);spec.loader.exec_module(art)
memory=0
for kind,draw in [('ocean',art.ocean),('lightning',art.lightning),('blossom',art.blossom),('nether',art.nether),('basic',art.basic)]:
    count=1 if kind=='basic' else art.FRAMES
    assert len(list(art.ROOT.glob(f'{kind}cape_*.png')))==count,kind
    for frame in range(count):
        with Image.open(art.ROOT/f'{kind}cape_{frame}.png') as image:
            assert image.size==(512,256)
            # Cape cutout rendering must never acquire holes from translucent artwork.
            assert image.getchannel('A').getextrema()==(255,255),(kind,frame)
            front=image.crop((8,8,88,136));back=image.crop((96,8,176,136))
            assert ImageChops.difference(front,back.transpose(Image.Transpose.FLIP_LEFT_RIGHT)).getbbox() is None
            memory+=image.width*image.height*4
    difference=ImageStat.Stat(ImageChops.difference(draw(0).convert('RGB'),draw(art.TAU).convert('RGB')))
    assert max(difference.mean)<1,(kind,'loop endpoint mismatch',difference.mean)
for kind in ('angel','demon','fairy','butterfly'):
    _,original=art.wing(kind)
    for segment in range(4):
        with Image.open(art.ROOT/f'{kind}wings_{segment}.png') as image:
            assert image.size==(128,512)
            front=image.crop((0,0,64,352))
            expected=original.crop((segment*64,0,(segment+1)*64,352))
            assert front.tobytes()==expected.tobytes(),(kind,segment,'broken UV join')
            assert front.transpose(Image.Transpose.FLIP_LEFT_RIGHT).tobytes()==image.crop((64,0,128,352)).tobytes()
            memory+=image.width*image.height*4
for kind in ('black','white','basic'):
    with Image.open(art.ROOT/f'{kind}bandana.png') as image:
        assert image.size==(512,256)
        assert image.getchannel('A').getextrema()==(255,255)
        memory+=image.width*image.height*4
assert memory<=80*1024*1024
print(f'PASS: cape opacity/UVs, 32-frame loops, articulated wing joins, bandanas; {memory/1024/1024:.1f} MiB decoded texture budget (all styles loaded).')
