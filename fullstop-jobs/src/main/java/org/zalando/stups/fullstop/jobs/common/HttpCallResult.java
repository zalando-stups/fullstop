package org.zalando.stups.fullstop.jobs.common;

public class HttpCallResult {
    private  boolean open;
    private String message;


    public HttpCallResult(boolean isOpen, String message) {
        this.open = isOpen;
        this.message = message;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
