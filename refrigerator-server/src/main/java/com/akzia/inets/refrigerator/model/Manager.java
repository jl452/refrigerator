package com.akzia.inets.refrigerator.model;

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

@Entity
@Table(name = "manager")
public class Manager implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "manager_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "manager_SEQUENCE_GENERATOR", sequenceName = "manager_sequence")
	private long id;
	private String name;
	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY/*, orphanRemoval = true*/)//TODO
	private Collection<Merchandiser> merchandisers;

	private Manager(){
	}

	public Manager(String name, Collection<Merchandiser> merchandisers){
		this.name = name;
		this.merchandisers = merchandisers;
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public Collection<Merchandiser> getMerchandisers(){
		return merchandisers;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setMerchandisers(Collection<Merchandiser> merchandisers){
		this.merchandisers = merchandisers;
	}
}
