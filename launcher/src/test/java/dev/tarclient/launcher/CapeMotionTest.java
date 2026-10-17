package dev.tarclient.launcher;

import dev.tarclient.config.CapeMotion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapeMotionTest {
    @Test void allCapeCornersStayBehindTheJacketThroughoutIdleAndRunningMotion(){
        // Evaluate the actual hinged cuboid vertices, independently of rendering.
        // The old negative-X bend drives the bottom vertices below the Z=2.25 skin back.
        for(float stride:new float[]{0,.1f,.5f,1,2,10,-1})for(int frame=0;frame<720;frame++){
            float t=frame/60f;double pitch=CapeMotion.rootPitch(stride),z=CapeMotion.BACK_OFFSET;
            for(int section=0;section<4;section++){
                pitch+=CapeMotion.hingePitch(section,t);
                assertTrue(pitch>0&&pitch<Math.PI/2,"Cape must hang down and outwards");
                for(double y:new double[]{0,4})for(double depth:new double[]{0,1}){
                    double back=z+Math.sin(pitch)*y+Math.cos(pitch)*depth;
                    assertTrue(back>=2.75,"Cape penetrated the jacket clearance plane");
                }
                z+=4*Math.sin(pitch);
            }
            assertTrue(z>CapeMotion.BACK_OFFSET+1,"Hem should sit visibly behind its attachment");
        }
    }
    @Test void invalidAnimationInputsCannotProduceNanVertices(){
        for(float value:new float[]{Float.NaN,Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY}){
            assertTrue(Float.isFinite(CapeMotion.rootPitch(value)));
            for(int section=0;section<4;section++)assertTrue(Float.isFinite(CapeMotion.hingePitch(section,value)));
        }
        assertThrows(IllegalArgumentException.class,()->CapeMotion.hingePitch(4,0));
    }
}
