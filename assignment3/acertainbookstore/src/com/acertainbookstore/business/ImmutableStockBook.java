package com.acertainbookstore.business;

/**
 * The book object that gets sent back to the StockManager. We do not allow the
 * StockManager to make any changes to the data structure. The member functions
 * are commented in the StockBook interface instead of here.
 *
 */
public final class ImmutableStockBook extends ImmutableBook implements
		StockBook {

	private final long totalRating;
	private final long timesRated;
	private final int numCopies;
	private final long saleMisses;
	private final boolean editorPick;

	/**
	 * Creates an immutable StockBook.
	 *
	 * @param isbn
	 * @param title
	 * @param author
	 * @param price
	 * @param numCopies
	 * @param SaleMisses
	 * @param TimesRated
	 * @param TotalRating
	 * @param editorPick
	 */
	public ImmutableStockBook(int isbn, String title, String author,
			float price, int numCopies, long saleMisses, long timesRated,
			long totalRating, boolean editorPick) {
		super(isbn, title, author, price);
		this.totalRating = totalRating;
		this.timesRated = timesRated;
		this.numCopies = numCopies;
		this.saleMisses = saleMisses;
		this.editorPick = editorPick;
	}

	public long getTotalRating() {
		return totalRating;
	}

	public long getTimesRated() {
		return timesRated;
	}

	public int getNumCopies() {
		return numCopies;
	}

	public long getSaleMisses() {
		return saleMisses;
	}

	public float getAverageRating() {
		return (float) (timesRated == 0 ? -1.0 : totalRating / timesRated);
	}

	public boolean isEditorPick() {
		return editorPick;
	}

}
