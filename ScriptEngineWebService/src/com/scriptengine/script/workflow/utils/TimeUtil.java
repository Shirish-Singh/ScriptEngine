package com.scriptengine.script.workflow.utils;

/**
 * TimeUtil Class Holds utility functions for manipulating time. 
 * 
 * @author Shirish Singh
 */
public class TimeUtil {
	
	/**
	 * Function for returning current unix time stamp.
	 * 
	 * @return unix time stamp
	 */
	public static Long getCurrentUnixTimeStamp(){
		return System.currentTimeMillis() / 1000L;
	}
	
}
