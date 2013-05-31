package com.scriptengine.script.workflow.utils;

/**
 * TimeUtil Class Holds utility functions for manipulating time. 
 * @author Shirish Singh
 * @since 1.6
 */
public class TimeUtil {
	
	/**
	 * Function for returning unix time stamp.
	 * 
	 * @return unix time stamp
	 */
	public static Long getUnixTimeStamp(){
		return System.currentTimeMillis() / 1000L;
	}
	
}
