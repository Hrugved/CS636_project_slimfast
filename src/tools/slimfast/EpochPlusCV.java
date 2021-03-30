package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EpochPlusCV extends EpochPair implements ShadowVar {

    public volatile VectorClock RVC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);;

    HashMap<Integer,EpochPlusCV> next = new HashMap<>();

    public EpochPlusCV(int W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }

    public EpochPlusCV(EpochPlusCV epcv) {
        this(epcv.W);
        RVC.copy(epcv.RVC);
    }

    public EpochPlusCV getNextEpcv(int threadEpoch) {
        int tid = Epoch.tid(threadEpoch);
        EpochPlusCV epcv = next.get(tid);
        if(epcv==null) {
            epcv = new EpochPlusCV(this);
            epcv.RVC.set(tid,threadEpoch);
            next.put(tid,epcv);
        }
        return epcv;
    }

}
