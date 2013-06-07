package com.scriptengine.script.workflow.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.facade.runtime.ProcessInstance;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.scriptengine.script.constants.ScriptEngineConstants;
import com.scriptengine.script.dto.IncomingDataDTO;
import com.scriptengine.script.helper.ScriptEngineHelper;
import com.scriptengine.script.workflow.helper.WorkFlowHelper;

/**
 * Job Scheduler for scheduling job that runs at the given interval and cleans stale mapping's if present in cache.

 * @author Shirish Singh
 */
public class ScriptEngineJobScheduler implements Job {

	protected final static Logger LOGGER = Logger.getLogger(ScriptEngineJobScheduler.class.getName());

	/**
	 * @see Job#execute(JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		LOGGER.log(Level.INFO,"------------------------------Job Started-----------------------------------------------------------");
		LOGGER.log(Level.INFO,"<<<<<-- Job For Cleaning Stale Mapping From Cache is Running -->>>>>"); 
		Map<IncomingDataDTO, ProcessInstance> mappingMap =WorkFlowHelper.getInMemoryCache();
		Iterator<Map.Entry<IncomingDataDTO, ProcessInstance>> entries = mappingMap.entrySet().iterator();
		//Iterate map entries
		while (entries.hasNext()) {
			LOGGER.log(Level.INFO,"<<<<<-- Total Entries in Memory Cache:" + mappingMap.size()+" at TimeStamp:"+TimeUtil.getCurrentUnixTimeStamp()+" -->>>>>");
			Map.Entry<IncomingDataDTO, ProcessInstance> entry = entries.next();
			IncomingDataDTO dataDTO=entry.getKey();
			Long futureTimeStamp=dataDTO.getTimeStamp()+ScriptEngineConstants.TIME_TO_LIVE;
			Long currentTimeStamp=TimeUtil.getCurrentUnixTimeStamp();
			LOGGER.log(Level.INFO,"<<<<<-- Current TimeStamp:" + currentTimeStamp+" -->>>>>");
			LOGGER.log(Level.INFO,"<<<<<-- Incoming DTO Time Stamp:" + dataDTO.getTimeStamp()+" -->>>>>");
			LOGGER.log(Level.INFO,"<<<<<-- Total Time To Live TimeStamp:" + futureTimeStamp+" -->>>>>");

			//Check if currentTimeStamp exceeds futureTimeStamp in order to decide whether to remove or keep the instance in cache.
			if(currentTimeStamp>futureTimeStamp){
				LOGGER.log(Level.INFO,"<<<<<-- Stale Mapping Found, Removing mapping and deleting ProcessInstance"+" -->>>>>");
				boolean result=ScriptEngineHelper.getInstance().clean(dataDTO);
				LOGGER.log(Level.INFO,"<<<<<-- Process instance for id: "+dataDTO.getId()+" and Type Id:"+dataDTO.getTypeId() +" Deleted:"+result+" -->>>>>" );
			}
			LOGGER.log(Level.INFO,"<<<<<-- Total Entries in Memory Cache:" + mappingMap.size()+" at TimeStamp:"+TimeUtil.getCurrentUnixTimeStamp()+" -->>>>>");
		}
		LOGGER.log(Level.INFO,"-----------------------------Job Ended-------------------------------------------------------------");
	}

}
