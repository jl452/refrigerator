package com.akzia.inets.refrigerator.model;

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
@Table(name = "refrigeratorShelf")
public class RefrigeratorShelf implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "refrigeratorShelf_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "refrigeratorShelf_SEQUENCE_GENERATOR", sequenceName = "refrigeratorShelf_sequence")
	private long id;
	private int index;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Collection<RefrigeratorLine> lines;

	private RefrigeratorShelf(){
	}

	public RefrigeratorShelf(int index, Collection<RefrigeratorLine> lines){
		this.index = index;
		this.lines = lines;
	}

	public long getId(){
		return id;
	}

	public int getIndex(){
		return index;
	}

	public Collection<RefrigeratorLine> getLines(){
		return lines;
	}
}
