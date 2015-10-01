package com.akzia.inets.refrigerator.model;

import com.akzia.inets.refrigerator.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

@Entity
@Table(name = "shop")
public class Shop implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "shop_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "shop_SEQUENCE_GENERATOR", sequenceName = "shop_sequence")
	private long id;
	private String name;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonIgnore
	private Collection<Refrigerator> refrigerators;
	private int oos;//0-100 = %

	@JsonIgnore
	private byte[] descriptionBinaryData;


	private int currentMaxItems;
	private int currentItems;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER/*, orphanRemoval = true*/)
	private Collection<OOSEvent> oosEvents;

	//for REST return object->json
	@Transient
	private int refrigeratorsCount;

	private Shop(){
	}

	public Shop(String name, Collection<Refrigerator> refrigerators, int oos, HashMap<String, String> description){
		this.name = name;
		this.refrigerators = refrigerators;
		this.oos = oos;
		descriptionBinaryData = Utils.byteArrayFromMap(description);
		this.refrigeratorsCount = refrigerators.size();
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public Collection<Refrigerator> getRefrigerators(){
		return refrigerators;
	}

	public int getOos(){
		return oos;
	}

	public HashMap<String, String> getDescription(){
		return Utils.mapFromByteArray(descriptionBinaryData);
	}

	public void updateDescription(HashMap<String, String> description){
		descriptionBinaryData = Utils.byteArrayFromMap(description);
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

	public Collection<OOSEvent> getOosEvents(){
		return oosEvents;
	}

	public void setOosEvents(Collection<OOSEvent> oosEvents){
		this.oosEvents = oosEvents;
	}

	public int getRefrigeratorsCount(){
		return refrigeratorsCount;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setRefrigerators(Collection<Refrigerator> refrigerators){
		this.refrigerators = refrigerators;
	}

	public void setOos(int oos){
		this.oos = oos;
	}

	public void updateRestItems(){
		refrigeratorsCount = refrigerators.size();
	}
}
