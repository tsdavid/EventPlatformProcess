package com.dk.platform.ems.common;

public enum EmsConnConf {

    CONN_ATP_COUNT(3),
    CONN_ATP_DELAY_MS(500),
    CONN_ATP_TIMEOUT_MS(100),
    RECONN_ATP_COUNT(500000),
    RECONN_ATP_DELAY_MS(500),
    RECONN_ATP_TIMEOUT_MS(100);

    final private int val;

    public int getVal() {
        return this.val;
    }

    private EmsConnConf(int val) {
        this.val = val;
    }
}
