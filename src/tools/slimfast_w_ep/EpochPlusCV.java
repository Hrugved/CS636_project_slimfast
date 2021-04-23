package tools.slimfast_w_ep;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;


public class EpochPlusCV extends EpochPair implements ShadowVar {

    public VectorClock RVC = new VectorClock(SlimFastTool_w_ep.INIT_VECTOR_CLOCK_SIZE);

    public EpochPlusCV(int/* epoch */ W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }

    public EpochPlusCV(EpochPlusCV epcv) {
        this(epcv.W);
        RVC.copy(epcv.RVC);
    }

}
