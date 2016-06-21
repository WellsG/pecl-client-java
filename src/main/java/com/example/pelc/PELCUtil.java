package com.example.pelc;

public class PELCUtil {
    static final PelcClient client;
    static {
        client = PelcClientImpl.create("");
    }

    public static PelcClient setUp() {
        return client;
    }
}
