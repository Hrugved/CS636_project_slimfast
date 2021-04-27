package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;
import tools.util.VectorClock;


public class EpochPlusVC extends EpochPair implements ShadowVar {

    public VectorClock RVC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);

    private EpochPlusVC[] next = new EpochPlusVC[SlimFastTool.INIT_VECTOR_CLOCK_SIZE];

    public EpochPlusVC(int/* epoch */ W) {
        this.W = W;
        R = Epoch.READ_SHARED;
    }

    public EpochPlusVC(EpochPlusVC epvc) {
        this(epvc.W);
        RVC.copy(epvc.RVC);
        ensureCapacity(RVC.size());
    }

    public EpochPlusVC getNextEpvc(int/* epoch */ threadEpoch) {
        int tid = Epoch.tid(threadEpoch);
        ensureCapacity(tid+1);
        EpochPlusVC epvc = next[tid];
        if (epvc == null) {
            epvc = new EpochPlusVC(this);
        }
        if (epvc.RVC.get(tid) != threadEpoch) {
            epvc.RVC.set(tid, threadEpoch);
            next[tid] = epvc;
        }
        return epvc;
    }

    final private void ensureCapacity(int len) {
        int curLength = next.length;
        if (curLength < len) {
            EpochPlusVC[] b = new EpochPlusVC[len];
            for (int i = 0; i < curLength; i++) {
                b[i] = next[i];
            }
            next = b;
        }
    }


}
