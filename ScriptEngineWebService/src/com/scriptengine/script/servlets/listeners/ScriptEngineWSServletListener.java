package com.scriptengine.script.servlets.listeners;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.workflow.utils.ScriptEngineJobScheduler;
import com.scriptengine.webservice.ScriptEngineFacade;
import com.scriptengine.webservice.ScriptEngineFacadeImpl;

/**
 * Application Life cycle Listener implementation class
 * ScriptEngineWSServletListener
 * 
 * @author Shirish Singh
 */
public class ScriptEngineWSServletListener implements ServletContextListener {

	/**
	 * Logger for Logging Purposes
	 */
	protected final static Logger LOGGER = Logger.getLogger(ScriptEngineWSServletListener.class.getName());
	
	/**
	 * Constants
	 */
	private static final String TRIGGER_NAME="Clean Cache Trigger";
	private static final String JOB_NAME="Script Engine Cache";

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		LOGGER.log(Level.INFO, "Script Engine: Context Intialized");
		//Important to set realPath
		//ScriptEngineFacadeImpl.realPath = servletContextEvent.getServletContext().getRealPath(WEB_INF).replace("\\","/"); 
		ScriptEngineFacade scriptEngine = new ScriptEngineFacadeImpl();
		scriptEngine.startScriptService(servletContextEvent);
		//Setup Job
		setupSchedulingJob();
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		LOGGER.log(Level.INFO, "Script Engine: Context Destroyed");
	}

	/**
	 * Setup Scheduling Job
	 */
	private void setupSchedulingJob(){
		JobDetailImpl job = new JobDetailImpl();
		job.setName(JOB_NAME);
		job.setJobClass(ScriptEngineJobScheduler.class);

		Scheduler scheduler=null;
		CronTriggerImpl trigger = new CronTriggerImpl();
		trigger.setName(TRIGGER_NAME);
		try {
			trigger.setCronExpression(ScriptEngineConstants.SCHEDULE_TIME);
			LOGGER.log(Level.INFO, "Script Engine: Scheduling job Triggered");
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(job, trigger);
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, "Script Engine: Exception while creating job");
			e.printStackTrace();
		} catch (SchedulerException e) {
			LOGGER.log(Level.SEVERE, "Script Engine: Exception while creating job");
			e.printStackTrace();
		}
	}
}
