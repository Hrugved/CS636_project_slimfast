package tools.slimfast_w;

import rr.state.ShadowVar;
import tools.util.Epoch;

public class EpochPair implements ShadowVar {

    public volatile int/* epoch */ R;
    public volatile int/* epoch */ W;

    public EpochPair(int/* epoch */ readEpoch, int/* epoch */ writeEpoch) {
        R = readEpoch;
        W = writeEpoch;
    }

    public EpochPair(int readTid, int readClock, int writeTid, int writeClock) {
        R = Epoch.make(readTid, readClock);
        W = Epoch.make(writeTid, writeClock);
    }

    public EpochPair() {
    }

}
