package edu.calpoly.cpe409.fridgereminder;

/**
 * Enums for the different categories of product 
 */
public enum Category {
	FRUIT, VEGETABLES, DAIRY, MEAT, FISH, BREADANDCEREAL, PACKAGEDFOODANDMIXES, POULTRYANDEGGS, NONE;

	/**
	 * Returns a category depending on the input
	 * @param ordinal number to be parsed to category
	 * @return a category
	 */
	public static Category parse(int ordinal) {
		switch (ordinal) {
		case 0:
			return FRUIT;
		case 1:
			return VEGETABLES;
		case 2:
			return DAIRY;
		case 3:
			return MEAT;
		case 4:
			return FISH;
		case 5:
			return BREADANDCEREAL;
		case 6:
			return PACKAGEDFOODANDMIXES;
		case 7:
			return POULTRYANDEGGS;
		case 8:
			return NONE;
		}

		return null;
	}

	/**
	 * Returns a string that represents the current category
	 */
	public String toString() {
		switch (this.ordinal()) {
		case 0:
			return "Fruits";
		case 1:
			return "Vegetables";
		case 2:
			return "Dairy";
		case 3:
			return "Meat";
		case 4:
			return "Fish";
		case 5:
			return "Bread/Cereal";
		case 6:
			return "Packaged Food/Mixes";
		case 7:
			return "Poultry/Eggs";
		case 8:
			return "N/A";
		}

		return null;
	}
}
