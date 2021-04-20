package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;


public class EpochPlusCV extends EpochPair implements ShadowVar {

    public VectorClock RVC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);

    private EpochPlusCV[] next = new EpochPlusCV[SlimFastTool.INIT_VECTOR_CLOCK_SIZE];

    public EpochPlusCV(int/* epoch */ W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }

    public EpochPlusCV(EpochPlusCV epcv) {
        this(epcv.W);
        RVC.copy(epcv.RVC);
        ensureCapacity(RVC.size());
    }

    public EpochPlusCV getNextEpcv(int/* epoch */ threadEpoch) {
        int tid = Epoch.tid(threadEpoch);
        ensureCapacity(tid+1);
        EpochPlusCV epcv = next[tid];
        if (epcv == null) {
            epcv = new EpochPlusCV(this);
        }
        if (epcv.RVC.get(tid) != threadEpoch) {
            epcv.RVC.set(tid, threadEpoch);
            next[tid] = epcv;
        }
        return epcv;
    }

    final private void ensureCapacity(int len) {
        int curLength = next.length;
        if (curLength < len) {
            EpochPlusCV[] b = new EpochPlusCV[len];
            for (int i = 0; i < curLength; i++) {
                b[i] = next[i];
            }
            next = b;
        }
    }


}
