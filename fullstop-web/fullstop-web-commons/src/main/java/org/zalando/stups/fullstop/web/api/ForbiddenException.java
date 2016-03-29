package org.zalando.stups.fullstop.web.api;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String msg) {
        super(403, msg);
    }
}
