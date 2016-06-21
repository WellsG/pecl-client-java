package com.example.pelc;

public interface PelcClient {

    public void auth() throws Exception;

    public String importPackage(String productRelease, String brewTag, String packageName) throws Exception;
}
