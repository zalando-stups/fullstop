package org.zalando.stups.fullstop.violation.entity;

public class CountByAccountAndType {

    private final String account;
    private final String type;
    private final long quantity;

    public CountByAccountAndType(String account, String type, long quantity) {
        this.account = account;
        this.type = type;
        this.quantity = quantity;
    }

    public String getAccount() {
        return account;
    }

    public String getType() {
        return type;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "{" + "account='" + account + '\'' + ", type='" + type + '\'' + ", quantity=" + quantity + '}';
    }


}
