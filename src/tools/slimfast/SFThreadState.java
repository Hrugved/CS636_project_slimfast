package tools.slimfast;

import acme.util.Assert;
import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock VC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);
    public int/* epoch */ E; // current epoch = VC[self tid]

    /* other metadata for optimizations */

    // S_t
    private static final int EpochPairCacheSize = 50;
    private int EpochPairCacheCurrentSize;
    private final EpochPair[] EpochPairCache = new EpochPair[EpochPairCacheSize];

    // Q_t
    private static final int EpochPlusCVCacheSize = 50;
    private int EpochPlusCVCacheCurrentSize;
    private final EpochPlusCV[] EpochPlusCVCache = new EpochPlusCV[EpochPlusCVCacheSize];

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
        EpochPair ep = new EpochPair(Epoch.tid(E), Epoch.clock(R), Epoch.tid(W), Epoch.clock(W));
        if (EpochPairCacheCurrentSize < EpochPairCacheSize) EpochPairCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPairCache[EpochPairCacheCurrentSize - 1] = ep;
        return ep;
    }

    public EpochPlusCV getEpochPlusCV(EpochPair prevEpochPair, int/* epoch */ newReadEpoch) {
        EpochPlusCV ep = getEpochPlusCVFromCache(prevEpochPair);
        if (ep != null) return ep; // READINFLREUSE
        ep = generateAndInsertNewEpochPlusCVIntoCache(prevEpochPair, newReadEpoch);
        return ep; // READINFLALLOC
    }

    public EpochPlusCV getEpochPlusCVFromCache(EpochPair prevEpochPair) {
        for (int i = 0; i < EpochPlusCVCacheCurrentSize; i++) {
            if ((EpochPlusCVCache[i].W == prevEpochPair.W) && (Epoch.clock(EpochPlusCVCache[i].RVC.get(Epoch.tid(prevEpochPair.R))) == Epoch.clock(prevEpochPair.R))) {
                return EpochPlusCVCache[i];
            }
        }
        return null;
    }

    public EpochPlusCV generateAndInsertNewEpochPlusCVIntoCache(EpochPair prevEpochPair, int/* epoch */ newReadEpoch) {
        EpochPlusCV epcv = new EpochPlusCV(prevEpochPair.W);
        epcv.RVC.set(Epoch.tid(prevEpochPair.R), prevEpochPair.R);
        epcv.RVC.set(Epoch.tid(newReadEpoch), newReadEpoch);
        if (EpochPlusCVCacheCurrentSize < EpochPlusCVCacheSize) EpochPlusCVCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPlusCVCache[EpochPlusCVCacheCurrentSize - 1] = epcv;
        return epcv;
    }

    public void refresh() {
        currentWriteEpoch = new EpochPair(Epoch.ZERO, E); // R->init, W->curr-thread-epoch
        for (int i = 0; i < EpochPairCacheCurrentSize; i++) {
            EpochPairCache[i] = null;
        }
        EpochPairCacheCurrentSize = 0;
        for (int i = 0; i < EpochPlusCVCacheCurrentSize; i++) {
            EpochPlusCVCache[i] = null;
        }
        EpochPlusCVCacheCurrentSize = 0;
    }

}
