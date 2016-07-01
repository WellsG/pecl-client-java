package com.example.pelc;

import com.google.gson.annotations.SerializedName;

public class Task {

    private String id;
    @SerializedName("package_name")
    private String packageName;
    @SerializedName("package_nvr")
    private String packageNVR;
    private String status;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getPackageNVR() {
        return packageNVR;
    }
    public void setPackageNVR(String packageNVR) {
        this.packageNVR = packageNVR;
    }
}
