package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;

import java.util.ArrayList;

public class EpochPlusCV extends VectorClock implements ShadowVar {

    public volatile int W;
    // R -> VectorClock

    ArrayList<EpochPlusCV> M[];

    public EpochPlusCV(int W) {
        this.W = W;
    }



}
