/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private final ViolationHandler delegate;

    private volatile boolean running;

    private Object selector = "/violations";

    private EventBus eventBus;

    private Registration reg;

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
