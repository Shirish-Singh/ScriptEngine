package com.scriptengine.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.servlet.ServletContextEvent;

import com.scriptengine.dto.ScriptDetailsDTO;

/**
 * <code>ScriptEngineFacade</code> exposes basic functions that can be used to
 * perform operations on Script Engine. <br>
 * <code>ScriptEngineFacade</code> is an web service. </p>
 * 
 * Note: If you are going to give this interface to client make sure it is the same one that you have here.
 * @author Shirish singh
 */
@WebService
@SOAPBinding(style = Style.RPC)
public interface ScriptEngineFacade {

	/**
	 * Function to get current script name, requires an unique id and type id to
	 * be passed.
	 * 
	 * @param id
	 * @param typeId
	 * @return current script name or error if any.
	 */
	@WebMethod
	ScriptDetailsDTO fetchCurrentScript(String id, String typeId);

	/**
	 * Function to get outcome list associated with provided id and type id.
	 * 
	 * @param id
	 * @param typeId
	 * @return list of outcomes
	 */
	@WebMethod
	String[] fetchPossibleOutcomeList(String id, String typeId);

	/**
	 * Function to submit the selected outcome and proceed to next state.
	 * 
	 * @param id
	 * @param typeId
	 * @param inputData
	 *            selected outcome
	 * @return If it is successful then it returns "Successful" else it returns
	 *         "Failed" for failure
	 */
	@WebMethod
	public String submitLineItem(String id, String typeId, String inputData);

	/**
	 * Function to start Script Engine service.
	 * 
	 * @param servletContextEvent
	 * @return script engine process id
	 */
	@WebMethod(exclude = true)
	public void startScriptService(ServletContextEvent servletContextEvent);
}
