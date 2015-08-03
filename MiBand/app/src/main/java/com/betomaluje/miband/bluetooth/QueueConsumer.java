package com.betomaluje.miband.bluetooth;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Lewis on 10/01/15.
 */
public class QueueConsumer implements Runnable, BTConnectionManager.DataRead {
    private String TAG = this.getClass().getSimpleName();

    private BTCommandManager bleCommandManager;
    private Context context;
    private final LinkedBlockingQueue<BLETask> queue;

    private CountDownLatch mWaitForActionResultLatch;

    public QueueConsumer(Context context, final BTCommandManager bleCommandManager) {
        this.context = context;
        this.bleCommandManager = bleCommandManager;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void add(final BLETask task) {
        queue.add(task);
    }

    @Override
    public void run() {
        while (BTConnectionManager.getInstance(context, null).isConnected()) {
            try {
                final BLETask task = queue.take();

                final List<BLEAction> actions = task.getActions();

                for (BLEAction action : actions) {

                    mWaitForActionResultLatch = new CountDownLatch(1);

                    if (action.run(bleCommandManager)) {
                        boolean waitForResult = action.expectsResult();
                        if (waitForResult) {
                            mWaitForActionResultLatch.await();
                            mWaitForActionResultLatch = null;
                        }
                    } else {
                        Log.v(TAG, "action " + action.getClass().getSimpleName() + " returned false");
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());

            } finally {
                mWaitForActionResultLatch = null;

                if (queue.isEmpty()) {
                    bleCommandManager.setHighLatency();
                }
            }
        }

        if (mWaitForActionResultLatch != null)
            mWaitForActionResultLatch.countDown();

        if (!queue.isEmpty()) {
            queue.clear();
        }
    }

    @Override
    public void OnDataRead() {
        if (mWaitForActionResultLatch != null)
            mWaitForActionResultLatch.countDown();
    }
}
