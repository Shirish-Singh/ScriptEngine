package com.scriptengine.script.workflow.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.service.WorkFlowServiceImpl;

/**
 * User: Shirish Singh
 * Date: 3/30/13
 * Time: 12:33 AM
 */
public class WorkFlowHelper {

	private static Map<IncomingDataDTO,ProcessInstance> inMemoryCache=null;
	//TODO: Fetch Below List from Process Context (processvariables2)
	private final static List<String> LAST_STATE_DATA=new ArrayList<String>();
	private static ServletContext servletContext = null;
	public final static String PROCESS_DEFINATION="processDefination";
	public final static String PROCESS_VARIABLE="processVariable";
	private final static WorkFlowServiceImpl INSTANCE=new WorkFlowServiceImpl(); 

	/**
	 * getWorkFlowServiceInstance returns WorkFlowService Object
	 * @return
	 */
	public static WorkFlowServiceImpl getWorkFlowServiceInstance(){
		return INSTANCE;
	}

	/**
	 * Get Cache which Stores mapping (id and processInstance)
	 * @return
	 */
	public static Map<IncomingDataDTO, ProcessInstance> getInMemoryCache() {
		return inMemoryCache;
	}

	/**
	 * Set Cache which Stores mapping (id and processInstance)
	 * @param inMemoryCache
	 */
	public static void setInMemoryCache(Map<IncomingDataDTO, ProcessInstance> inMemoryCache) {
		WorkFlowHelper.inMemoryCache = inMemoryCache;
	}
	
	/**
	 * get Servlet Context
	 * @return
	 */
	public static ServletContext getServletContext(){
		return servletContext;
	}
	
	/**
	 * Set Servlet Context
	 * @param servletContext
	 */
	public static void setServletContext(ServletContext servletContext) {
		WorkFlowHelper.servletContext=servletContext;
	}

	/**
	 *Construct processDetailsDTO using processDefinition.
	 * @param processDefinition
	 * @param processDetailsDTO
	 * @return
	 */
	public static ProcessDetailsDTO constructProcessDetailsDTO(ProcessDefinition processDefinition, ProcessDetailsDTO processDetailsDTO) {
		if (processDefinition == null) {
			return null;
		}
		if (processDetailsDTO == null) {
			processDetailsDTO = new ProcessDetailsDTO();
		}
		List<String> listProcessVariable=new ArrayList<String>();
		DataFieldDefinition dataFieldDefinition=  processDefinition.getDatafield(PROCESS_VARIABLE);
		Set<String> processVariableSet=dataFieldDefinition.getEnumerationValues();
		processDetailsDTO.setProcessID(processDefinition.getUUID().getValue());
		for(String processVariable:processVariableSet){
			listProcessVariable.add(processVariable);
		}
		processDetailsDTO.setProcessVariables(listProcessVariable);
		return processDetailsDTO;
	}

	/**
	 * Construct taskDetailsDTO using ActivityInstance
	 * @param currentActivity
	 * @param taskDetailsDTO
	 * @return
	 */
	public static TaskDetailsDTO constructTaskDetailsDTO(ActivityInstance currentActivity, TaskDetailsDTO taskDetailsDTO) {
		if (currentActivity == null) {
			return null;
		}
		if (taskDetailsDTO == null) {
			taskDetailsDTO = new TaskDetailsDTO();
		}
		taskDetailsDTO.setTaskID(currentActivity.getActivityInstanceId());
		taskDetailsDTO.setTaskName(currentActivity.getActivityName());
		taskDetailsDTO.setTaskDescription(currentActivity.getActivityDescription());
		return taskDetailsDTO;
	}

	/**
	 * Returns true if the current selected outcome is last state.
	 * @param inputData
	 * @return
	 */
	public static boolean isLastInputData(String inputData) {
		//Check
		if(WorkFlowHelper.getLastStateList().contains(inputData)){
			return true;
		}
		return false;
	}
	
	/**
	 * Method to return leaf nodes which doesnt contain END state but are Last State. 
	 * @return List of Leaf End Node. List<String>
	 */
	private static List<String> getLastStateList() {

		if(LAST_STATE_DATA.size() > 0){ //TODO NOT GOOD CODE SHOULD CHANGE
			return LAST_STATE_DATA;
		}
		//List with CB
		LAST_STATE_DATA.add("REGISTRATION_SUCCESSFUL");
		LAST_STATE_DATA.add("REGISTRATION_FAILED");
		//EMAIL 
		LAST_STATE_DATA.add("EMAIL_SENT");
		//LETTER
		LAST_STATE_DATA.add("LETTER_SENT");
		LAST_STATE_DATA.add("LETTER_NOT_DELIVERED");
		LAST_STATE_DATA.add("LETTER_DELIVERED");
		//VISIT DEBTOR
		LAST_STATE_DATA.add("VISIT_SUCCESSFUL");
		LAST_STATE_DATA.add("INVALID_ADDRESS");
		LAST_STATE_DATA.add("CLIENT_NOT_AT_ADDRESS");
		LAST_STATE_DATA.add("CLIENT_NOT_AT_HOME");
		LAST_STATE_DATA.add("VISIT_CLIENT_DECEASED");
		LAST_STATE_DATA.add("OTHER");
		LAST_STATE_DATA.add("VISITING_AGENT_DISPATCHED");
		//SETTLE DEBT
		LAST_STATE_DATA.add("DEBT_SETTLED");
		LAST_STATE_DATA.add("DEBT_NOT_SETTLED");
		//PRE-LEGAL
		LAST_STATE_DATA.add("DO_NOT_SUE_DEBTOR");
		LAST_STATE_DATA.add("PRE_LEGAL_SUE_DEBTOR");
		LAST_STATE_DATA.add("AWAITING_DECISION");
		//SUE
		LAST_STATE_DATA.add("SUE_IN_PROGRESS");
		LAST_STATE_DATA.add("SUE_SUCCESSFUL");
		LAST_STATE_DATA.add("SUE_FAILED");
		//SELL-WRITE OFF
		LAST_STATE_DATA.add("SELL_WRITE_IN_PROGRESS");
		LAST_STATE_DATA.add("SOLD");
		LAST_STATE_DATA.add("WRITE_OFF");
		
		//Return
		return LAST_STATE_DATA;
	}
}
