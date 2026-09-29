package dev.tarclient.config;

/** Unscaled armor geometry, shared by drawing and HUD hit testing. */
public record ArmorLayout(int count, boolean horizontal, boolean reverse, int cellWidth, int cellHeight, int gap) {
    public int width(){return 8+(horizontal?count*cellWidth+Math.max(0,count-1)*gap:cellWidth);}
    public int height(){return 8+(horizontal?cellHeight:count*cellHeight+Math.max(0,count-1)*gap);}
    private int position(int index){return reverse?count-1-index:index;}
    public int x(int index){return 4+(horizontal?position(index)*(cellWidth+gap):0);}
    public int y(int index){return 4+(horizontal?0:position(index)*(cellHeight+gap));}
}
