package com.scriptengine.script.helper;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.facade.runtime.ProcessInstance;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.script.workflow.service.WorkFlowService;
import com.scriptengine.webservice.ScriptEngineFacadeImpl;

/**
 * 
 * @author Shirish Singh
 *
 */
public class ScriptEngineHelper {

	private final static Logger LOGGER = Logger.getLogger(WorkFlowService.class.getName());

	private static final ScriptEngineHelper scriptEngineHelper=new ScriptEngineHelper();

	private ScriptEngineHelper(){
		super();
	}

	/**
	 * Return scriptEngineHelper singleton object
	 * @return
	 */
	public static ScriptEngineHelper getInstance(){
		return scriptEngineHelper;
	}

	/**
	 * 
	 * @return Path of Bar
	 */
	public String getBarPath(){
		return ScriptEngineFacadeImpl.realPath+ScriptEngineConstants.BAR_FILE_PATH;

	}

	/**
	 * 
	 * @return Path of JAAS
	 */
	public String getJAASPath(){
		return ScriptEngineFacadeImpl.realPath+ScriptEngineConstants.JAAS_FILE_PATH;

	}

	/**
	 * 
	 * @return Path of Bonita Home 
	 */
	public String getBonitaHomePath(){
		return ScriptEngineFacadeImpl.realPath+ScriptEngineConstants.BONITA_HOME_PATH;

	}
	/**
	 * clean function for cleaning Script Engine cache and deleting all process.
	 * @param id
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
