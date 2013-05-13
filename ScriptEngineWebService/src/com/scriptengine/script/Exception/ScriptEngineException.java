package com.scriptengine.script.Exception;

/**
 * Base Class Exception for Scripting Engine
 * User: Shirish Singh
 * Date: 3/29/13
 * Time: 10:48 PM
 */
public class ScriptEngineException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3384890469675700935L;
	private String errorName;
    private String errorDescription;
    private Throwable cause;

    public ScriptEngineException(String errorName, String errorDescription) {
    	super();
        this.errorName = errorName;
        this.errorDescription = errorDescription;
    }
    
    public ScriptEngineException(String errorName, String errorDescription,Throwable cause) {
    	super();
        this.errorName = errorName;
        this.errorDescription = errorDescription;
        this.cause=cause;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}
}
