package com.example.pelc;

import com.google.gson.annotations.SerializedName;

public class ImportTaskResponse {
    
    @SerializedName("task_id")
    private String taskId;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}
