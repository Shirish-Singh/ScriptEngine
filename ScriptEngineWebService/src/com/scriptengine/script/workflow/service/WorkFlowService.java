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
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.webservice.ScriptEngineFacadeImpl;

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
	//8. Bind WorkFlow Object with Process ID in WS .. Maintain state of client. (Can do this also Pass process ID and get Process Definition ..think ..)
	//9.Re Design Architecture - current architecture dosent cater for configurable process... can check reliability of current by performing performance testing specially memory check...
	//10.web service security
	//11. Process Instance state is set to null at the moment , need to deactivate or change the status to inactive or something for process Instance. 
	//12. REmove collection related code and make Script Engine Generic ...Big Task..think...
	
	//TODO
	//Take END and Start State from Process variable
	//Cache mechanism [Think * Think]
	//*If in case of Exception of Severe level restart server clean journal automatically.. [Need to get in depth knowledge of Bonita run time in this case ).
	//Can Set cache clean schedule time via web service request ..[do able but risky].. [keep cache mechanism stick to system and not exposed... if possible..or can be done configurable but with validation checking..[think]]
	
	protected final static Logger LOGGER = Logger.getLogger(WorkFlowService.class.getName());
	
	
	
    //Configuration
    private static final String LOGIN = "admin";
    private static final String PASSWORD = "bpm";
    private static final String AUTHENTICATE_TYPE = "BonitaAuth";
    
    private static final String BAR_FILE_PATH = ScriptEngineFacadeImpl.realPath+"/resources/configuration/ScriptEngine.bar";
    
    private static final String JAAS_FILE_PATH = ScriptEngineFacadeImpl.realPath+"/resources/configuration/jaas-standard.cfg";
    
    private static final String BONITA_HOME_PATH=ScriptEngineFacadeImpl.realPath+"/resources/bonita";
    
    static {
        System.setProperty(BonitaConstants.JAAS_PROPERTY, JAAS_FILE_PATH);
        System.setProperty(BonitaConstants.HOME,BONITA_HOME_PATH);
    }
    
    //Enhancement : Use Service Locater for below ... Add wrapper and ..runtime Exception if any.. 
    protected final ManagementAPI managementAPI = AccessorUtil.getManagementAPI();
    protected final RuntimeAPI runtimeAPI = AccessorUtil.getRuntimeAPI();
    protected final QueryRuntimeAPI queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
    protected final QueryDefinitionAPI queryDefinationAPI = AccessorUtil.getQueryDefinitionAPI();
    //Class level objects
    private ProcessDefinition processDefinition;
    private ProcessInstanceUUID processInstanceUUID;
    

    /**
     * beginProcess function
     *
     * @return ProcessDetailsDTO
     * @throws ScriptEngineException 
     */
    public final ProcessDetailsDTO beginProcess() throws ScriptEngineException {

    	//ContactScriptTempalteDTO fetchCurrentScript(String processID) and String submitLineITem(processID,outcome)
        try {
        	//create a cache
        	WorkFlowHelper.setCacheMap(new ConcurrentHashMap<String, ProcessInstance>());
        	//Get processInstance from  query runtime api ... match process instance Nb .... once you get ProcessIstace get TaskInstances from getTasks. 
        	LOGGER.log(Level.INFO,"Script Engine: Begin Process");
        	LOGGER.log(Level.INFO,"BAR FILE IS DEPLOYED AT:"+BAR_FILE_PATH);
        	LOGGER.log(Level.INFO,"BONITA HOME IS AT:"+BONITA_HOME_PATH);
        	LOGGER.log(Level.INFO,"JAAS IS AT:"+JAAS_FILE_PATH);
            //login
            login(); //One Time Operation
            //clean Work Flow Engine
            cleanWorkFlow(); //One Time Operation (Restart Process)
            //deploy
            ProcessDefinition processDefinition = deployBar();
            //set process definition
            setProcessDefinition(processDefinition);
            //set Process Definition ServletContext
            setProcessDefinitionInServletContext(processDefinition);
            //instantiate process
            ProcessInstanceUUID processInstanceUUID1 = instantiateProcess(processDefinition); //Multiple times operations ...
            //set ProcessInstanceUUID
            setProcessInstanceUUID(processInstanceUUID1);
            //convert to process details dto
            return WorkFlowHelper.convertToDTO(getProcessDefinition(), null);
        } catch (Exception exception) {
        	LOGGER.log(Level.SEVERE,"++++++++++++Script Engine Exception+++++++++++:"+exception.getMessage());
            exception.printStackTrace();
            //Throw exception
            //throws InstanceNotFoundException,InvalidLoginException, ClassNotFoundException, DeploymentException, ProcessNotFoundException, IOException
            throw new ScriptEngineException("BEGIN_PROCESS",exception.getMessage(),exception.getCause());
        }
    }

    private void setProcessDefinitionInServletContext(
			ProcessDefinition processDefinition) {
    	WorkFlowHelper.getServletContext().setAttribute(WorkFlowHelper.PROCESS_DEFINATION,processDefinition);
	}

	/**
     * login
     *
     * @throws javax.security.auth.login.LoginException
     */
    public void login() throws LoginException {
    	LOGGER.log(Level.INFO,"Script Engine: Login");
        LoginContext loginContext = new LoginContext(AUTHENTICATE_TYPE, new SimpleCallbackHandler(LOGIN, PASSWORD));
        loginContext.login();
    }

    /**
     * TODO: Along with cleaning cache this method should also be called by cache mechanism which will delete all processes [IMP]
     * TODO:"IN PRODUCTION YOU SHOULD NOT USE THIS METHOD" , NEED A SINGLETON TO HANDLE THIS. [THINK]
     * cleanWorkFlow
     */
    public String cleanWorkFlow() {
        try {
        	LOGGER.log(Level.INFO,"Script Engine: Clean WorkFlow Process");
            managementAPI.deleteAllProcesses();    //TODO:  R and D
            return "Clean- Success";
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE,"Script Engine Exception in Clean WorkFlow Process");
            e.getMessage();
            return "Clean- Failed";
        }finally{
        	 WorkFlowHelper.getCacheMap().clear();
        }
    }

    /**
     * deployBar
     *
     * @return
     * @throws Exception
     */
    protected ProcessDefinition deployBar() throws Exception {
    	LOGGER.log(Level.INFO,"Script Engine: Deploying Bar...");
        //deploy the bar file
        File barFile = new File(BAR_FILE_PATH);
        BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(barFile);
        return managementAPI.deploy(businessArchive);
    }

    /**
     * instantiateProcess
     *
     * @param processDefinition
     * @return ProcessInstanceUUID
     * @throws Exception
     */
    protected ProcessInstanceUUID instantiateProcess(ProcessDefinition processDefinition) throws Exception {
        return runtimeAPI.instantiateProcess(processDefinition.getUUID());
    }

    /**
     * setProcessInstanceUUID
     *
     * @param processInstanceUUID
     */
    private void setProcessInstanceUUID(ProcessInstanceUUID processInstanceUUID) {
        this.processInstanceUUID = processInstanceUUID;
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
     * @param processID
     * @return
     * @throws Exception 
     */
    public abstract List<String> fetchCurrentTaskVariableValues(String processID) throws ScriptEngineException;

    abstract public List<String> fetchCurrentTaskVariableValues(ProcessInstance processInstance) throws ScriptEngineException;
    /**
     * fetchTaskDetailsDTO
     *
     * @param processID
     * @return
     * @throws ScriptEngineException 
     */
    @Deprecated
    abstract public TaskDetailsDTO fetchTaskDetails(String processID) throws ScriptEngineException;
    
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
    @Deprecated
    abstract public void executeTask(final String processID, String inputData, String adHocToData) throws ScriptEngineException;

    /**
     * executeTask
     *
     * @param processID
     * @param inputData
     * @param adHocToData
     * @throws Exception 
     */
    abstract public void executeTask(ProcessInstance processInstance, String inputData, String adHocToData) throws ScriptEngineException;

    //TODO abstract void deleteProcess(String processID) throws ProcessNotFoundException, UndeletableInstanceException
    	
	public ProcessInstance getProcessInstance(String processID,String typeId) throws Exception {
		processID=WorkFlowHelper.getCombinedProcessIdWithTypeId(processID, typeId);
		if(WorkFlowHelper.getCacheMap().containsKey(processID)){
			return WorkFlowHelper.getCacheMap().get(processID);
		}
		ProcessDefinition definition=(ProcessDefinition) WorkFlowHelper.getServletContext().getAttribute(WorkFlowHelper.PROCESS_DEFINATION);
		ProcessInstanceUUID processInstanceUUID=runtimeAPI.instantiateProcess(definition.getUUID());
		ProcessInstance processInstance=queryRuntimeAPI.getProcessInstance(processInstanceUUID);
		Map<String,ProcessInstance> map=WorkFlowHelper.getCacheMap();
		map.put(processID, processInstance);
		WorkFlowHelper.setCacheMap(map);
		//This is basically to initialize
		executeTask(processInstance,typeId,null);
		return processInstance;
	}
	
	@Deprecated
	public ProcessInstance getProcessInstance(String processID) throws Exception {
		return getProcessInstance(processID,null);
	}
	
	/**
	 * TODO This method is sensitive please make it private...
	 * @param pi
	 * @throws ScriptEngineException
	 */
	abstract public void deleteProcess(ProcessInstance pi) throws ScriptEngineException;
}
