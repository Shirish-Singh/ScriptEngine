package com.scriptengine.script.dto;

/**
 * Simple DTO class for holding incoming data
 * @author Shirish Singh
 *
 */
public class IncomingDataDTO {
	
	private String id=null;
	private String typeID=null;
	private Long timeStamp=null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
				+ ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return "IncomingDataDTO [id=" + id + ", typeID=" + typeID
				+ ", timeStamp=" + timeStamp + "]";
	}
	
	

}
