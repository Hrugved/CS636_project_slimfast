package tools.slimfast;

import rr.state.ShadowVar;
import tools.util.Epoch;

public class EpochPair implements ShadowVar {

    public volatile int R;
    public volatile int W;

    public EpochPair(boolean isWrite, int/* epoch */ epoch) {
        if (isWrite) {
            R = Epoch.ZERO;
            W = epoch;
        } else {
            W = Epoch.ZERO;
            R = epoch;
        }
    }

    public EpochPair(int readTid, int readClock, int writeTid, int writeClock) {
        R = Epoch.make(readTid,readClock);
        W = Epoch.make(writeTid,writeClock);
    }

}
