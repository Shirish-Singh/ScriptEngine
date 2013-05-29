package com.scriptengine.script.workflow.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.SimpleCallbackHandler;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.helper.ScriptEngineHelper;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;

/**
 * Work Flow Service To Service Scripting Engine
 * User: Shirish Singh
 * Date: 2013/03/25
 * Time: 9:24 AM
 * Last Updated : 14 April 2013
 */
public abstract class WorkFlowService {


	//TODO: may be in next phase...
	// 1. login / password authn mechanism
	//2. Bar_deploy_path
	//3. JAAS Will be part of project [Shirish: Bonita authentication ]
	//4. BONITA_HOME : Do something
	//6.Scheduler to clean tmp files in bonita home / server/default/tmp .. Think that it is catered by create-drop in config file, still need to verify..?
	//7.Think of singleton class to deploy the bar.
	//9.Re Design Architecture - current architecture dosent cater for configurable process... can check reliability of current by performing performance testing specially memory check...
	//10.web service security
	//11. Process Instance state is set to null at the moment , need to deactivate or change the status to inactive or something for process Instance. 
	//12. REmove collection related code and make Script Engine Generic ...Big Task..think...

	//TODO
	//Take END and Start State from Process variable
	//*If in case of Exception of Severe level restart server clean journal automatically.. [Need to get in depth knowledge of Bonita api run time in this case ).

	//NOTE: Always put variables in state.. in diagram..for current architecture
	
	protected final static Logger LOGGER = Logger.getLogger(WorkFlowService.class.getName());

	static {
		System.setProperty(BonitaConstants.JAAS_PROPERTY, ScriptEngineHelper.getInstance().getJAASPath());
		System.setProperty(BonitaConstants.HOME,ScriptEngineHelper.getInstance().getBonitaHomePath());
	}

	//Configuration
	private static final String LOGIN = "admin";
	private static final String PASSWORD = "bpm";
	private static final String AUTHENTICATE_TYPE = "BonitaAuth";

	//Enhancement : Use Service Locater for below ... Add wrapper and ..runtime Exception if any.. 
	protected final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
	protected final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
	protected final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
	protected final QueryDefinitionAPI queryDefinationAPI = AccessorUtil.getQueryDefinitionAPI();
	//Class level objects
	private ProcessDefinition processDefinition=null;
	private ProcessInstanceUUID processInstanceUUID=null;

	/**
	 * @return ProcessDetailsDTO
	 * @throws ScriptEngineException 
	 */
	public final ProcessDetailsDTO beginProcess() throws ScriptEngineException {

		LOGGER.log(Level.INFO,"Script Engine: Begin Process");
		LOGGER.log(Level.INFO,"BAR FILE IS DEPLOYED AT:"+ScriptEngineHelper.getInstance().getBarPath());
		LOGGER.log(Level.INFO,"BONITA HOME IS AT:"+ScriptEngineHelper.getInstance().getBonitaHomePath());
		LOGGER.log(Level.INFO,"JAAS IS AT:"+ScriptEngineHelper.getInstance().getJAASPath());

		try {
			//create a cache
			WorkFlowHelper.setInMemoryCache(new ConcurrentHashMap<IncomingDataDTO, ProcessInstance>());
			//login
			login(); //One Time Operation
			//clean Work Flow Engine
			cleanScriptEngine(); //One Time Operation (Restart Process)
			//deploy
			ProcessDefinition processDefinition = deployBar();
			//set process definition
			setProcessDefinition(processDefinition);
			//set Process Definition ServletContext
			setProcessDefinitionInServletContext(processDefinition);
			return WorkFlowHelper.constructProcessDetailsDTO(getProcessDefinition(), null);
		} catch (Exception exception) {
			LOGGER.log(Level.SEVERE,"Script Engine : Exception in beginProcess:"+exception.getMessage());
			exception.printStackTrace();
			throw new ScriptEngineException("BEGIN_PROCESS",exception.getMessage(),exception.getCause());
		}
	}

	/**
	 * setProcessDefinitionInServletContext
	 * @param processDefinition
	 */
	private void setProcessDefinitionInServletContext(
			ProcessDefinition processDefinition) {
		WorkFlowHelper.getServletContext().setAttribute(WorkFlowHelper.PROCESS_DEFINATION,processDefinition);
	}

	/**
	 * login
	 * @throws javax.security.auth.login.LoginException
	 */
	public void login() throws LoginException {
		LOGGER.log(Level.INFO,"Script Engine: Login In Process");
		LoginContext loginContext = new LoginContext(AUTHENTICATE_TYPE, new SimpleCallbackHandler(LOGIN, PASSWORD));
		loginContext.login();
		LOGGER.log(Level.INFO,"Script Engine: Login Succesfull");
	}

