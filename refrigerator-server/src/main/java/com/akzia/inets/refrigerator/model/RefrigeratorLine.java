package com.akzia.inets.refrigerator.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

@Entity
@Table(name = "refrigeratorLine")
public class RefrigeratorLine implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "refrigeratorLine_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "refrigeratorLine_SEQUENCE_GENERATOR", sequenceName = "refrigeratorLine_sequence")
	private long id;
	private int index;
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)//must be FetchType.EAGER for jackson
	private Brand brand;
	private int filledVolume;
	private int x;
	private int y;
	private int filledVolumeMax;

	//for REST return object->json
	@Transient
	private long brandLogoId;

	private RefrigeratorLine(){
	}

	public RefrigeratorLine(int index, Brand brand, int filledVolume, int x, int y){//todo X, Y
		this.index = index;
		this.brand = brand;
		this.filledVolume = filledVolume;
		this.x = x;
		this.y = y;
	}

	public RefrigeratorLine(int index, Brand brand, int filledVolume, int filledVolumeMax){
		this.index = index;
		this.brand = brand;
		this.filledVolume = filledVolume;
		this.filledVolumeMax = filledVolumeMax;
	}

	public long getId(){
		return id;
	}

	public int getIndex(){
		return index;
	}

	public Brand getBrand(){
		return brand;
	}

	public int getFilledVolume(){
		return filledVolume;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public long getFilledVolumeMax(){
		return filledVolumeMax;
	}


	public long getBrandLogoId(){
		return brandLogoId;
	}

	public void updateRestItems(long logoId){
		this.brandLogoId = logoId;
	}
}
