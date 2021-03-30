package tools.slimfast;

import acme.util.Assert;
import tools.util.Epoch;
import tools.util.VectorClock;

public class SFThreadState {

    /* thread-specific metadata */
    public VectorClock VC = new VectorClock(SlimFastTool.INIT_VECTOR_CLOCK_SIZE);
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
    public EpochPair currentWriteEpoch;

    public EpochPair getEpochPair(int readClock, int W) {
        EpochPair ep = getEpochPairFromCache(readClock,W);
        if(ep!=null) return ep; // READEXCLREUSE
        ep = generateAndInsertNewEpochPairIntoCache(readClock,W);
        return ep; // READEXCLALLOC
    }

    public EpochPair getEpochPairFromCache(int readEpoch, int W) {
        for(int i=0;i<EpochPairCacheCurrentSize;i++) {
            if(Epoch.equals(EpochPairCache[i].W,W)) {
                return EpochPairCache[i];
            }
        }
        return null;
    }

    public EpochPair generateAndInsertNewEpochPairIntoCache(int readEpoch, int W) {
        EpochPair ep = new EpochPair(Epoch.tid(W),Epoch.clock(readEpoch),Epoch.tid(W),Epoch.clock(W));
        if(EpochPairCacheCurrentSize<EpochPairCacheSize) EpochPairCacheCurrentSize++;
        // if overflow -> replace last entry
        EpochPairCache[EpochPairCacheCurrentSize-1]=ep;
        return ep;
    }

    public EpochPlusCV getEpochPlusCV(EpochPair prevEpochPair, int newReadClock, int newReadTid) {
        EpochPlusCV ep = getEpochPlusCVFromCache(prevEpochPair, newReadClock, newReadTid);
        if(ep!=null) return ep; // READINFLREUSE
        ep = generateAndInsertNewEpochPlusCVIntoCache(prevEpochPair, newReadClock, newReadTid);
        return ep; // READINFLALLOC
    }

    public EpochPlusCV getEpochPlusCVFromCache(EpochPair prevEpochPair, int newReadClock, int newReadTid) {
        for(int i=0;i<EpochPlusCVCacheCurrentSize;i++) {
            if((Epoch.equals(EpochPlusCVCache[i].W,prevEpochPair.W)) && (EpochPlusCVCache[i].RVC.get(Epoch.tid(prevEpochPair.R))==Epoch.clock(prevEpochPair.R))) {
                return EpochPlusCVCache[i];
            }
        }
        return null;
    }

    public EpochPlusCV generateAndInsertNewEpochPlusCVIntoCache(EpochPair prevEpochPair, int newReadClock, int newReadTid) {
        EpochPlusCV epcv = new EpochPlusCV(prevEpochPair.W);
        epcv.RVC.set(Epoch.tid(prevEpochPair.R),Epoch.clock(prevEpochPair.R));
        epcv.RVC.set(newReadTid,newReadClock);
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
        EpochPairCacheCurrentSize=0;
        for(int i=0;i<EpochPlusCVCacheCurrentSize;i++) {
            EpochPlusCVCache[i]=null;
        }
        EpochPlusCVCacheCurrentSize=0;
    }

}
