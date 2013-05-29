package com.scriptengine.script.dto;

/**
 * Simple DTO class for holding incoming data
 * @author Shirish Singh
 *
 */
public class IncomingDataDTO {
	
	private String processID=null;
	private String typeID=null;
	private Long timeStamp=null;

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getTypeID() {
		return typeID;
	}

	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((processID == null) ? 0 : processID.hashCode());
		result = prime * result + ((typeID == null) ? 0 : typeID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncomingDataDTO other = (IncomingDataDTO) obj;
		if (processID == null) {
			if (other.processID != null)
				return false;
		} else if (!processID.equals(other.processID))
			return false;
		if (typeID == null) {
			if (other.typeID != null)
				return false;
		} else if (!typeID.equals(other.typeID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IncomingDataDTO [processID=" + processID + ", typeID=" + typeID
				+ ", timeStamp=" + timeStamp + "]";
	}
	
	

}
