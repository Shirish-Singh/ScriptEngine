package com.scriptengine.script.workflow.dto;

import java.util.List;

/**
 * Task Details DTO Class
 * 
 * @author  Shirish Singh
 */
public class TaskDetailsDTO {

    private String taskID;
    private String taskName;
    private String taskDescription;
    private List<String> taskVariables;
    
    /**
     * @return task Id
     */
    public String getTaskID() {
        return taskID;
    }
    
    /**
     * @param taskID
     */
    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
    
    /**
     * @return task Name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return task Description
     */
    public String getTaskDescription() {
        return taskDescription;
    }
    
    /**
     * @param taskDescription
     */
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    /**
     * @return task variables
     */
    public List<String> getTaskVariables() {
        return taskVariables;
    }

    /**
     * @param taskVariables
     */
    public void setTaskVariables(List<String> taskVariables) {
        this.taskVariables = taskVariables;
    }
}
