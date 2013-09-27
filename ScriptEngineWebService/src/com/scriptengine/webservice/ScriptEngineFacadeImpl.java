package com.scriptengine.webservice;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletContextEvent;

import org.ow2.bonita.facade.runtime.ProcessInstance;

import com.scriptengine.dto.IncomingDataDTO;
import com.scriptengine.dto.ScriptDetailsDTO;
import com.scriptengine.script.Exception.ScriptEngineException;
import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.helper.ScriptEngineHelper;
import com.scriptengine.script.workflow.dto.ProcessDetailsDTO;
import com.scriptengine.script.workflow.dto.TaskDetailsDTO;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;
import com.scriptengine.script.workflow.utils.TimeUtil;

/**
 * Skeletal implementation of {@link ScriptEngineFacade}.
 * 
 * @author Shirish singh
 */
@WebService(endpointInterface = "com.scriptengine.webservice.ScriptEngineFacade")
public class ScriptEngineFacadeImpl implements ScriptEngineFacade {

	/**
	 * Logger for logging purpose
	 */
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
	public void startScriptService(ServletContextEvent servletContextEvent) {
		try {
			// Set Real Path
			ScriptEngineHelper.setRealServerPath(servletContextEvent.getServletContext().getRealPath("/WEB-INF").replace("\\", "/"));
			// Set Servlet Context
			WorkFlowHelper.setServletContext(servletContextEvent.getServletContext());
			
			WorkFlowHelper.getWorkFlowServiceInstance().beginProcess();
			
			ProcessDetailsDTO processDetailsDTO = WorkFlowHelper.constructProcessDetailsDTO("PhoneClient_phoneDebtor", null);
			// Set AD_HOC variables to AD_HOC List
			adHocList = processDetailsDTO.getProcessVariables();
			//processDetailsDTO.getProcessId();
		} catch (ScriptEngineException e) {
			LOGGER.log(Level.SEVERE,"ERROR[101]:ERROR OCCURED WHILE STARTING SCRIPTING ENGINE : "+ e.getMessage());
		}
	}

