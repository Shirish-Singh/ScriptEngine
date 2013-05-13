package com.scriptengine.script.Exception;

/**
 * User: Shirish Singh
 * Date: 3/29/13
 * Time: 10:48 PM
 */
public class InvalidLoginException extends ScriptEngineException {

    public InvalidLoginException(String errorName, String errorDescription) {
        super(errorName, errorDescription);
    }
}
