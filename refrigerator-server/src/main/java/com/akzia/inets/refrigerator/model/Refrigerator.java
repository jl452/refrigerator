package com.akzia.inets.refrigerator.model;

import com.akzia.inets.refrigerator.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashMap;

@Entity
@Table(name = "refrigerator")
public class Refrigerator implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "refrigerator_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "refrigerator_SEQUENCE_GENERATOR", sequenceName = "refrigerator_sequence")
	private long id;
	private String name;

	@JsonIgnore
	private byte[] soldBinaryData;

	private int currentMaxItems;
	private int currentItems;

	public Refrigerator(){
	}

	public Refrigerator(String name){
		this.name = name;
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public HashMap<Long, Long> getSold(){
		return Utils.mapFromByteArray(soldBinaryData);
	}

	public void updateSold(HashMap<Long, Long> sold){
		soldBinaryData = Utils.byteArrayFromMap(sold);
	}

	public int getCurrentMaxItems(){
		return currentMaxItems;
	}

	public void setCurrentMaxItems(int currentMaxItems){
		this.currentMaxItems = currentMaxItems;
	}

	public int getCurrentItems(){
		return currentItems;
	}

	public void setCurrentItems(int currentItems){
		this.currentItems = currentItems;
	}

	public void setName(String name){
		this.name = name;
	}
}
