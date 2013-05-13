package com.scriptengine.script.workflow.dto;

import java.util.List;

/**
 * User: Shirish Singh
 * Date: 3/29/13
 * Time: 9:42 PM
 */
public class ProcessDetailsDTO {

    String processID;

    String processDescription;

    List<String> processVariables;

    //ProcessInstanceUUID

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getProcessDescription() {
        return processDescription;
    }

    public void setProcessDescription(String processDescription) {
        this.processDescription = processDescription;
    }

    public List<String> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(List<String> processVariables) {
        this.processVariables = processVariables;
    }
}
