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
import org.ow2.bonita.facade.exception.UndeletableInstanceException;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.dto.IncomingDataDTO;
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
	 * @throws InstanceNotFoundException 
	 * @throws Exception 
	 */
	@Override
	public TaskDetailsDTO fetchTaskDetails(final ProcessInstance processInstance) throws Exception {
		Set<ActivityInstance> activityInstances=queryRuntimeAPI.getActivityInstances(processInstance.getProcessInstanceUUID());
		ActivityInstance readyActivityInstance=null;
		for(ActivityInstance activityInstance:activityInstances){
			if(activityInstance.getState()==ActivityState.READY){
				readyActivityInstance=activityInstance;
				break;
			}
		}
		try {
			return WorkFlowHelper.constructTaskDetailsDTO(readyActivityInstance, null);
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
			return WorkFlowHelper.constructProcessDetailsDTO((ProcessDefinition) WorkFlowHelper.getServletContext().getAttribute(WorkFlowHelper.PROCESS_DEFINATION), null);
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
	 * @throws InstanceNotFoundException 
	 */
	public ActivityInstance getCurrentActivity(ProcessInstance processInstance) throws ScriptEngineException, InstanceNotFoundException {
		Collection<TaskInstance> tasks=queryRuntimeAPI.getTasks(processInstance.getProcessInstanceUUID());
		//Check there should be only one task in active state
		for(TaskInstance instance:tasks){
			if(instance.getState()==ActivityState.READY || instance.getState()==ActivityState.EXECUTING)
				return instance;
		}
		LOGGER.log(Level.SEVERE,"No ActivityInstance in Ready State...");
		throw new ScriptEngineException("ERROR_GET_EXECUTE_TASK","No ActivityInstance in Ready State...",null);
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
			final ActivityInstanceUUID activityInstanceUUID=getCurrentActivity(processInstance).getTask().getUUID();
			runtimeAPI.setActivityInstanceVariable(activityInstanceUUID, OUTCOME_SELECTION_TASK_VARIABLE, inputData);  //TODO:make it configurable
			if(adHocToData != null){
				runtimeAPI.setProcessInstanceVariable(processInstance.getProcessInstanceUUID(), ROUTE_TO_SCRIPT_PROCESS_VARIABLE, adHocToData);
				runtimeAPI.startTask(activityInstanceUUID, true);
				runtimeAPI.finishTask(activityInstanceUUID, true);
			}else{
				runtimeAPI.startTask(activityInstanceUUID, true);
				runtimeAPI.finishTask(activityInstanceUUID, true);
			}
			if(inputData.equals(END) || (adHocToData!=null?adHocToData.equals(END):false)){
				deleteProcessInstanceAndCacheEntry(processInstance);
			}
			if(WorkFlowHelper.isLastInputData(inputData)){
				deleteProcessInstanceAndCacheEntry(processInstance);
			}
			LOGGER.log(Level.INFO,"Script Engine: Execute Task:["+inputData+"]");
		}catch(Exception exception){
			LOGGER.log(Level.SEVERE,"Script Engine: executeTask: "+exception.getMessage());
			exception.printStackTrace();
			throw new ScriptEngineException("ERROR_EXECUTE_TASK",exception.getMessage(),exception.getCause());
		}
	}

	
	/**
	 * Delete Process Instances
	 * @param processID
	 * @throws ScriptEngineException 
	 */
	//TODO make this method private 
	public void deleteProcessInstanceAndCacheEntry(ProcessInstance processInstance) throws ScriptEngineException{
		try{ 
			IncomingDataDTO pi=null;
			for(Map.Entry<IncomingDataDTO, ProcessInstance> entry: WorkFlowHelper.getInMemoryCache().entrySet()){
				if(entry.getValue().equals(processInstance)){
					pi=entry.getKey();
				}
			}
			if(pi==null){
				return;
			}
			//This is important ..need wrapping ..think 
			runtimeAPI.deleteProcessInstance(processInstance.getProcessInstanceUUID());
			WorkFlowHelper.getInMemoryCache().remove(pi);
			LOGGER.log(Level.INFO,"Script Engine:Deleting Process"+processInstance);
			LOGGER.log(Level.INFO,"Check Cache Dump: Current Size:"+WorkFlowHelper.getInMemoryCache().size());
			processInstance=null;
		}catch(UndeletableInstanceException  undeletableInstanceException){
			LOGGER.log(Level.SEVERE,"Script Engine: deleteProcess: Current processInstance has a parent Instance Active so cannot delete current process Instance"+undeletableInstanceException.getMessage());
			undeletableInstanceException.printStackTrace();
			throw new ScriptEngineException("ERROR_DELETE_PROCESS_DUE_TO_UNDELETABLE_INSTANCE_EXCEPTION",undeletableInstanceException.getMessage(),undeletableInstanceException.getCause());
		}
		catch(Exception exception){
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

