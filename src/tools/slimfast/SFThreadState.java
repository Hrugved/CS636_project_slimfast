package tools.slimfast;

import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock VC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);
    public int/* epoch */ E; // current epoch = VC[self tid]

    /* other metadata for optimizations */

    // S_t
    private static final int EpochPairCacheSize = SlimFastTool.CACHE_SIZE;
    private int EpochPairCacheCurrentSize;
    private final EpochPair[] EpochPairCache = new EpochPair[EpochPairCacheSize];

    // Q_t
    private static final int EpochPlusVCCacheSize = SlimFastTool.CACHE_SIZE;
    private int EpochPlusVCCacheCurrentSize;
    private final EpochPlusVC[] EpochPlusVCCache = new EpochPlusVC[EpochPlusVCCacheSize];

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

    public EpochPlusVC getEpochPlusVC(EpochPair prevEpochPair, int/* epoch */ newReadEpoch) {
        EpochPlusVC ep = getEpochPlusVCFromCache(prevEpochPair);
        if (ep != null) return ep; // READINFLREUSE
        ep = generateAndInsertNewEpochPlusVCIntoCache(prevEpochPair, newReadEpoch);
        return ep; // READINFLALLOC
    }

    public EpochPlusVC getEpochPlusVCFromCache(EpochPair prevEpochPair) {
        for (int i = 0; i < EpochPlusVCCacheCurrentSize; i++) {
            if ((EpochPlusVCCache[i].W == prevEpochPair.W) && (Epoch.clock(EpochPlusVCCache[i].RVC.get(Epoch.tid(prevEpochPair.R))) == Epoch.clock(prevEpochPair.R))) {
                return EpochPlusVCCache[i];
            }
        }
        return null;
    }

    public EpochPlusVC generateAndInsertNewEpochPlusVCIntoCache(EpochPair prevEpochPair, int/* epoch */ newReadEpoch) {
        EpochPlusVC epvc = new EpochPlusVC(prevEpochPair.W);
        epvc.RVC.set(Epoch.tid(prevEpochPair.R), prevEpochPair.R);
        epvc.RVC.set(Epoch.tid(newReadEpoch), newReadEpoch);
        if (EpochPlusVCCacheCurrentSize < EpochPlusVCCacheSize) EpochPlusVCCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPlusVCCache[EpochPlusVCCacheCurrentSize - 1] = epvc;
        return epvc;
    }

    public void refresh() {
        currentWriteEpoch = new EpochPair(Epoch.ZERO, E); // R->init, W->curr-thread-epoch
        for (int i = 0; i < EpochPairCacheCurrentSize; i++) {
            EpochPairCache[i] = null;
        }
        EpochPairCacheCurrentSize = 0;
        for (int i = 0; i < EpochPlusVCCacheCurrentSize; i++) {
            EpochPlusVCCache[i] = null;
        }
        EpochPlusVCCacheCurrentSize = 0;
    }

}
