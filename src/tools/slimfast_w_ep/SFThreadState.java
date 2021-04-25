package tools.slimfast_w_ep;

import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock VC = new VectorClock(SlimFastTool_w_ep.INIT_VECTOR_CLOCK_SIZE);
    public int/* epoch */ E; // current epoch = VC[self tid]

    /* other metadata for optimizations */

    // S_t
    private static final int EpochPairCacheSize = 20;
    private int EpochPairCacheCurrentSize;
    private final EpochPair[] EpochPairCache = new EpochPair[EpochPairCacheSize];

    // W_t
    public EpochPair currentWriteEpoch;

    public EpochPair getEpochPair(int/* epoch */ R, int/* epoch */ W) {
        EpochPair ep = getEpochPairFromCache(W);
        if (ep != null) return ep; // READEXCLREUSE
        ep = generateAndInsertNewEpochPairIntoCache(R, W);
        return ep; // READEXCLALLOC
    }

    public EpochPair getEpochPairFromCache(int/* epoch */ W) {
        for (int i = 0; i < EpochPairCacheCurrentSize; i++) {
            if (EpochPairCache[i].W == W) {
                return EpochPairCache[i];
            }
        }
        return null;
    }

    public EpochPair generateAndInsertNewEpochPairIntoCache(int/* epoch */ R, int/* epoch */ W) {
        EpochPair ep = new EpochPair(Epoch.tid(R), Epoch.clock(R), Epoch.tid(W), Epoch.clock(W));
        if (EpochPairCacheCurrentSize < EpochPairCacheSize) EpochPairCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPairCache[EpochPairCacheCurrentSize - 1] = ep;
        return ep;
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
