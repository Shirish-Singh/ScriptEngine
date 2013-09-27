package com.scriptengine.script.workflow.service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
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

import com.scriptengine.dto.IncomingDataDTO;
import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.helper.ScriptEngineHelper;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;

/**
 * Work Flow Service To Service Scripting Engine
 * This Service basically talks to bpm engine.
 * 
 * @author Shirish singh
 * Date: 2013/03/25
 * Time: 9:24 AM
 * 
 */
public abstract class WorkFlowService {

	//TODO: enhancement may be in next phase...
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
	//USe AOP for logging , Login purpose
	//Move methods From  facade layer to helper
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

	private Map<String,ProcessDefinition> processDefinitions = null;
//	private ProcessInstanceUUID processInstanceUUID=null;

	/**
	 * Function to begin process. Bar File is Deployed. Path's are set. 
	 * @return ProcessDetailsDTO
	 * @throws ScriptEngineException 
	 */
	public final void beginProcess() throws ScriptEngineException {

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
			//ProcessDefinition processDefinition = deployBar();
			Map<String, ProcessDefinition> processDefinitionMap = deployBar();
			//set process definition
			//setProcessDefinition(processDefinition);
			setProcessDefinitions(processDefinitionMap);
			//set Process Definition ServletContext
			setProcessDefinitionInServletContext(processDefinitionMap);
			//return WorkFlowHelper.constructProcessDetailsDTO(getProcessDefinition(), null);
		} catch (Exception exception) {
			LOGGER.log(Level.SEVERE,"Script Engine : Exception in beginProcess:"+exception.getMessage());
			exception.printStackTrace();
			throw new ScriptEngineException("BEGIN_PROCESS",exception.getMessage(),exception.getCause());
		}
	}

	/**
	 * Login to Bpm
	 * @throws javax.security.auth.login.LoginException
	 */
	public void login() throws LoginException {
		LOGGER.log(Level.INFO,"Script Engine: Login In Process...");
		LoginContext loginContext = new LoginContext(AUTHENTICATE_TYPE, new SimpleCallbackHandler(LOGIN, PASSWORD));
		loginContext.login();
		LOGGER.log(Level.INFO,"Script Engine: Login Succesfull");
	}
	
	/**
	 * Set Process Definition in servlet context
	 * @param processDefinition
	 */
	private void setProcessDefinitionInServletContext(
			Map<String, ProcessDefinition> processDefinitions) {
		WorkFlowHelper.getServletContext().setAttribute(WorkFlowHelper.PROCESS_DEFINATION,processDefinitions);
	}


	/**
	 * Clean's cache and deletes all processes.
	 * @return boolean
	 */
	public boolean cleanScriptEngine() {
		try {
			LOGGER.log(Level.INFO,"Script Engine: Clean In Process");
			managementAPI.deleteAllProcesses();
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Clean- Failed : Script Engine Exception in clean Script Engine ");
			e.printStackTrace();
			return false;
		}finally{
			// Clear cache
			WorkFlowHelper.getInMemoryCache().clear();
		}
	}

	/**
	 * Function to Deploy Bar
	 * @return
	 * @throws Exception
	 */
	protected Map<String, ProcessDefinition> deployBar() throws Exception {
		LOGGER.log(Level.INFO,"Script Engine: Deploying Bar");
		
		File barDir = new File(ScriptEngineHelper.getInstance().getBarPath());
		
		File[] files = barDir.listFiles(new FilenameFilter() {			
											@Override
											public boolean accept(File dir, String fileName) {
												return fileName.endsWith(".bar");
											}
									    });
		
		Map<String, ProcessDefinition> processDefinitionMap = new HashMap<String, ProcessDefinition>();
		//deploy the bar files
		for(File barFile : files){
			System.out.println(" \n\n \t\t  BarFile : " + barFile + " \n\n");
			BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(barFile);
			ProcessDefinition processDefinition = managementAPI.deploy(businessArchive);
			System.out.println(" Deployed Process  :  " + processDefinition.getName() + "\n\n ");
			processDefinitionMap.put(processDefinition.getName(), processDefinition);
		}
		return processDefinitionMap;
		
	}
	
	/**
	 * Function to return associated Process Instance for the passed incoming Data DTO
	 * @param incomingDataDTO
	 * @return processInstance
	 * @throws Exception
	 */
	public ProcessInstance getProcessInstance(IncomingDataDTO incomingDataDTO) throws Exception {
		//Check in cache
		if(WorkFlowHelper.getInMemoryCache().containsKey(incomingDataDTO)){
			return WorkFlowHelper.getInMemoryCache().get(incomingDataDTO);
		}
		//Not present in cache create one and initialise
		//This Code is to initialise Script Engine and take it to State depending on Type Id.
		//ProcessDefinition definition=(ProcessDefinition) WorkFlowHelper.getServletContext().getAttribute(WorkFlowHelper.PROCESS_DEFINATION);
		ProcessDefinition definition = null;
		System.out.println("incomingDataDTO.getTypeId()  " + incomingDataDTO.getTypeId());
			
		definition = getProcessDefinitions().get(incomingDataDTO.getTypeId());
		
		LOGGER.log(Level.INFO," definition : " + definition);
		
		if(definition != null){
			System.out.println(" definition : " + definition.getUUID());
			Map<IncomingDataDTO,ProcessInstance> map = WorkFlowHelper.getInMemoryCache();
			synchronized (this) {
				ProcessInstanceUUID processInstanceUUID = runtimeAPI.instantiateProcess(definition.getUUID());
				System.out.println(" processInstanceUUID : " + processInstanceUUID);
				ProcessInstance processInstance = queryRuntimeAPI.getProcessInstance(processInstanceUUID);
				System.out.println(" processInstance : " + processInstance);
				map.put(incomingDataDTO, processInstance);
				WorkFlowHelper.setInMemoryCache(map);
				//This is basically to initialise
				//executeTask(processInstance,incomingDataDTO.getTypeId(),null);
				return processInstance;
			}
		} 
		else {
			throw new ScriptEngineException("Invalid Process Type", "Scripting Engine Process Definition Not Found");
		}
	}

	/**
	 * instantiate Process
	 * @param processDefinition
	 * @return ProcessInstanceUUID
	 * @throws Exception
	 */
	protected ProcessInstanceUUID instantiateProcess(ProcessDefinition processDefinition) throws Exception {
		return runtimeAPI.instantiateProcess(processDefinition.getUUID());
	}

