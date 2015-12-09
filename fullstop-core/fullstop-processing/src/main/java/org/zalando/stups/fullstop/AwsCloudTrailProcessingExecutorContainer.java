package org.zalando.stups.fullstop;

import com.amazonaws.services.cloudtrail.processinglibrary.AWSCloudTrailProcessingExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author jbellmann
 */
@Component
public class AwsCloudTrailProcessingExecutorContainer implements SmartLifecycle {

    private final AWSCloudTrailProcessingExecutor executor;

    @Autowired
    private FullstopContainerProperties fullstopContainerProperties = new FullstopContainerProperties();

    private volatile boolean running = false;

    @Autowired
    public AwsCloudTrailProcessingExecutorContainer(final AWSCloudTrailProcessingExecutor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }

        Assert.notNull(executor, "Executor should never be null");
        executor.start();
        running = true;
    }

    @Override
    public synchronized void stop() {
        if (!isRunning()) {
            return;
        }

        if (executor != null) {
            executor.stop();
            running = false;
        }
    }

    @Override
    public boolean isRunning() {

        return running;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {

        return fullstopContainerProperties.isAutoStart();
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

}
