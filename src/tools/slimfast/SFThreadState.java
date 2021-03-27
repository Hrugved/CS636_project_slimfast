package tools.slimfast;

import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock CV;
    public int E; // current epoch -> clock value = CV[current tid]

    /* other metadata for optimizations */

    // S_t
    private static final int EpochPairCacheSize = 20;
    private int EpochPairCacheCurrentSize;
    private final EpochPair[] EpochPairCache = new EpochPair[EpochPairCacheSize];

    // Q_t
    private static final int EpochPlusCVCacheSize = 20;
    private int EpochPlusCVCacheCurrentSize;
    private final EpochPlusCV[] EpochPlusCVCache = new EpochPlusCV[EpochPlusCVCacheSize];

    // W_t
    private EpochPair currentWriteEpoch;

    public EpochPair getEpochPair(int readClock, int W) {
        EpochPair ep = getEpochPairFromCache(readClock,W);
        if(ep!=null) return ep; // READEXCLREUSE
        ep = generateAndInsertNewEpochPairIntoCache(readClock,W);
        return ep; // READEXCLALLOC
    }

    public EpochPair getEpochPairFromCache(int readClock, int W) {
        for(int i=0;i<EpochPairCacheCurrentSize;i++) {
            if(Epoch.equals(EpochPairCache[i].W,W)) {
                return EpochPairCache[i];
            }
        }
        return null;
    }

    public EpochPair generateAndInsertNewEpochPairIntoCache(int readClock, int W) {
        EpochPair ep = new EpochPair(Epoch.tid(W),readClock,Epoch.tid(W),Epoch.clock(W));
        if(EpochPairCacheCurrentSize<EpochPairCacheSize) EpochPairCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPairCache[EpochPairCacheCurrentSize-1]=ep;
        return ep;
    }

    public EpochPlusCV getEpochPlusCV(EpochPair prevEpoch, int newReadClock, int newReadTid) {
        EpochPlusCV ep = getEpochPlusCVFromCache(prevEpoch, newReadClock, newReadTid);
        if(ep!=null) return ep; // READINFLREUSE
        ep = generateAndInsertNewEpochPlusCVIntoCache(prevEpoch, newReadClock, newReadTid);
        return ep; // READINFLALLOC
    }

    public EpochPlusCV getEpochPlusCVFromCache(EpochPair prevEpoch, int newReadClock, int newReadTid) {
        for(int i=0;i<EpochPlusCVCacheCurrentSize;i++) {
            if((Epoch.equals(EpochPlusCVCache[i].W,prevEpoch.W)) && (EpochPlusCVCache[i].get(Epoch.tid(prevEpoch.R))==Epoch.clock(prevEpoch.R))) {
                return EpochPlusCVCache[i];
            }
        }
        return null;
    }

    public EpochPlusCV generateAndInsertNewEpochPlusCVIntoCache(EpochPair prevEpoch, int newReadClock, int newReadTid) {
        EpochPlusCV epcv = new EpochPlusCV(prevEpoch.W);
        epcv.makeCV(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);
        epcv.set(Epoch.tid(prevEpoch.R),Epoch.clock(prevEpoch.R));
        epcv.set(newReadTid,newReadClock);
        if(EpochPlusCVCacheCurrentSize<EpochPlusCVCacheSize) EpochPlusCVCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPlusCVCache[EpochPlusCVCacheCurrentSize-1]=epcv;
        return epcv;
    }

    public void refresh() {
        currentWriteEpoch=new EpochPair(true,E); // R->init, W->curr-thread-epoch
        for(int i=0;i<EpochPairCacheCurrentSize;i++) {
            EpochPairCache[i] = null;
        }
        for(int i=0;i<EpochPlusCVCacheCurrentSize;i++) {
            EpochPlusCVCache[i]=null;
        }
        EpochPairCacheCurrentSize=0;
        EpochPlusCVCacheCurrentSize=0;
    }

}
