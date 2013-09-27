package com.scriptengine.script.helper;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.facade.runtime.ProcessInstance;

import com.scriptengine.dto.IncomingDataDTO;
import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.script.workflow.service.WorkFlowService;

/**
 * An Helper class for Script Engine Facade
 * 
 * @author Shirish Singh
 *
 */
public class ScriptEngineHelper {

	private final static Logger LOGGER = Logger.getLogger(WorkFlowService.class.getName());
	
	/**
	 * Server Path
	 */
	private static String realServerPath = null;

	/**
	 * Initialise Script Engine Helper
	 */
	private static final ScriptEngineHelper scriptEngineHelper=new ScriptEngineHelper();
	
	/**
	 * private script Engine Helper constructor
	 */
	private ScriptEngineHelper(){
		super();
	}
	
	/**
	 * @return realServerPath
	 */
	public static String getRealServerPath() {
		return realServerPath;
	}

	/**
	 * 
	 * @param realServerPath
	 */
	public static void setRealServerPath(String realServerPath) {
		ScriptEngineHelper.realServerPath = realServerPath;
	}
	
	/**
	 * Return scriptEngineHelper singleton object
	 * 
	 * @return scriptEngineHelper
	 */
	public static ScriptEngineHelper getInstance(){
		return scriptEngineHelper;
	}

	/**
	 * @return Path of Bar
	 */
	public String getBarPath(){
		return getRealServerPath()+ScriptEngineConstants.BAR_FILE_PATH;

	}

	/**
	 * @return Path of JAAS
	 */
	public String getJAASPath(){
		return getRealServerPath()+ScriptEngineConstants.JAAS_FILE_PATH;

	}

	/**
	 * @return Path of Bonita Home 
	 */
	public String getBonitaHomePath(){
		return getRealServerPath()+ScriptEngineConstants.BONITA_HOME_PATH;

	}
	/**
	 * Clean function for cleaning Script Engine cache and deleting all process.
	 * 
	 * @return boolean true(successful)/false(failed)
	 */
	public boolean clean(IncomingDataDTO incomingDataDTO){
		try {
			WorkFlowService workFlowService=WorkFlowHelper.getWorkFlowServiceInstance();
			workFlowService.login();
			ProcessInstance processInstance=WorkFlowHelper.getInMemoryCache().get(incomingDataDTO);
			workFlowService.deleteProcessInstanceAndCacheEntry(processInstance);
			return true;
		} catch (ScriptEngineException scriptEngineException) {
			LOGGER.log(Level.SEVERE,"Script Engine: Exception occured during cleaning"+scriptEngineException.getMessage());
			return false;
		} catch (LoginException e) {
			LOGGER.log(Level.SEVERE,"Script Engine: Login Exception Occured");
			return false;
		}
	}
}
