package org.zalando.stups.fullstop.jobs.common;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Provides all account ids
 */
public interface AccountIdSupplier extends Supplier<Set<String>> {
}
