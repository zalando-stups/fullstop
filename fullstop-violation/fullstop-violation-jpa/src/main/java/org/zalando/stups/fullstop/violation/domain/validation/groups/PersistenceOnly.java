package org.zalando.stups.fullstop.violation.domain.validation.groups;

/**
 * Validation group marker interface for constraints, that should be verified only in the persistence layer (Especially
 * not in REST interface).
 *
 * @author ahartmann
 */
public interface PersistenceOnly {
}
