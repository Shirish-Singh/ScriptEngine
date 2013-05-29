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
 * Job for cleaning stale mapping.
 * 
 * @author Shirish Singh
 * 
 */
public class ScriptEngineJobScheduler implements Job {

	protected final static Logger LOGGER = Logger
			.getLogger(ScriptEngineJobScheduler.class.getName());

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		LOGGER.log(Level.INFO,"------------------------------Job Started-----------------------------------------------------------");
		LOGGER.log(Level.INFO,"<<<<<Job For Cleaning Stale Mapping From Cache is Running>>>>>"); 
		Map<IncomingDataDTO, ProcessInstance> mappingMap =WorkFlowHelper.getInMemoryCache();
		Iterator<Map.Entry<IncomingDataDTO, ProcessInstance>> entries = mappingMap.entrySet().iterator();
		//Iterate map entries
		while (entries.hasNext()) {
			Map.Entry<IncomingDataDTO, ProcessInstance> entry = entries.next();
			IncomingDataDTO dataDTO=entry.getKey();
			Long futureTimeStamp=dataDTO.getTimeStamp()+ScriptEngineConstants.TIME_TO_LIVE;
			Long currentTimeStamp=TimeUtil.getUnixTimeStamp();
			
			LOGGER.log(Level.INFO,"Entries in Memory Cache:" + mappingMap.size()+" at TimeStamp:"+currentTimeStamp);
			LOGGER.log(Level.INFO,"End TimeStamp:" + futureTimeStamp);
			LOGGER.log(Level.INFO,"Current TimeStamp:" + currentTimeStamp);

			//Check if currentTimeStamp exceeds futureTimeStamp in order to remove or keep the instance in cache.
			if(currentTimeStamp>futureTimeStamp){
				LOGGER.log(Level.INFO,"Removing mapping and delete ProcessInstance");
				boolean result=ScriptEngineHelper.getInstance().clean(dataDTO);
				LOGGER.log(Level.INFO,"Process instance for id: "+dataDTO.getProcessID()+" and Type Id:"+dataDTO.getTypeID() +" Deleted:"+result);
			}
		}
		LOGGER.log(Level.INFO,"-----------------------------Job Ended-------------------------------------------------------------");
	}

}
