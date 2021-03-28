package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;

import java.util.ArrayList;

public class EpochPlusCV extends EpochPair implements ShadowVar {

    public volatile VectorClock RCV;

    ArrayList<EpochPlusCV> M[];

    public EpochPlusCV(int W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }



}
