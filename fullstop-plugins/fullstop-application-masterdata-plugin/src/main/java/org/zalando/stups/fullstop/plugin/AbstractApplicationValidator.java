package org.zalando.stups.fullstop.plugin;

import org.zalando.stups.clients.kio.Application;

/**
 * @author  jbellmann
 */
public abstract class AbstractApplicationValidator implements NamedValidator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return Application.class.equals(clazz);
    }

}