	/**
	 * @see ScriptEngineFacade#fetchCurrentScript(String, String)
	 */
	@Override
	@WebMethod
	public ScriptDetailsDTO fetchCurrentScript(String id, String processName) {
		
		ScriptDetailsDTO scriptDetailsDTO = new ScriptDetailsDTO();
		
		if (isDataNull(id)) {			
			scriptDetailsDTO.setErrorMsg("VALIDATION ERROR: Incoming Data is Invalid");
			return scriptDetailsDTO;
		}
		
		
		processName = processName.replace('.', '_');
		
		LOGGER.log(Level.INFO," processName : "+ processName);
	
		

		/*if (!typeId.matches("[1-9]")) { 
			// TODO ( What can be done here is fetch
			// this number list from process diagram
			// ..think)
			// TODO Refactor , make it configurable or handle validation in
			// other way..
			scriptDetailsDTO.setErrorMsg("VALIDATION ERROR: Provided Type ID Doesnt Exist, please send typeID in range [1-9]");
			return scriptDetailsDTO;
		}*/
		try {
			// Login to proceed
			WorkFlowHelper.getWorkFlowServiceInstance().login(); // TODO Not Good here
			// Create IncomingDataDTO
			IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, processName, TimeUtil.getCurrentUnixTimeStamp());
			// Fetch ProcessInstance
			// Check if incomingDataDTO is present in cache if yes get process
			// instance for the same else create process instance and return it.
			System.out.println("Fetching process Instace");
			ProcessInstance processInstance = WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			// Get Task Details DTO based on processInstance
			TaskDetailsDTO currentTaskDetailsDTO = WorkFlowHelper.getWorkFlowServiceInstance().fetchTaskDetails(processInstance);
			List<String> listContact = WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
			listContact = findAndAddAdHocVariables(listContact);
			
			scriptDetailsDTO.setScriptName(currentTaskDetailsDTO.getTaskName());
			scriptDetailsDTO.setOutcomes(listContact);
			
			return scriptDetailsDTO;
		} catch (ScriptEngineException se) {
			scriptDetailsDTO.setErrorMsg("ERROR[102]:ERROR OCCURED WHILE FETCHING CURRENT SCRIPT : " + se.getErrorName() + " :: " + se.getErrorDescription());
			return scriptDetailsDTO;
		} catch (Exception e) {
			scriptDetailsDTO.setErrorMsg("ERROR[102]:ERROR OCCURED WHILE FETCHING CURRENT SCRIPT : " + e.getMessage());
			return scriptDetailsDTO;
		}
	}

	/**
	 * @see ScriptEngineFacade#fetchPossibleOutcomeList(String, String)
	 */
	@Override
	@WebMethod
	public String[] fetchPossibleOutcomeList(String id, String processName) {
		if (isDataNull(id)) {
			return new String[] { "VALIDATION ERROR: Incoming Data is Invalid:" + id };
		}
		if (isDataNull(processName)) {
			return new String[] { "VALIDATION ERROR: Type ID is Invalid:" + processName };
		}
		
		processName = processName.replace('.', '_');
		
		IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, processName, TimeUtil.getCurrentUnixTimeStamp());
		if (isProcessIDInCorrect(incomingDataDTO)) {
			return new String[] { "VALIDATION ERROR: Process ID is Invalid:" + id + processName };
		}
		try {
			// Login to proceed
			WorkFlowHelper.getWorkFlowServiceInstance().login();
			
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
	public String submitLineItem(String id, String processName, String inputData) {
		System.out.println(" Inside submitLineItem " );
		System.out.println(" processName :  " + processName );
		System.out.println(" inputData :  " + inputData );
		// TODO Validation process for fetch/submit
		if (isDataNull(id, inputData)) {
			System.out.println("VALIDATION ERROR: Incoming Data is Invalid:" + id + " and " + inputData);
			return "VALIDATION ERROR: Incoming Data is Invalid:" + id + " and " + inputData; // SB
		}
		if (isDataNull(processName)) {
			System.out.println("VALIDATION ERROR: processName is Invalid:" + processName);
			return "VALIDATION ERROR: processName is Invalid:" + processName;
		}
		
		processName = processName.replace('.', '_');
		
		IncomingDataDTO incomingDataDTO = createIncomingDataDTO(id, processName, TimeUtil.getCurrentUnixTimeStamp());
		
		if (isProcessIDInCorrect(incomingDataDTO)) {
			System.out.println("VALIDATION ERROR: Process ID is Invalid:" + id + " : " + processName);
			return "VALIDATION ERROR: Process ID is Invalid:" + id + processName; // SB
		}

		
		/*if (!Arrays.asList(fetchPossibleOutcomeList(id, processName)).contains(
				inputData)) {
			return "VALIDATION ERROR: Input Data is Invalid:" + inputData;
		}*/
		String result = ScriptEngineConstants.FAILED;
		try {
			String adHoc = null;
			WorkFlowHelper.getWorkFlowServiceInstance().login();
			
			// Fetch ProcessInstance
			ProcessInstance processInstance = WorkFlowHelper.getWorkFlowServiceInstance().getProcessInstance(incomingDataDTO);
			System.out.println(" processInstance :  " + processInstance );
			// Check for adHocData
			if("PhoneClient_phoneDebtor".equalsIgnoreCase(processName) || "PhoneClient_phoneReference".equalsIgnoreCase(processName) 
					|| "PhoneClient_phoneCoDebtor".equalsIgnoreCase(processName) ){
				
				if (getAdHocList(processName).contains(inputData)) {
					List<String> previousOutcomes = WorkFlowHelper.getWorkFlowServiceInstance().fetchCurrentTaskVariableValues(processInstance);
					if (previousOutcomes.contains(AD_HOC)) {
						adHoc = inputData;
						inputData = AD_HOC;
					}
				}
				//EXECUTE TASK
				if (adHoc != null) {
					// AD HOC SCENARIO
					System.out.println(" AD HOC SCENARIO :  " + processInstance );
					// Complete current task and based on outcome move to next task.
					WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance, inputData, adHoc); 
					return result = ScriptEngineConstants.SUCCESS;
				}
			}
			// REGULAR SCENARIO
			System.out.println(" REGULAR SCENARIO :  " + processInstance );
			// Complete current task and based on outcome move to next task.
			WorkFlowHelper.getWorkFlowServiceInstance().executeTask(processInstance, inputData, null); 
			result = ScriptEngineConstants.SUCCESS;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"ERROR[104]: ERROR OCCURED WHILE SUBMITTING LINE ITEM: "+ e.getMessage());
			return result;
		}
		return result;
	}


	/*************************UTIL METHODS ************************************/
	/**
	 * Validate ProcessId
	 * 
	 * @param id
	 * @param typeId
	 * @return
	 */
	private boolean isProcessIDInCorrect(IncomingDataDTO incomingDataDTO) {
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
	private List<String> getAdHocList(String processName) throws ScriptEngineException {
		return WorkFlowHelper.getWorkFlowServiceInstance().fetchProcessDetails(processName).getProcessVariables();
	}

	/**
	 * Validate if data is null
	 * 
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
	 *  
	 * @param id
	 * @param typeId
	 * @param timeStamp
	 * @return
	 */
	private IncomingDataDTO createIncomingDataDTO(String sessionId, String typeId,long timeStamp) {
		IncomingDataDTO dataDTO = new IncomingDataDTO();
		dataDTO.setSessionId(sessionId);
		dataDTO.setTypeId(typeId);
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
