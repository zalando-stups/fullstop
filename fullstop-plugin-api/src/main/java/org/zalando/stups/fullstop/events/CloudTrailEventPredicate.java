/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.events;

import java.util.function.Predicate;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * Base for {@link Predicate} implementations.
 *
 * @author  jbellmann
 */
public abstract class CloudTrailEventPredicate implements Predicate<CloudTrailEvent> {

    public CloudTrailEventPredicate andWith(final CloudTrailEventPredicate cloudTrailEventPredicate) {
        return new Internal(this.and(cloudTrailEventPredicate));
    }

    public CloudTrailEventPredicate orWith(final CloudTrailEventPredicate cloudTrailEventPredicate) {
        return new Internal(this.or(cloudTrailEventPredicate));
    }

    public static CloudTrailEventPredicate fromSource(final String eventSource) {
        return new EventSourcePredicate(eventSource);
    }

    public static CloudTrailEventPredicate withName(final String eventName) {
        return new EventNamePredicate(eventName);
    }

    @Override
    public boolean test(final CloudTrailEvent t) {
        return doTest(t);
    }

    public abstract boolean doTest(CloudTrailEvent event);

    /**
     * For type.
     *
     * @author  jbellmann
     */
    static class Internal extends CloudTrailEventPredicate {

        private final Predicate<CloudTrailEvent> delegate;

        Internal(final Predicate<CloudTrailEvent> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean doTest(final CloudTrailEvent event) {
            return delegate.test(event);
        }
    }

}
