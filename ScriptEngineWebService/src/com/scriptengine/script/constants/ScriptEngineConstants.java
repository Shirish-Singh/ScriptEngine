package com.scriptengine.script.constants;

import com.scriptengine.script.workflow.helper.WorkFlowHelper;


/***
 * CONSTANTS 
 * @author Shirish singh
 *
 */
public class ScriptEngineConstants {
	
	
	public static final String SUCCESS="Success";
	public static final String FAILED="Failed";
	
	public static final String PHONE_TYPE="1";
	public static final String EMAIL_TYPE="2";
	public static final String SEND_LETTER_TYPE="3";
	public static final String VISIT_DEBTOR_TYPE="4";
	public static final String LIST_DEBTOR_WITH_CB="5";
	public static final String SETTLE_DEBTOR="6";
	public static final String PRE_LEGAL="7";
	public static final String SUE_DEBTOR="8";
	public static final String SELL_WRITE_OFF="9";
	
	
	//Setting default to Phone Type
	public static final String DEFAULT=PHONE_TYPE;
	
	
	//PATH
	public static final String BAR_FILE_PATH = "/resources/configuration/ScriptEngine.bar";
	public static final String JAAS_FILE_PATH = "/resources/configuration/jaas-standard.cfg";
	public static final String BONITA_HOME_PATH = "/resources/bonita";
	
	//Time To Live for Stale mapping	
	public static final Long TIME_TO_LIVE=Long.valueOf(WorkFlowHelper.getServletContext().getInitParameter("TimeToLive")); 
	
	//Cron Expression
	//Job Schedule Time 
	public static final String SCHEDULE_TIME=WorkFlowHelper.getServletContext().getInitParameter("JobInterval"); //This is a cron expression
	
}

