package org.zalando.stups.fullstop.violation.entity;

public class CountByAppVersionAndType {

    private final String application;
    private final String version;
    private final String type;
    private final long quantity;


    public CountByAppVersionAndType(final String application, final String version, final String type, final long quantity) {
        this.application = application;
        this.version = version;
        this.type = type;
        this.quantity = quantity;
    }

    public String getApplication() {
        return application;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "{" +
                "application='" + application + '\'' +
                ", version='" + version + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
