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
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

@Entity
@Table(name = "merchandiser")
public class Merchandiser implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "merchandiser_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "merchandiser_SEQUENCE_GENERATOR", sequenceName = "merchandiser_sequence")
	private long id;
	private String name;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Collection<Shop> shops;

	@JsonIgnore
	private byte[] descriptionBinaryData;

	private Merchandiser(){
	}

	public Merchandiser(String name, Collection<Shop> shops, HashMap<String, String> description){
		this.name = name;
		this.shops = shops;
		descriptionBinaryData = Utils.byteArrayFromMap(description);
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public Collection<Shop> getShops(){
		return shops;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setShops(Collection<Shop> shops){
		this.shops = shops;
	}

	public HashMap<String, String> getDescription(){
		return Utils.mapFromByteArray(descriptionBinaryData);
	}

	public void updateDescription(HashMap<String, String> description){
		descriptionBinaryData = Utils.byteArrayFromMap(description);
	}
}
