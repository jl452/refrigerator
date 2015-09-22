package com.akzia.inets.refrigerator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "brand")
public class Brand implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "brand_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "brand_SEQUENCE_GENERATOR", sequenceName = "brand_sequence")
	private long id;
	@Column(unique = true)
	private String name;

	public Brand(){
	}

	public Brand(String name){
		this.name = name;
	}

	public long getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
}
