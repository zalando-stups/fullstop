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
package org.zalando.stups.fullstop.jobs;

import java.util.Objects;

/**
 * Try out 'functional' style. ;-)
 *
 * @author jbellmann
 */
class Tuple<U, B> {

    final U _1;

    final B _2;

    Tuple(final U u, final B b) {
        _1 = Objects.requireNonNull(u, "First Argument should never be null");
        _2 = Objects.requireNonNull(b, "Second Argument should never be null");
    }
}
