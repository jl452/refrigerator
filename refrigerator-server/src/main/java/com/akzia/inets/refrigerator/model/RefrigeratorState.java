package com.akzia.inets.refrigerator.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "refrigeratorState")
public class RefrigeratorState implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "refrigeratorState_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "refrigeratorState_SEQUENCE_GENERATOR", sequenceName = "refrigeratorState_sequence")
	private long id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationTime = new Date();
	private long refrigeratorId;
	private RefrigeratorState planogram;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Collection<RefrigeratorShelf> shelves;

	private String name;
	@Temporal(TemporalType.TIMESTAMP)
	private Date creationLocalTime;

	private int itemCount;

	protected RefrigeratorState(){
	}

	public RefrigeratorState(long refrigeratorId, Collection<RefrigeratorShelf> shelves, RefrigeratorState planogram, Date creationLocalTime){
		this.refrigeratorId = refrigeratorId;
		this.shelves = shelves;
		this.creationLocalTime = creationLocalTime;
		for (RefrigeratorShelf shelf : shelves){
			for (RefrigeratorLine line : shelf.getLines()){
				itemCount += line.getFilledVolume();
			}
		}
		this.planogram = planogram;
		this.name = null;
	}

	public RefrigeratorState(long refrigeratorId, Collection<RefrigeratorShelf> shelves, String name){
		this.refrigeratorId = refrigeratorId;
		this.shelves = shelves;
		this.name = name;
		for (RefrigeratorShelf shelf : shelves){
			for (RefrigeratorLine line : shelf.getLines()){
				itemCount += line.getFilledVolume();
			}
		}
		this.planogram = null;
	}

	public long getId(){
		return id;
	}

	public Date getCreationTime(){
		return creationTime;
	}

	public long getRefrigeratorId(){
		return refrigeratorId;
	}

	public RefrigeratorState getPlanogram(){
		return planogram;
	}

	public Collection<RefrigeratorShelf> getShelves(){
		return shelves;
	}


	public String getName(){
		return name;
	}

	public Date getCreationLocalTime(){
		return creationLocalTime;
	}

	public int getItemCount(){
		return itemCount;
	}

	@JsonIgnore
	public boolean isPlanogram(){
		return planogram == null;
	}
}
