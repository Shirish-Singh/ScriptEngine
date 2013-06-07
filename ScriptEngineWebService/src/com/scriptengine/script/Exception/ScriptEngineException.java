package com.scriptengine.script.Exception;

/**
 * Base Class Exception for Scripting Engine
 * 
 * @author Shirish Singh
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

	/**
	 * @return errorName
	 */
	 public String getErrorName() {
		 return errorName;
	 }

	 /**
	  * @param errorName
	  */
	 public void setErrorName(String errorName) {
		 this.errorName = errorName;
	 }

	 /**
	  * @return errorDescription
	  */
	 public String getErrorDescription() {
		 return errorDescription;
	 }

	 /**
	  * @param errorDescription
	  */
	 public void setErrorDescription(String errorDescription) {
		 this.errorDescription = errorDescription;
	 }

	 /**
	  * @return cause
	  */
	 public Throwable getCause() {
		 return cause;
	 }

	 /**
	  * @param cause
	  */
	 public void setCause(Throwable cause) {
		 this.cause = cause;
	 }
}
