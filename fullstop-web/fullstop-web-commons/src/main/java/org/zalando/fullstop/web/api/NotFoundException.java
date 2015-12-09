package org.zalando.fullstop.web.api;

public class NotFoundException extends ApiException {
    public NotFoundException(String msg) {
        super(404, msg);
    }
}
