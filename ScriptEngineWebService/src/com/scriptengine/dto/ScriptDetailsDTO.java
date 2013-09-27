package com.scriptengine.dto;

import java.util.List;

public class ScriptDetailsDTO {
	
	private String scriptName;
    private List<String> outcomes;
    private String errorMsg;
	
    
    public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public List<String> getOutcomes() {
		return outcomes;
	}
	public void setOutcomes(List<String> outcomes) {
		this.outcomes = outcomes;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
    
    

}
