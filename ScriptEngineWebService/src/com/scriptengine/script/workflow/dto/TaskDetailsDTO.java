package com.scriptengine.script.workflow.dto;

import java.util.List;

/**
 * User: Shirish Singh
 * Date: 3/29/13
 * Time: 9:41 PM
 */
public class TaskDetailsDTO {

    String taskID;
    String taskName;
    String taskDescription;
    List<String> taskVariables;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public List<String> getTaskVariables() {
        return taskVariables;
    }

    public void setTaskVariables(List<String> taskVariables) {
        this.taskVariables = taskVariables;
    }
}
