package edu.calpoly.cpe409.fridgereminder;

public class BarcodeInfo {
	public String barcode;
	public String name;
	public int category;

	public BarcodeInfo(String barcode, String name, int category) {
		this.barcode = barcode;
		this.name = name;
		this.category = category;
	}
}