	/**
	 * cleanScriptEngine
	 */
	public boolean cleanScriptEngine() {
		try {
			LOGGER.log(Level.INFO,"Script Engine: Clean In Process");
			managementAPI.deleteAllProcesses();    //TODO:  R and D
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Clean- Failed : Script Engine Exception in clean Script Engine ");
			e.getMessage();
			return false;
		}finally{
			// Clear cache
			WorkFlowHelper.getInMemoryCache().clear();
		}
	}

	/**
	 * deployBar
	 * @return
	 * @throws Exception
	 */
	protected ProcessDefinition deployBar() throws Exception {
		LOGGER.log(Level.INFO,"Script Engine: Deploying Bar...");
		//deploy the bar file
		File barFile = new File(ScriptEngineHelper.getInstance().getBarPath());
		BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(barFile);
		return managementAPI.deploy(businessArchive);
	}
	
	/**
	 * getProcessInstance : Fetch Process Instance
	 * @param incomingDataDTO
	 * @return
	 * @throws Exception
	 */
	public ProcessInstance getProcessInstance(IncomingDataDTO incomingDataDTO) throws Exception {

		if(WorkFlowHelper.getInMemoryCache().containsKey(incomingDataDTO)){
			return WorkFlowHelper.getInMemoryCache().get(incomingDataDTO);
		}
		
		//This Code is to initialise Script Engine and take it to State depending on Type Id.
		ProcessDefinition definition=(ProcessDefinition) WorkFlowHelper.getServletContext().getAttribute(WorkFlowHelper.PROCESS_DEFINATION);
		Map<IncomingDataDTO,ProcessInstance> map=WorkFlowHelper.getInMemoryCache();
		synchronized (this) {
			ProcessInstanceUUID processInstanceUUID=runtimeAPI.instantiateProcess(definition.getUUID());
			ProcessInstance processInstance=queryRuntimeAPI.getProcessInstance(processInstanceUUID);
			map.put(incomingDataDTO, processInstance);
			WorkFlowHelper.setInMemoryCache(map);
			//This is basically to initialize
			executeTask(processInstance,incomingDataDTO.getTypeID(),null);
			return processInstance;
		}
	}

	/**
	 * instantiateProcess
	 * @param processDefinition
	 * @return ProcessInstanceUUID
	 * @throws Exception
	 */
	protected ProcessInstanceUUID instantiateProcess(ProcessDefinition processDefinition) throws Exception {
		return runtimeAPI.instantiateProcess(processDefinition.getUUID());
	}

	/**
	 * getProcessInstanceUUID
	 *
	 * @return ProcessInstanceUUID
	 */
	public ProcessInstanceUUID getProcessInstanceUUID() {
		return processInstanceUUID;
	}

	/**
	 * setProcessDefinition
	 *
	 * @param processDefinition
	 */
	private void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}

	/**
	 * getProcessDefinition
	 *
	 * @return ProcessDefinition
	 */
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	/**
	 * fetchCurrentTaskVariableValues function
	 * Fetch Current Ready/Active Task's variables list.
	 *
	 * @param processInstance
	 * @return
	 * @throws Exception 
	 */

	abstract public List<String> fetchCurrentTaskVariableValues(ProcessInstance processInstance) throws ScriptEngineException;

	/**
	 * fetchTaskDetailsDTO
	 *
	 * @param processID
	 * @return
	 * @throws ScriptEngineException 
	 * @throws InstanceNotFoundException 
	 * @throws Exception 
	 */
	abstract public TaskDetailsDTO fetchTaskDetails(final ProcessInstance processInstance) throws ScriptEngineException, InstanceNotFoundException, Exception; 

	/**
	 * Fetch Process Details
	 *
	 * @return
	 * @throws ScriptEngineException 
	 */
	abstract public ProcessDetailsDTO fetchProcessDetails() throws ScriptEngineException;

	/**
	 * executeTask
	 *
	 * @param processID
	 * @param inputData
	 * @param adHocToData
	 * @throws Exception 
	 */
	abstract public void executeTask(ProcessInstance processInstance, String inputData, String adHocToData) throws ScriptEngineException;

	/**
	 * TODO This method is sensitive please make it private...
	 * @param pi
	 * @throws ScriptEngineException
	 */
	abstract public void deleteProcessInstanceAndCacheEntry(ProcessInstance pi) throws ScriptEngineException;
}
