package org.zalando.stups.fullstop.violation.reactor;

import org.springframework.context.SmartLifecycle;

public class EventBusViolationHandler implements SmartLifecycle {

    private final Object monitor = new Object();
    private volatile boolean running;

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        synchronized (monitor) {

            running = true;
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        synchronized (monitor) {

            running = false;
        }

    }

    @Override
    public boolean isRunning() {

        return running;
    }

    @Override
    public int getPhase() {

        return -200;
    }

    @Override
    public boolean isAutoStartup() {

        return true;
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }

}
