package edu.calpoly.cpe409.fridgereminder;

import java.io.Serializable;
import java.util.Date;

public class Product implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7739122094586735508L;
	private String name;
	private int count;
	private Date boughtDate;
	private Date expirationDate;
	private Category category;
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Product(String name, Date boughtDate, Date expirationDate, int count,
			Category category, int id) {
		this.name = name;
		this.expirationDate = expirationDate;
		this.count = count;
		this.boughtDate = boughtDate;
		this.category = category;
		this.id = id;
	}

	public Date getBoughtDate() {
		return boughtDate;
	}

	public Category getCategory() {
		return category;
	}

	public int getCount() {
		return count;
	}

	public String getName() {
		return name;
	}

	public void setBoughtDate(Date boughtDate) {
		this.boughtDate = boughtDate;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setName(String name) {
		this.name = name;
	}

}
