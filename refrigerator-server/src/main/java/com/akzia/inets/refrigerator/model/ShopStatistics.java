package com.akzia.inets.refrigerator.model;

public class ShopStatistics{
	public String brandName;
	public long need;
	public long sold;

	public ShopStatistics(String brandName, long need, long sold){
		this.brandName = brandName;
		this.need = need;
		this.sold = sold;
	}
}
