package org.zalando.stups.fullstop.jobs;

import java.util.Objects;

/**
 * Try out 'functional' style. ;-)
 *
 * @author  jbellmann
 */
class Tuple<U, B> {

    final U _1;
    final B _2;

    Tuple(final U u, final B b) {
        _1 = Objects.requireNonNull(u, "First Argument should never be null");
        _2 = Objects.requireNonNull(b, "Second Argument should never be null");
    }
}
