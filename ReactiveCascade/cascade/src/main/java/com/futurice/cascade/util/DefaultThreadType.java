/*
The MIT License (MIT)

Copyright (c) 2015 Futurice Oy and individual contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.futurice.cascade.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.futurice.cascade.i.NotCallOrigin;
import com.futurice.cascade.i.nonnull;
import com.futurice.cascade.i.nullable;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import static com.futurice.cascade.Async.e;
import static com.futurice.cascade.Async.vv;

/**
 * The default implementation of the subscribe executor
 * <p>
 * Other classes may utilize this with startup parameter, extend it or substitute an own fresh
 * implementation to the {@link com.futurice.cascade.i.IThreadType} interface.
 */
@NotCallOrigin
public class DefaultThreadType extends AbstractThreadType {
    private static final String TAG = DefaultThreadType.class.getSimpleName();
    final boolean inOrderExecution;

    /**
     * Construct a new thread group
     * @param name
     * @param executorService
     * @param queue           may be null in which
     *                        case {@link #isInOrderExecutor()} will return <code>true</code>
     *                        ; may be {@link java.util.concurrent.BlockingDeque} in which
     *                        case {@link #isInOrderExecutor()} will return <code>false</code>
     */
    public DefaultThreadType(
            @NonNull @nonnull final String name,
            @NonNull @nonnull final ExecutorService executorService,
            @Nullable @nullable final BlockingQueue<Runnable> queue) {
        super(name, executorService, queue);

        this.inOrderExecution = queue == null || queue instanceof BlockingDeque;
    }

    private volatile boolean wakeUpIsPending = false; // Efficiency filter to wake the ServiceExecutor only once TODO Is there a simpler way with AtomicBoolean?
    private final Runnable wakeUpRunnable = () -> {
        // Do nothing, this is just used for insurance fast flushing the ServiceExecutor mQueue when items are added out-of-order to the associated BlockingQueue
        wakeUpIsPending = false;
    };

    @Override // IThreadType
    public void run(@NonNull @nonnull final Runnable runnable) {
        if (executorService.isShutdown()) {
            e(TAG, "Executor service for ThreadType='" + getName() + "' was shut down. Can not run " + runnable);
        }

        executorService.submit(runnable);
    }

    @Override // IThreadType
    @SuppressWarnings("unchecked")
    @NotCallOrigin
    public void runNext(@NonNull @nonnull final Runnable runnable) {
        int n;
        if (inOrderExecution || (n = mQueue.size()) == 0) {
            run(runnable);
            return;
        }

        if (executorService.isShutdown()) {
            e(TAG, "Executor service for ThreadType='" + getName() + "' was shut down. Can not run " + runnable);
        }

        // Out of order execution is permitted and desirable to finish functional chains we have started before clouding memory and execution queues by starting more
        if (isInOrderExecutor()) {
            vv(mOrigin, "WARNING: runNext() on single threaded IThreadType. This will be run FIFO only after previously queued tasks");
            mQueue.add(runnable);
        } else {
            ((BlockingDeque) mQueue).addFirst(runnable);
        }
        if (!wakeUpIsPending && ++n != mQueue.size()) {
            // The mQueue changed during submit- just be sure something is submitted to wake the executor right now to pull from the mQueue
            wakeUpIsPending = true;
            executorService.execute(wakeUpRunnable);
        }
    }

    @Override // IThreadType
    public boolean isInOrderExecutor() {
        return inOrderExecution;
    }
}
