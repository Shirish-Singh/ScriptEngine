package com.scriptengine.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.servlet.ServletContextEvent;

/**
 * ScriptEngineFacade is basically Web Service Interface that will be exposed to Client.
 * @author Shirish singh
 *
 */
@WebService
@SOAPBinding(style=Style.RPC)
public interface ScriptEngineFacade {
	
	//ACCESS URL : http://localhost:8080/ScriptEngineWebService/se?wsdl
	
	/**
	 * @param id
	 * @param typeId
	 * @return currentScript/error if any
	 */
	@WebMethod
	String fetchCurrentScript(String id,String typeId);

	/**
	 * 
	 * @param scriptProcessId
	 * @param typeId
	 * @return
	 */
	@WebMethod
	String[] fetchPossibleOutcomeList(String scriptProcessId,String typeId);
	
	/**
	 * 
	 * @param processID
	 * @param typeId
	 * @param inputData
	 * @return isSuccesfull = "Successful"/"Failed" 
	 */
	@WebMethod
	 public String submitLineItem(String processID,String typeId,String inputData);
	
	/**
	 * 
	 * @param servletContext
	 * @return
	 */
	@WebMethod(exclude=true)
	public String startScriptService(ServletContextEvent servletContextEvent);
	
	/**
	 * 
	 * @param 
	 * @return
	 */
	@WebMethod
	public String clean();
}
