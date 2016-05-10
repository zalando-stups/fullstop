package org.zalando.stups.fullstop.web.api;

public class ForbiddenException extends ApiException {
    public ForbiddenException(final String msg) {
        super(403, msg);
    }
}
