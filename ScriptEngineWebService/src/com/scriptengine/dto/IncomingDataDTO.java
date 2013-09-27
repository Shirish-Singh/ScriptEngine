package com.scriptengine.dto;

/**
 * Simple DTO class for holding incoming data
 * 
 * @author Shirish Singh
 */
public class IncomingDataDTO {
	
	private String sessionId=null;
	private String typeId=null;
	private Long timeStamp=null;

	/**
	 * @return Id
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param id
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	/**
	 * @return Type Id
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	
	/**
	 * @return Time Stamp
	 */
	public Long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * @param timeStamp
	 */
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * @return generated hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
		return result;
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncomingDataDTO other = (IncomingDataDTO) obj;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;
		return true;
	}
	
	/**
	 * @return string
	 */
	@Override
	public String toString() {
		return "IncomingDataDTO [sessionId=" + sessionId + ", typeId=" + typeId
				+ ", timeStamp=" + timeStamp + "]";
	}

}
