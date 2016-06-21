package com.example.pelc;

import java.util.List;

public interface PelcClient {

    public void auth() throws Exception;

    public String importPackage(String productRelease, String brewTag, List<String> packageNames) throws Exception;
}
