package com.scriptengine.webservice;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletContextEvent;

import org.ow2.bonita.facade.runtime.ProcessInstance;

import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.helper.ScriptEngineHelper;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.script.workflow.utils.TimeUtil;

/**
 * Skeletal implementation of {@link ScriptEngineFacade}.
 * 
 * @author Shirish singh
 * @since 1.6
 */
@WebService(endpointInterface = "com.scriptengine.webservice.ScriptEngineFacade")
public class ScriptEngineFacadeImpl implements ScriptEngineFacade {

	protected final static Logger LOGGER = Logger.getLogger(ScriptEngineFacade.class.getName());

	/**
	 * AD_HOC is hard code variable in script engine process diagram.
	 */
	private final String AD_HOC = "AD_HOC";

	/**
	 * List containing Ad Hoc Values.
	 */
	private static List<String> adHocList = null;

	/**
	 * @see ScriptEngineFacade#startScriptService(ServletContextEvent)
	 */
	@Override
	public String startScriptService(ServletContextEvent servletContextEvent) {
		try {
			// Set Real Path
			ScriptEngineHelper.setRealPath(servletContextEvent.getServletContext().getRealPath("/WEB-INF").replace("\\", "/"));
			// Set Servlet Context
			WorkFlowHelper.setServletContext(servletContextEvent.getServletContext());
			ProcessDetailsDTO processDetailsDTO = WorkFlowHelper.getWorkFlowServiceInstance().beginProcess();
			// Set AD_HOC variables to AD_HOC List
			adHocList = processDetailsDTO.getProcessVariables();
			return processDetailsDTO.getProcessID();
		} catch (ScriptEngineException e) {
			return "ERROR[101]:ERROR OCCURED WHILE STARTING SCRIPTING ENGINE: "+ e.getMessage();
		}
	}

