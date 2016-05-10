package org.zalando.stups.fullstop.web.api;

public class NotFoundException extends ApiException {
    public NotFoundException(final String msg) {
        super(404, msg);
    }
}
