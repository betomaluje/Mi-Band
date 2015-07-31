package com.betomaluje.miband.bluetooth;

/**
 * Created by Lewis on 10/01/15.
 */
public class WaitAction implements BLEAction {
    private final long duration;

    public WaitAction(final long duration) {
        this.duration = duration;
    }

    @Override
    public boolean expectsResult() {
        return false;
    }

    @Override
    public boolean run(BTCommandManager btCommandManager) {
        return threadWait(duration);
    }

    private boolean threadWait(final long duration) {
        try {
            Thread.sleep(duration);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
