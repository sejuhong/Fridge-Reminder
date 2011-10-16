package edu.calpoly.cpe409.fridgereminder;

/**
 * This class represents the barcode information
 */
public class BarcodeInfo {
	public String barcode;
	public String name;
	public int category;

	/**
	 * This is a constructor for the Barcode info
	 * @param barcode the barcode in string
	 * @param name the name of the item
	 * @param category the category that barcode falls into
	 */
	public BarcodeInfo(String barcode, String name, int category) {
		this.barcode = barcode;
		this.name = name;
		this.category = category;
	}
}
