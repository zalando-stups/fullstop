package org.zalando.stups.fullstop.jpa.auditing;

import org.slf4j.Logger;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author <a href="mailto:michele.randi@zalando.de" title="Michele Randi">mrandi</a>
 */
public final class SpringSecurityAuditorAware implements AuditorAware<String> {

    private final Logger logger = getLogger(SpringSecurityAuditorAware.class);

    @Override
    public String getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "FULLSTOP";
        }
        else {
            Assert.notNull(authentication, "Current authentication is null; could not gather user details from it");

            final String userName = authentication.getName();
            logger.trace("Found Auditor: {}", userName);

            Assert.hasText(userName, "Username should never by empty");

            return userName;
        }
    }

}
