package org.zalando.stups.fullstop.violation.reactor;

import org.springframework.context.SmartLifecycle;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationHandler;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;

/**
 * 
 * @author jbellmann
 *
 */
public class EventBusViolationHandler implements SmartLifecycle {

    private final Object monitor = new Object();

    private volatile boolean running;

    private Object selector = "/violations";

    private EventBus eventBus;

    private Registration reg;

    private final ViolationHandler delegate;

    public EventBusViolationHandler(EventBus eventBus,
            ViolationHandler delegate) {
        this.eventBus = eventBus;
        this.delegate = delegate;
    }

    protected void handleEvent(Event<?> event) {
        handleUnwrappedEvent(event.getData());
    }

    protected void handleUnwrappedEvent(Object data) {
        Violation violation = Violation.class.cast(data);
        handleViolation(violation);
    }

    protected void handleViolation(Violation violation) {
        this.delegate.handle(violation);
    }

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

    private static final class InternalConsumer<T> implements Consumer<T> {

        @Override
        public void accept(T t) {

        }

    }
}
