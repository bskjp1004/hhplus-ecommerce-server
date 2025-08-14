package kr.hhplus.be.server.config.lock;

public enum LockType {
    ORDER("ORDER"),
    USER("USER"),
    PRODUCT("PRODUCT"),
    COUPON("COUPON");

    private final String prefix;

    LockType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}