	/**
	 * @see ScriptEngineFacade#fetchCurrentScript(String, String)
	 */
	@Override
	@WebMethod
	public String fetchCurrentScript(String id, String typeId) {
		if (isDataNull(id)) {
			return "Incoming Data is Invalid";
		}
		if (isDataNull(typeId)) {
			typeId = ScriptEngineConstants.DEFAULT;
		}

		if (!typeId.matches("[1-9]")) { 
			// TODO ( What can be done here is fetch
			// this number list from process diagram
			// ..think)
			// TODO Refactor , make it configurable or handle validation in
			// other way..
			return "Provided Type ID Doesnt Exist, please send typeID in range [1-9]";
		}
		try {
			// Login to proceed
			WorkFlowHelper.getWorkFlowServiceInstance().login(); // TODO Not Good here
			// Create IncomingDataDTO
			IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, typeId, TimeUtil.getUnixTimeStamp());
			// Fetch ProcessInstance
			// Check if incomingDataDTO is present in cache if yes get process
			// instance for the same else create process instance and return it.
			ProcessInstance processInstance = WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			// Get Task Details DTO based on processInstance
			TaskDetailsDTO currentTaskDetailsDTO = WorkFlowHelper.getWorkFlowServiceInstance().fetchTaskDetails(processInstance);
			return currentTaskDetailsDTO.getTaskName();
		} catch (Exception e) {
			return "ERROR[102]:ERROR OCCURED WHILE FETCHING CURRENT SCRIPT: "+ e.getMessage();
		}
	}

	/**
	 * @see ScriptEngineFacade#fetchPossibleOutcomeList(String, String)
	 */
	@Override
	@WebMethod
	public String[] fetchPossibleOutcomeList(String id, String typeId) {
		if (isDataNull(id)) {
			return new String[] { "Incoming Data is Invalid:" + id };
		}
		if (isDataNull(typeId)) {
			return new String[] { "Type ID is Invalid:" + typeId };
		}
		if (isProcessIDInCorrect(id, typeId)) {
			return new String[] { "Process ID is Invalid:" + id + typeId };
		}
		try {
			// Login to proceed
			WorkFlowHelper.getWorkFlowServiceInstance().login();
			IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, typeId, TimeUtil.getUnixTimeStamp());
			// Fetch ProcessInstance
			ProcessInstance processInstance = WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			List<String> listContact = WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
			listContact = findAndAddAdHocVariables(listContact);
			return listContact.toArray(new String[listContact.size()]);
		} catch (Exception exception) {
			String error[] = new String[] { "ERROR[103]:ERROR OCCURED WHILE FETCHING POSSIBLE OUTCOMES: "+ exception.getMessage() };
			return error;
		}
	}

	/**
	 * @see ScriptEngineFacade#submitLineItem(String, String, String)
	 */
	@Override
	@WebMethod
	public String submitLineItem(String id, String typeId, String inputData) {
		// TODO Validation process for fetch/submit
		if (isDataNull(id, inputData)) {
			return "Incoming Data is Invalid:" + id + " and " + inputData; // SB
		}
		if (isProcessIDInCorrect(id, typeId)) {
			return "Process ID is Invalid:" + id + typeId; // SB
		}
		if (isDataNull(typeId)) {
			return "Type Id is Invalid:" + typeId;
		}
		if (!Arrays.asList(fetchPossibleOutcomeList(id, typeId)).contains(
				inputData)) {
			return "Input Data is Invalid:" + inputData;
		}
		String result = ScriptEngineConstants.FAILED;
		try {
			String adHoc = null;
			WorkFlowHelper.getWorkFlowServiceInstance().login();
			IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, typeId, TimeUtil.getUnixTimeStamp());
			// Fetch ProcessInstance
			ProcessInstance processInstance = WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			// Check for adHocData
			if (getAdHocList().contains(inputData)) {
				List<String> previousOutcomes = WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
				if (previousOutcomes.contains(AD_HOC)) {
					adHoc = inputData;
					inputData = AD_HOC;
				}
			}
			//EXECUTE TASK
			if (adHoc != null) {
				// AD HOC SCENARIO
				// Complete current task and based on outcome move to next task.
				WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance, inputData, adHoc); 
				return result = ScriptEngineConstants.SUCCESS;
			}
			// REGULAR SCENARIO
			// Complete current task and based on outcome move to next task.
			WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance, inputData, null); 
			result = ScriptEngineConstants.SUCCESS;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"ERROR[104]: ERROR OCCURED WHILE SUBMITTING LINE ITEM: "+ e.getMessage());
		}
		return result;
	}


	/*************************UTIL METHODS ************************************/
	/**
	 * Validate ProcessId
	 * @param id
	 * @param typeId
	 * @return
	 */
	private boolean isProcessIDInCorrect(String id, String typeId) {
		IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, typeId,TimeUtil.getUnixTimeStamp()); //TODO: This should not be done here..
		return !WorkFlowHelper.getInMemoryCache().containsKey(incomingDataDTO);
	}

	/**
	 * Function to find the 'AD_HOC' and replace it with ad hoc values. 
	 * Ad hoc values are fetched from process details dto.
	 * If no 'AD_HOC' is fond list is return as it is.
	 * 
	 * @param listContact
	 * @return list of values containing ad hoc values.
	 */
	private List<String> findAndAddAdHocVariables(List<String> listContact) {
		//Check for ad hoc
		if (listContact.contains(AD_HOC)) {
			listContact.remove(AD_HOC);
			listContact.addAll(adHocList);
		}
		return listContact;
	}

	/**
	 * Function returns AD Hoc List
	 * 
	 * @return list of ad hoc variables
	 * @throws ScriptEngineException
	 */
	private List<String> getAdHocList() throws ScriptEngineException {
		return WorkFlowHelper.getWorkFlowServiceInstance().fetchProcessDetails().getProcessVariables();
	}

	/**
	 * Validate if data is null
	 * @param args
	 * @return boolean
	 */
	private boolean isDataNull(String... args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null || args[i].equals("?")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create Incoming DTO Object 
	 * @param id
	 * @param typeId
	 * @param timeStamp
	 * @return
	 */
	private IncomingDataDTO createIncomingDataDTO(String id, String typeId,long timeStamp) {
		IncomingDataDTO dataDTO = new IncomingDataDTO();
		dataDTO.setId(id);
		dataDTO.setTypeID(typeId);
		dataDTO.setTimeStamp(timeStamp);
		return dataDTO;
	}

	//	/**
	//  * THIS MIGHT BE USED IN FUTURE	
	//	 * clean Script Engine 
	//	 */
	//	@Override
	//	public String clean() {
	//		int size = WorkFlowHelper.getInMemoryCache().size();
	//		for (ProcessInstance pi : WorkFlowHelper.getInMemoryCache().values()) {
	//			try {
	//				WorkFlowHelper.getWorkFlowServiceInstance().login();
	//				WorkFlowHelper.getWorkFlowServiceInstance()
	//				.deleteProcessInstanceAndCacheEntry(pi);
	//			} catch (ScriptEngineException e) {
	//				e.printStackTrace();
	//				WorkFlowHelper.getWorkFlowServiceInstance().cleanScriptEngine();
	//				return new StringBuilder().append(
	//						"Script Engine:Exception while Cleaning: "
	//								+ e.getErrorName()).toString();
	//
	//			} catch (LoginException loginException) {
	//				return ("Script Engine: LoginException while Cleaning: Please Try to Login.... ");
	//			}
	//		}
	//		return new StringBuilder().append("Script Engine:Total:[").append(size)
	//				.append("]Cleared from cache.").toString();
	//	}

}
