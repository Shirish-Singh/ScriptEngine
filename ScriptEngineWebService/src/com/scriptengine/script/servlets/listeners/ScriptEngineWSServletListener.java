package com.scriptengine.script.servlets.listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.scriptengine.webservice.ScriptEngineFacade;
import com.scriptengine.webservice.ScriptEngineFacadeImpl;

/**
 * Application Lifecycle Listener implementation class ScriptEngineWSServletListener
 *
 */
public class ScriptEngineWSServletListener implements ServletContextListener {

	protected final static Logger LOGGER = Logger.getLogger(ScriptEngineWSServletListener.class.getName());
	
    /**
     * Default constructor. 
     */
    public ScriptEngineWSServletListener() {
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    	LOGGER.log(Level.INFO,"Script Engine: Context Intialized");
    	ScriptEngineFacade scriptEngine=new ScriptEngineFacadeImpl();
		scriptEngine.startScriptService(servletContextEvent);
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	LOGGER.log(Level.INFO,"Script Engine: Context Destroyed");
    }
	
}
