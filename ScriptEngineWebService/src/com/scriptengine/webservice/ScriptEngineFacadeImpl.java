package com.scriptengine.webservice;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletContextEvent;

import org.ow2.bonita.facade.runtime.ProcessInstance;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.script.workflow.utils.TimeUtil;

/**
 * Impl of ScriptEngineFacade
 * @author Shirish singh
 * 
 */
@WebService(endpointInterface = "com.scriptengine.webservice.ScriptEngineFacade")
public class ScriptEngineFacadeImpl implements ScriptEngineFacade {

	public static String realPath = null;

	//private static WorkFlowService workflowService=null;
	
	private static List<String> adHocList=null;
	
	private final String AD_HOC="AD_HOC";

	protected final static Logger LOGGER = Logger.getLogger(ScriptEngineFacade.class.getName());
	
	
	
	public String startScriptService(ServletContextEvent servletContextEvent) {
		try{
			WorkFlowHelper.setServletContext(servletContextEvent.getServletContext());
			//LOGGER.log(Level.WARNING,"check path of BAR and JAAS File while deploying on Server...");
			realPath = servletContextEvent.getServletContext().getRealPath("/WEB-INF").replace("\\","/"); 
		//	LOGGER.log(Level.INFO,"Setting Context Path to:"+realPath);
		//	LOGGER.log(Level.INFO,"Starting Scripting Engine...");
			// WorkFlowHelper.getWorkFlowServiceInstance() = WorkFlowHelper.getWorkFlowServiceInstance();
			ProcessDetailsDTO processDetailsDTO =  WorkFlowHelper.getWorkFlowServiceInstance().beginProcess();
			//Set AD Hoc List variables
			adHocList=processDetailsDTO.getProcessVariables();
		//	LOGGER.log(Level.INFO,"Scripting Engine Started Successfully...");
			return processDetailsDTO.getProcessID();
			}catch(ScriptEngineException e){
				String error="ERROR[101]:ERROR OCCURED WHILE STARTING SCRIPTING ENGINE: "+e.getMessage();
				return error;
			}
	}

	
	@Override
	@WebMethod
	public String fetchCurrentScript(String id,String typeId) {
		if(isDataNull(id)){
			return "Incoming Data is Invalid";
		}
		if(isDataNull(typeId)){
			typeId=ScriptEngineConstants.DEFAULT;
		}

		if(!typeId.matches("[1-9]")){ //TODO ( What can be done here is fetch this number list from process diagram ..think)
			//TODO Refactor , make it configurable or handle validation in other way..
			return "Provided Type ID Doesnt Exist, please send typeID in range [1-9]";
		}
		
		try{
			 WorkFlowHelper.getWorkFlowServiceInstance().login(); //TODO Not Good here
			
			IncomingDataDTO incomingDataDTO=createIncomingDTOObject(id,typeId,TimeUtil.getUnixTimeStamp());
			//Fetch ProcessInstance
			ProcessInstance processInstance= WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			//check if processid is present in map cache if yes get process instance id for the same else create process instance id..
			TaskDetailsDTO currentTaskDetailsDTO =  WorkFlowHelper.getWorkFlowServiceInstance().fetchTaskDetails(processInstance);
			/**TODO BELOW LINE CODE SHOULD BE REFACTOR **/
//			if(typeId.equals(ScriptEngineConstants.LIST_DEBTOR_WITH_CB)){
//				//Below line will submit outcome for List with Credit bureau. This will remove List with cb from cache.. [REFACTOR THIS]
//				submitLineItem(id,typeId,"REGISTRATION_SUCCESSFUL");
//			}
		return currentTaskDetailsDTO.getTaskName();
		}catch(Exception e){
			String error="ERROR[102]:ERROR OCCURED WHILE FETCHING CURRENT SCRIPT: "+e.getMessage();
			return error;
		}
	}
	

	@Override
    @WebMethod
    public String[] fetchPossibleOutcomeList(String processID,String typeId) {
		if(isDataNull(processID)){
			return new String[]{"Incoming Data is Invalid:"+processID};
		}
		if(isDataNull(typeId)){
			return new String[]{"Type ID is Invalid:"+typeId};
		}
		if(isProcessIDInCorrect(processID,typeId)){ //TODO
			return new String[]{"Process ID is Invalid:"+processID+typeId};
		}
    	try{
    		 WorkFlowHelper.getWorkFlowServiceInstance().login();
    		
    		IncomingDataDTO incomingDataDTO=createIncomingDTOObject(processID,typeId,TimeUtil.getUnixTimeStamp());
    		
    		//Fetch ProcessInstance
			ProcessInstance processInstance= WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			
        List<String> listContact =  WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
        listContact=findAndAddAdHocVariables(listContact);
         return  listContact.toArray(new String[listContact.size()]);
    	}catch(Exception exception){
    		String error[]=new String[]{"ERROR[103]:ERROR OCCURED WHILE FETCHING POSSIBLE OUTCOMES: "+exception.getMessage()};
			return error;
    	}
    }

