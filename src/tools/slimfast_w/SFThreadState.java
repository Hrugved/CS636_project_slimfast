package tools.slimfast_w;

import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock VC = new VectorClock(SlimFastTool_w.INIT_VECTOR_CLOCK_SIZE);
    public int/* epoch */ E; // current epoch = VC[self tid]

    /* other metadata for optimizations */

    // W_t
    public EpochPair currentWriteEpoch;

    public EpochPair getEpochPair(int/* epoch */ R, int/* epoch */ W) {
        return new EpochPair(Epoch.tid(E), Epoch.clock(R), Epoch.tid(W), Epoch.clock(W));
    }

    public EpochPlusCV getEpochPlusCV(EpochPair prevEpochPair, int/* epoch */ newReadEpoch) {
        EpochPlusCV epcv = new EpochPlusCV(prevEpochPair.W);
        epcv.RVC.set(Epoch.tid(prevEpochPair.R), prevEpochPair.R);
        epcv.RVC.set(Epoch.tid(newReadEpoch), newReadEpoch);
        return epcv;
    }

    public void refresh() {
        currentWriteEpoch = new EpochPair(Epoch.ZERO, E); // R->init, W->curr-thread-epoch
    }

}
