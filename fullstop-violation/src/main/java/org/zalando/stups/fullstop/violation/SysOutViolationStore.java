/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation;

import java.util.concurrent.atomic.AtomicInteger;

import org.zalando.stups.fullstop.violation.entity.Violation;

/**
 * Prints the 'violation' to system-out.
 *
 * @author  jbellmann
 */
public class SysOutViolationStore implements ViolationStore {

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void save(final Violation violation) {
        counter.incrementAndGet();
        System.out.println(violation.toString());
    }

    public int getInvocationCount() {
        return counter.get();
    }

}
