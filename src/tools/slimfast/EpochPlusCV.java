package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;

import java.util.ArrayList;

public class EpochPlusCV extends EpochPair implements ShadowVar {

    public volatile VectorClock RVC; // read vector clock

    ArrayList<EpochPlusCV> next;

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
        int clock = Epoch.clock(threadEpoch);
        EpochPlusCV epcv = next.get(tid);
        if(epcv==null) {
            epcv = new EpochPlusCV(this);
            epcv.RVC.set(tid,clock);
            next.set(tid,epcv);
        }
        return epcv;
    }

}
