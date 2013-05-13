package com;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.scriptengine.webservice.ScriptEngineFacade;

public class WSMain {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		
		URL url = new URL("http://localhost:8080/ScriptEngineWebService/se?wsdl");
        //1st argument service URI, refer to wsdl document above
	//2nd argument is service name, refer to wsdl document above
        QName qname = new QName("http://webservice.scriptengine.com/", "ScriptEngineFacadeImplService");
 
        Service service = Service.create(url, qname);
 
        ScriptEngineFacade hello = service.getPort(ScriptEngineFacade.class);
 
        //System.out.println(hello.startScriptService());
	}

}
