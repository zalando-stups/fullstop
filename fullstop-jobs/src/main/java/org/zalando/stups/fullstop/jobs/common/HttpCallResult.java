package org.zalando.stups.fullstop.jobs.common;

public class HttpCallResult {
    private  boolean open;
    private String message;


    HttpCallResult(final boolean isOpen, final String message) {
        this.open = isOpen;
        this.message = message;
    }

    void setIsOpen() {
        this.open = true;
    }

    public boolean isOpen() {
        return open;
    }


    public String getMessage() {
        return message;
    }

    void setMessage(final String message) {
        this.message = message;
    }
}
