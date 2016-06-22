package com.example.pelc;

import java.util.List;
import java.util.Map;

public interface PelcClient {

    public void auth() throws Exception;

    public Map<String, Task> importPackage(String productRelease, String brewTag, List<String> packageNames) throws Exception;
}