	@Override
	@WebMethod
	public String submitLineItem(String processID, String typeId ,String inputData) {
		//TODO Validation process for fetch/submit
		if(isDataNull(processID,inputData)){
			return "Incoming Data is Invalid:"+processID+" and "+inputData; //SB
		}
		if(isProcessIDInCorrect(processID,typeId)){
			return "Process ID is Invalid:"+processID+typeId; //SB
		}
		if(isDataNull(typeId)){
			return "Type Id is Invalid:"+typeId;
		}
		if(!Arrays.asList(fetchPossibleOutcomeList(processID,typeId)).contains(inputData)){
			return "Input Data is Invalid:"+inputData;
		}
		String adHoc=null; 
		String result="Failed";
		   try{
			   WorkFlowHelper.getWorkFlowServiceInstance().login();
			   
	    		IncomingDataDTO incomingDataDTO=createIncomingDTOObject(processID,typeId,TimeUtil.getUnixTimeStamp());
	    		
			 //Fetch ProcessInstance
				ProcessInstance processInstance= WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
				
		//Check for adHocData
		if(getAdHocList().contains(inputData)){
			List<String> previousOutcomes=  WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
			if(previousOutcomes.contains(AD_HOC)){
				adHoc=inputData;
				inputData=AD_HOC;
				
			}
		}
		//////////Execute Task
	    	 if(adHoc!=null){
	    			//AD HOC SCENARIO
	    		 WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance,inputData, adHoc); //completes current task and based on outcome move to next task. 
	 	           return result="Success";
	    	 }
	        	//REGULAR SCENARIO
	    	 WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance,inputData,null); //completes current task and based on outcome move to next task.
	    		 result="Success";
	     }catch (Exception e){
	    	 LOGGER.log(Level.SEVERE,"ERROR[104]: ERROR OCCURED WHILE SUBMITTING LINE ITEM: " + e.getMessage());
	     }
	     return result;
	}

	private boolean isProcessIDInCorrect(String processID,String typeId) {
		IncomingDataDTO incomingDataDTO=createIncomingDTOObject(processID,typeId,TimeUtil.getUnixTimeStamp());
		return !WorkFlowHelper.getInMemoryCache().containsKey(incomingDataDTO);
	}


/**
 * TODO Work more on this....
 * @param listContact
 * @return
 */
	private List<String> findAndAddAdHocVariables(List<String> listContact) {
		if(listContact.contains(AD_HOC)){
			listContact.remove(AD_HOC);
			listContact.addAll(adHocList);
		}
		return listContact;
	}
	
	private List<String> getAdHocList() throws ScriptEngineException {
		return  WorkFlowHelper.getWorkFlowServiceInstance().fetchProcessDetails().getProcessVariables();
	}

    private boolean isDataNull(String ...args) {
    	for(int i=0;i<args.length;i++){
    		if(args[i]==null || args[i].equals("?")){
    			return true;
    		}
    	}
		return false;
	}
	
	@Override
	public String clean() {
		int size=WorkFlowHelper.getInMemoryCache().size();
		for (ProcessInstance pi:WorkFlowHelper.getInMemoryCache().values()){
			try {
				 WorkFlowHelper.getWorkFlowServiceInstance().login();
				 WorkFlowHelper.getWorkFlowServiceInstance().deleteProcessInstanceAndCacheEntry(pi);
			} catch (ScriptEngineException e) {
				e.printStackTrace();
				WorkFlowHelper.getInMemoryCache().clear(); //TODO This is safe removing of unwanted stuff from cache ..//TODO when cache is implemented this will be removed..
				//Need to delete all process as well over here ..think
				 WorkFlowHelper.getWorkFlowServiceInstance().cleanScriptEngine();
				return new StringBuilder().append("Script Engine:Exception while Cleaning: "+e.getErrorName()).toString();
				
			}
			catch(LoginException loginException){
				return ("Script Engine: LoginException while Cleaning: Please Try to Login.... ");
			}
		}
		return new StringBuilder().append("Script Engine:Total:[").append(size).append("]Cleared from cache.").toString();
	}
	
	/**
	 * 
	 * @param id
	 * @param typeId
	 * @param timeStamp
	 * @return
	 */
	private IncomingDataDTO createIncomingDTOObject(String id, String typeId,
			long timeStamp) {
		IncomingDataDTO dataDTO=new IncomingDataDTO();
		dataDTO.setProcessID(id);
		dataDTO.setTypeID(typeId);
		dataDTO.setTimeStamp(timeStamp);
		return dataDTO;
	}
	
}
