package tools.slimfast_w;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;


public class EpochPlusVC extends EpochPair implements ShadowVar {

    public VectorClock RVC = new VectorClock(SlimFastTool_w.INIT_VECTOR_CLOCK_SIZE);

    public EpochPlusVC(int/* epoch */ W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }

    public EpochPlusVC(EpochPlusVC epvc) {
        this(epvc.W);
        RVC.copy(epvc.RVC);
    }

}
