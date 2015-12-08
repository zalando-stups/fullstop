package org.zalando.stups.fullstop.violation.reactor;

import org.springframework.context.SmartLifecycle;
import org.zalando.stups.fullstop.violation.Violation;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selectors;

/**
 * @author jbellmann
 */
public abstract class EventBusViolationHandler implements SmartLifecycle {

    private final Object monitor = new Object();

    private volatile boolean running;

    private Object selector = "/violations";

    private EventBus eventBus;

    private Registration reg;

    public EventBusViolationHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected void handleEvent(Event<?> event) {
        handleUnwrappedEvent(event.getData());
    }

    protected void handleUnwrappedEvent(Object data) {
        Violation violation = Violation.class.cast(data);
        handleViolation(violation);
    }

    public abstract void handleViolation(Violation violation);

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }

        synchronized (monitor) {
            reg = eventBus.on(Selectors.$(selector), ev -> handleEvent(ev));
            running = true;
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        synchronized (monitor) {
            reg.cancel();
            reg = null;
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
