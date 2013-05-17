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

import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;


/**
 * User: Shirish Singh
 * Date: 3/30/13
 * Time: 12:33 AM
 */
public class WorkFlowHelper {
	
	//TODO //Need to use EHCache
	private static Map<String,ProcessInstance> cacheMap=null;
	
    //TODO: Fetch Below List from Process Context (processvariables2)
    //and Add End State to each LEaf Node of type 2,3,4
    private final static List<String> LAST_INPUT_DATA=new ArrayList<String>(24);
	
	private static ServletContext servletContext = null;
	
	public final static String PROCESS_DEFINATION="processDefination";
	public final static String PROCESS_VARIABLE="processVariable";
	
	
	//Need to use EHCache
    public static Map<String, ProcessInstance> getCacheMap() {
		return cacheMap;
	}

	public static void setCacheMap(Map<String, ProcessInstance> cacheMap) {
		WorkFlowHelper.cacheMap = cacheMap;
	}

	/**
     *  Construct processDetailsDTO using processDefinition.
     * @param processDefinition
     * @param processDetailsDTO
     * @return
     */
    public static ProcessDetailsDTO convertToDTO(ProcessDefinition processDefinition, ProcessDetailsDTO processDetailsDTO) {
        if (processDefinition == null) {
            return null;
        }
        if (processDetailsDTO == null) {
            processDetailsDTO = new ProcessDetailsDTO();
        }
        DataFieldDefinition dataFieldDefinition=  processDefinition.getDatafield(PROCESS_VARIABLE);
        Set<String> processVariableSet=dataFieldDefinition.getEnumerationValues();
        List<String> listProcessVariable=new ArrayList<String>();
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
    public static TaskDetailsDTO convertToDTO(ActivityInstance currentActivity, TaskDetailsDTO taskDetailsDTO) {

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

	public static void setServletContext(ServletContext servletContext) {
		WorkFlowHelper.servletContext=servletContext;
	}

	public static ServletContext getServletContext(){
		return servletContext;
	}

	/**
	 * Method will combine id and type id and send it to caller as process id as a whole.
	 * This helps in identifying processId as well as type id.
	 * @param id
	 * @param typeId
	 * @return
	 */
	public static String getCombinedProcessIdWithTypeId(String id,String typeId){
		 //TODO Not good Method and code, please simplify
		return id+typeId; 
	}
	/**
	 * Method to return leaf nodes which doesnt contain END state but are Last State. 
	 * @return List of Leaf End Node. List<String>
	 */
	public static List<String> getLastInputData() {
		
		if(LAST_INPUT_DATA.size() > 0){ //TODO NOT GOOD CODE SHOULD CHANGE
			return LAST_INPUT_DATA;
		}
//		//END_STATE
//		LAST_INPUT_DATA.add("END_STATE");
		//CLIENT_UNAVAILABLE
		LAST_INPUT_DATA.add("CALL_ON_SUGGESTED_TIME");
		LAST_INPUT_DATA.add("NO_SUGGESTED_TIME");
		//List with CB
		LAST_INPUT_DATA.add("REGISTRATION_SUCCESSFUL");
		LAST_INPUT_DATA.add("REGISTRATION_FAILED");
		//EMAIL 
    	LAST_INPUT_DATA.add("EMAIL_SENT");
    	//LETTER
    	LAST_INPUT_DATA.add("LETTER_SENT");
    	LAST_INPUT_DATA.add("LETTER_NOT_DELIVERED");
    	LAST_INPUT_DATA.add("LETTER_DELIVERED");
    	//VISIT DEBTOR
    	LAST_INPUT_DATA.add("VISIT_SUCCESSFUL");
    	LAST_INPUT_DATA.add("INVALID_ADDRESS");
    	LAST_INPUT_DATA.add("CLIENT_NOT_AT_ADDRESS");
    	LAST_INPUT_DATA.add("CLIENT_NOT_AT_HOME");
    	LAST_INPUT_DATA.add("VISIT_CLIENT_DECEASED");
    	LAST_INPUT_DATA.add("OTHER");
    	LAST_INPUT_DATA.add("VISITING_AGENT_DISPATCHED");
    	//SETTLE DEBT
    	LAST_INPUT_DATA.add("DEBT_SETTLED");
    	LAST_INPUT_DATA.add("DEBT_NOT_SETTLED");
    	//PRE-LEGAL
    	LAST_INPUT_DATA.add("DO_NOT_SUE_DEBTOR");
    	LAST_INPUT_DATA.add("PRE_LEGAL_SUE_DEBTOR");
    	LAST_INPUT_DATA.add("AWAITING_DECISION");
    	//SUE
    	LAST_INPUT_DATA.add("SUE_IN_PROGRESS");
    	LAST_INPUT_DATA.add("SUE_SUCCESSFUL");
    	LAST_INPUT_DATA.add("SUE_FAILED");
    	//SELL-WRITE OFF
    	LAST_INPUT_DATA.add("SELL_WRITE_IN_PROGRESS");
    	LAST_INPUT_DATA.add("SOLD");
    	LAST_INPUT_DATA.add("WRITE_OFF");
    	return LAST_INPUT_DATA;
	}
}
