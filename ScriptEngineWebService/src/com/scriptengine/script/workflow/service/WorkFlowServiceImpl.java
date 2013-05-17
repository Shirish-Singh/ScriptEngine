package com.scriptengine.script.workflow.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;

/**
 * User: Shirish Singh
 * Date: 2013/03/22
 * Time: 12:19 PM
 */
public class WorkFlowServiceImpl extends WorkFlowService {

    private final static String ROUTE_TO_SCRIPT_PROCESS_VARIABLE = "processVariable";
    private final static String OUTCOME_SELECTION_TASK_VARIABLE = "outcomeList";
    private final static String END="END_STATE";


    /**
     * Get task details dto, basically returns active or ready state task.
     *
     * @param processID
     * @return TaskDetailsDTO
     * @throws Exception 
     */
    @Override
    @Deprecated
    public TaskDetailsDTO fetchTaskDetails(final String processID) throws ScriptEngineException {
        try {
            return WorkFlowHelper.convertToDTO(getCurrentActivity(processID), null);
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE,"Script Engine: [Script might be in END State..."+e.getMessage());
            e.printStackTrace();
            throw new ScriptEngineException("ERROR_FETCHING_TASK_DETAILS",e.getMessage(),e.getCause());
        }
    }
    
    /**
     * Get task details dto, basically returns active or ready state task.
     *
     * @param processID
     * @return TaskDetailsDTO
     * @throws InstanceNotFoundException 
     * @throws Exception 
     */
    @Override
    public TaskDetailsDTO fetchTaskDetails(final ProcessInstance processInstance) throws Exception {
    	Set<ActivityInstance> activityInstances=queryRuntimeAPI.getActivityInstances(processInstance.getProcessInstanceUUID());
    	ActivityInstance readyActivityInstance=null;
    	//Set<ActivityInstance> activityInstances=processInstance.getActivities();
    	for(ActivityInstance activityInstance:activityInstances){
    		if(activityInstance.getState()==ActivityState.READY){
    			readyActivityInstance=activityInstance;
    			break;
    		}
    	}
    	try {
            return WorkFlowHelper.convertToDTO(readyActivityInstance, null);
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE,"Script Engine: [Script might be in END State..."+e.getMessage());
            e.printStackTrace();
            throw new ScriptEngineException("ERROR_FETCHING_TASK_DETAILS",e.getMessage(),e.getCause());
        }
    }
    

    /**
     * Get task details dto, basically returns active or ready state task.
     *
     * @param processID
     * @return ProcessDetailsDTO
     * @throws ScriptEngineException 
     */
    @Override
    public ProcessDetailsDTO fetchProcessDetails() throws ScriptEngineException {
        try {
            return WorkFlowHelper.convertToDTO((ProcessDefinition) WorkFlowHelper.getServletContext().getAttribute(WorkFlowHelper.PROCESS_DEFINATION), null);
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE,"Script Engine: Something Wrong with Process: "+e.getMessage());
            e.printStackTrace();
            throw new ScriptEngineException("ERROR_FETCHING_PROCESS_DETAILS",e.getMessage(),e.getCause());
        }
    }

    /**
     * Returns Ready state task.
     *
     * @return ActivityInstance
     * @throws ScriptEngineException 
     */
    @Deprecated
    private ActivityInstance getCurrentActivity(String processID) throws ScriptEngineException {
        ActivityInstance currentActivity = null;
        //Collection<TaskInstance> tasks =queryRuntimeAPI.getTasks(processID);
        Collection<TaskInstance> tasks = queryRuntimeAPI.getTaskList(ActivityState.READY); //check there should be only one task in active state
        if (tasks.size() > 1) {
        	LOGGER.log(Level.SEVERE,"Script Engine: Two Task's With Ready State Not Allowed: "+tasks.toString());
        	//throw new ScriptEngineException("ERROR_CURRENT_ACTIVITY","Two Task With Ready STate Not Allowed");
        	//TODO throw exception two task cannot be in ready state.
        }
        currentActivity = tasks.iterator().next();
        return currentActivity;
    }
    
    /**
     * Returns Ready state task.
     *
     * @return ActivityInstance
     * @throws ScriptEngineException 
     * @throws InstanceNotFoundException 
     */
    public ActivityInstance getCurrentActivity(ProcessInstance processInstance) throws ScriptEngineException, InstanceNotFoundException {
    	 Collection<TaskInstance> tasks=queryRuntimeAPI.getTasks(processInstance.getProcessInstanceUUID());
       // Collection<TaskInstance> tasks = processInstance.getTasks();// queryRuntimeAPI.getTaskList(ActivityState.READY); //check there should be only one task in active state
        for(TaskInstance instance:tasks){
        	 if(instance.getState()==ActivityState.READY || instance.getState()==ActivityState.EXECUTING)
        	        return instance;
        }
        LOGGER.log(Level.SEVERE,"No ActivityInstance in Ready State...");
       return null;
    }

    /**
     * Basic Execution of task is perform here ,based on
     * task variable value or process variable value and conditions that are available, transition to next task takes place.
     *
     * @param processID
     * @param inputData
     * @param adHocToData
     * @throws ScriptEngineException 
     */
    @Deprecated
    public void executeTask(final String processID, final String inputData, final String adHocToData) throws ScriptEngineException {
    	try{
        if (adHocToData != null) {
            //TODO:get route To sCript and outcomeSelection and remove
            runtimeAPI.setProcessInstanceVariable(getProcessInstanceUUID(), ROUTE_TO_SCRIPT_PROCESS_VARIABLE, adHocToData);
        }
        runtimeAPI.setActivityInstanceVariable(getCurrentActivity(processID).getTask().getUUID(), OUTCOME_SELECTION_TASK_VARIABLE, inputData);  //TODO:make it configurable
        runtimeAPI.executeTask(getCurrentActivity(processID).getTask().getUUID(), true);
        if(inputData.equals(END) || (adHocToData!=null?adHocToData.equals(END):false)){
        	//deleteProcess(processID);
        }
        LOGGER.log(Level.INFO,"Script Engine: Execute Task");
        }catch(Exception exception){
        	LOGGER.log(Level.SEVERE,"Script Engine: executeTask: "+exception.getMessage());
    		exception.printStackTrace();
    		throw new ScriptEngineException("ERROR_EXECUTE_TASK",exception.getMessage(),exception.getCause());
        }
    }

    /**
     * Basic Execution of task is perform here ,based on
     * task variable value or process variable value and conditions that are available, transition to next task takes place.
     *
     * @param processID
     * @param inputData
     * @param adHocToData
     * @throws ScriptEngineException 
     */
    public void executeTask(final ProcessInstance processInstance, final String inputData, final String adHocToData) throws ScriptEngineException {
    	try{
        if (adHocToData != null) {
            runtimeAPI.setProcessInstanceVariable(getProcessInstanceUUID(), ROUTE_TO_SCRIPT_PROCESS_VARIABLE, adHocToData);
        }
        runtimeAPI.setVariable(getCurrentActivity(processInstance).getTask().getUUID(), OUTCOME_SELECTION_TASK_VARIABLE, inputData);  //TODO:make it configurable
        runtimeAPI.startTask(getCurrentActivity(processInstance).getTask().getUUID(), true);
        runtimeAPI.finishTask(getCurrentActivity(processInstance).getTask().getUUID(), true);
       // runtimeAPI.finishTask(getCurrentActivity(processInstance).getTask().getUUID(), true);
        if(inputData.equals(END) || (adHocToData!=null?adHocToData.equals(END):false)){ //TODO Please take this end state to LAST_INPUT_DATA
        	deleteProcess(processInstance);
        }
        if(isLastInputData(inputData)){
        	deleteProcess(processInstance);
        }
        LOGGER.log(Level.INFO,"Script Engine: Execute Task:["+inputData+"]");
        }catch(Exception exception){
        	LOGGER.log(Level.SEVERE,"Script Engine: executeTask: "+exception.getMessage());
    		exception.printStackTrace();
    		throw new ScriptEngineException("ERROR_EXECUTE_TASK",exception.getMessage(),exception.getCause());
        }
    }

    
    private boolean isLastInputData(String inputData) {
    	//Check
		if(WorkFlowHelper.getLastInputData().contains(inputData)){
		return true;
		}
		return false;
	}

	/**
     * Delete Process Instances
     * @param processID
     * @throws ScriptEngineException 
     */
    //TODO make this method private 
    public void deleteProcess(ProcessInstance pi) throws ScriptEngineException{
    	try{ 
    		String processID=null;
    		 for(Map.Entry<String, ProcessInstance> entry: WorkFlowHelper.getCacheMap().entrySet()){
    			 if(entry.getValue().equals(pi)){
    				 processID=entry.getKey();
    			 }
    		 }
    		 if(processID==null){
    			 return;
    		 }
    		 //This is important ..need wrapping ..think 
    		 runtimeAPI.deleteProcessInstance(pi.getProcessInstanceUUID());
    		 WorkFlowHelper.getCacheMap().remove(processID);
    		LOGGER.log(Level.INFO,"Script Engine:Deleting Process"+pi);
    		LOGGER.log(Level.WARNING,"Check Cache Dump: Current Size:"+WorkFlowHelper.getCacheMap().size());
    		 pi=null;
    	}catch(Exception exception){
    		LOGGER.log(Level.SEVERE,"Script Engine: deleteProcess: "+exception.getMessage());
    		exception.printStackTrace();
    		throw new ScriptEngineException("ERROR_DELETE_PROCESS",exception.getMessage(),exception.getCause());
    	}
    }
    /**
     * Get list of task variables available with the task.
     * (Note:For Scripting Engine this will return number of outcomes associated with the particular script/task/activity.)
     *
     * @param processID
     * @return
     * @throws Exception 
     */
    @Override
    @Deprecated
    public List<String> fetchCurrentTaskVariableValues(String processID) throws ScriptEngineException {
        try {
            List<String> variableValues = new ArrayList<String>();
            ActivityDefinition activityDefinition = queryDefinationAPI.getProcessActivity(getProcessDefinition().getUUID(), getCurrentActivity(processID).getActivityName());
            DataFieldDefinition dataFieldDefinition = activityDefinition.getDataFields().iterator().next();
            Set<String> dataFieldDefinitionEnumValues = dataFieldDefinition.getEnumerationValues();
            for (String dataFieldDefinitionValue : dataFieldDefinitionEnumValues) {
                variableValues.add(dataFieldDefinitionValue);
            }
            return variableValues;
        } catch (Exception exception) {
        	LOGGER.log(Level.SEVERE,"Script Engine: fetchCurrentTaskVariableValues: "+exception.getMessage());
        	exception.printStackTrace();
            throw new ScriptEngineException("ERROR_CURRENT_TASK_VARIABLE_VALUES",exception.getMessage(),exception.getCause());
        }
    }
    
    /**
     * Get list of task variables available with the task.
     * (Note:For Scripting Engine this will return number of outcomes associated with the particular script/task/activity.)
     *
     * @param processID
     * @return
     * @throws Exception 
     */
    @Override
    public List<String> fetchCurrentTaskVariableValues(ProcessInstance processInstance) throws ScriptEngineException {
        try {
            List<String> variableValues = new ArrayList<String>();
            ActivityDefinition activityDefinition = queryDefinationAPI.getProcessActivity(getProcessDefinition().getUUID(), getCurrentActivity(processInstance).getActivityName());
            DataFieldDefinition dataFieldDefinition = activityDefinition.getDataFields().iterator().next();
            Set<String> dataFieldDefinitionEnumValues = dataFieldDefinition.getEnumerationValues();
            for (String dataFieldDefinitionValue : dataFieldDefinitionEnumValues) {
                variableValues.add(dataFieldDefinitionValue);
            }
            LOGGER.log(Level.INFO,"Script Engine: fetchCurrentTaskVariableValues: "+variableValues);
            return variableValues;
        } catch (Exception exception) {
        	LOGGER.log(Level.SEVERE,"Script Engine: fetchCurrentTaskVariableValues: "+exception.getMessage());
        	exception.printStackTrace();
            throw new ScriptEngineException("ERROR_CURRENT_TASK_VARIABLE_VALUES",exception.getMessage(),exception.getCause());
        }
    }
}

//NOTE: Always put variables in state.. in diagram..for current architecture
