package com.akzia.inets.refrigerator.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "oosEvent")
public class OOSEvent implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "oosEvent_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "oosEvent_SEQUENCE_GENERATOR", sequenceName = "oosEvent_sequence")
	private long id;
	private long refrigeratorId;
	//private LocalDateTime creationTime = LocalDateTime.now();
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime = new Date();

	//for REST return object->json
	@Transient
	private String refrigeratorName;
	@Transient
	private int currentPercent;

	private OOSEvent(){
	}

	public OOSEvent(long refrigeratorId){
		this.refrigeratorId = refrigeratorId;
	}

	public long getId(){
		return id;
	}

	public long getRefrigeratorId(){
		return refrigeratorId;
	}

	public Date getCreationTime(){
		return creationTime;
	}


	public void updateRestItems(String refrigeratorName, int currentPercent){
		this.refrigeratorName = refrigeratorName;
		this.currentPercent = currentPercent;
	}

	public String getRefrigeratorName(){
		return refrigeratorName;
	}

	public int getCurrentPercent(){
		return currentPercent;
	}
}