//	/**
//	 * getProcessInstanceUUID
//	 *
//	 * @return ProcessInstanceUUID
//	 */
//	public ProcessInstanceUUID getProcessInstanceUUID() {
//		return processInstanceUUID;
//	}

	/**
	 * setProcessDefinition
	 *
	 * @param processDefinition
	 */
	private void setProcessDefinitions(Map<String,ProcessDefinition> processDefinitions) {
		this.processDefinitions = processDefinitions;
	}

	/**
	 * getProcessDefinition
	 *
	 * @return ProcessDefinition
	 */
	public Map<String,ProcessDefinition> getProcessDefinitions() {
		return processDefinitions;
	}

	/**
	 * Function to fetch the Current Ready/Active Task's variables list.
	 *
	 * @param processInstance
	 * @return list of variables for current task.
	 * @throws ScriptEngineException 
	 */

	abstract public List<String> fetchCurrentTaskVariableValues(ProcessInstance processInstance) throws ScriptEngineException;

	/**
	 * Fetch TaskDetails
	 * @param processInstance
	 * @return TaskDetailsDTO
	 * @throws ScriptEngineException 
	 * @throws InstanceNotFoundException 
	 * @throws Exception 
	 */
	abstract public TaskDetailsDTO fetchTaskDetails(final ProcessInstance processInstance) throws ScriptEngineException, InstanceNotFoundException, Exception; 

	/**
	 * Fetch ProcessDetails
	 * @return ProcessDetailsDTO
	 * @throws ScriptEngineException 
	 */
	abstract public ProcessDetailsDTO fetchProcessDetails(String processName) throws ScriptEngineException;

	/**
	 * Function to Execute Task
	 * @param processInstance
	 * @param inputData
	 * @param adHocToData
	 * @throws ScriptEngineException 
	 */
	abstract public void executeTask(ProcessInstance processInstance, String inputData, String adHocToData) throws ScriptEngineException;

	/**
	 * Function to clear cache and process Instance associated with it.
	 * @param processInstance
	 * @throws ScriptEngineException
	 */
	//TODO This method should be private...
	abstract public void deleteProcessInstanceAndCacheEntry(ProcessInstance processInstance) throws ScriptEngineException;
}
