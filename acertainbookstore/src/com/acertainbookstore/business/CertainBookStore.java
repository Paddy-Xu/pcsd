package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * CertainBookStore implements the bookstore and its functionality which is
 * defined in the BookStore
 */
public class CertainBookStore implements BookStore, StockManager {

	final private Map<Integer, BookStoreBook> bookMap;

	public CertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<Integer, BookStoreBook>();
	}

	/**
	 * Auxiliary method to validate an ISBN number in order to avoid duplicate
	 * code.
   */
	private synchronized void checkValidity(Integer isbn)
	    throws BookStoreException {
		if (BookStoreUtility.isInvalidISBN(isbn)) {
			throw new BookStoreException(BookStoreConstants.ISBN + isbn
					+ BookStoreConstants.INVALID);
		}
		if (!bookMap.containsKey(isbn)) {
			throw new BookStoreException(BookStoreConstants.ISBN + isbn
					+ BookStoreConstants.NOT_AVAILABLE);
		}
	}

	/**
	 * Auxiliary method to validate a book in order to avoid duplicate code.
	 */
	private synchronized void checkValidity(StockBook book)
	    throws BookStoreException {
		checkValidity(book.getISBN());
		if (BookStoreUtility.isEmpty(book.getTitle())    					  ||
				BookStoreUtility.isEmpty(book.getAuthor())   	    			||
				BookStoreUtility.isInvalidNoCopies(book.getNumCopies()) ||
				book.getPrice() < 0.0) {
			throw new BookStoreException(BookStoreConstants.BOOK
					+ book.toString() + BookStoreConstants.INVALID);
		}
	}

	public synchronized void addBooks(Set<StockBook> bookSet)
			throws BookStoreException {

		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Validate input and check if books are there
		for (StockBook book : bookSet) {
			checkValidity(book);
			Integer isbn = book.getISBN();
			if (bookMap.containsKey(isbn)) {
				throw new BookStoreException(BookStoreConstants.ISBN + isbn
						+ BookStoreConstants.DUPLICATED);
			}
		}

		// Add books to store
		for (StockBook book : bookSet) {
			bookMap.put(book.getISBN(), new BookStoreBook(book));
		}

	}

	public synchronized void addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException {
		int ISBN, numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		for (BookCopy bookCopy : bookCopiesSet) {
			ISBN = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			if (BookStoreUtility.isInvalidISBN(ISBN))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			if (!bookMap.containsKey(ISBN))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			if (BookStoreUtility.isInvalidNoCopies(numCopies))
				throw new BookStoreException(BookStoreConstants.NUM_COPIES
						+ numCopies + BookStoreConstants.INVALID);

		}

		BookStoreBook book;
		// Update the number of copies
		for (BookCopy bookCopy : bookCopiesSet) {
			ISBN = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			book = bookMap.get(ISBN);
			book.addCopies(numCopies);
		}
	}

	public synchronized List<StockBook> getBooks() {
		List<StockBook> listBooks = new ArrayList<StockBook>();
		Collection<BookStoreBook> bookMapValues = bookMap.values();
		for (BookStoreBook book : bookMapValues) {
			listBooks.add(book.immutableStockBook());
		}
		return listBooks;
	}

	public synchronized void updateEditorPicks(Set<BookEditorPick> editorPicks)
			throws BookStoreException {
		// Check that all ISBNs that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int ISBNVal;

		for (BookEditorPick editorPickArg : editorPicks) {
			ISBNVal = editorPickArg.getISBN();
			if (BookStoreUtility.isInvalidISBN(ISBNVal))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
						+ BookStoreConstants.INVALID);
			if (!bookMap.containsKey(ISBNVal))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
						+ BookStoreConstants.NOT_AVAILABLE);
		}

		for (BookEditorPick editorPickArg : editorPicks) {
			bookMap.get(editorPickArg.getISBN()).setEditorPick(
					editorPickArg.isEditorPick());
		}
		return;
	}

	public synchronized void buyBooks(Set<BookCopy> bookCopiesToBuy)
			throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int ISBN;
		BookStoreBook book;
		Boolean saleMiss = false;
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			ISBN = bookCopyToBuy.getISBN();
			if (bookCopyToBuy.getNumCopies() < 0)
				throw new BookStoreException(BookStoreConstants.NUM_COPIES
						+ bookCopyToBuy.getNumCopies()
						+ BookStoreConstants.INVALID);
			if (BookStoreUtility.isInvalidISBN(ISBN))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			if (!bookMap.containsKey(ISBN))
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			book = bookMap.get(ISBN);
			if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
				book.addSaleMiss(); // If we cannot sell the copies of the book
									// its a miss
				saleMiss = true;
			}
		}

		// We throw exception now since we want to see how many books in the
		// order incurred misses which is used by books in demand
		if (saleMiss)
			throw new BookStoreException(BookStoreConstants.BOOK
					+ BookStoreConstants.NOT_AVAILABLE);

		// Then make purchase
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			book = bookMap.get(bookCopyToBuy.getISBN());
			book.buyCopies(bookCopyToBuy.getNumCopies());
		}
		return;
	}

	public synchronized List<StockBook> getBooksByISBN(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		for (Integer isbn : isbnSet) checkValidity(isbn);

		List<StockBook> listBooks = new ArrayList<StockBook>();

		for (Integer isbn : isbnSet) {
			listBooks.add(bookMap.get(isbn).immutableStockBook());
		}

		return listBooks;
	}

	public synchronized List<Book> getBooks(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		for (Integer isbn : isbnSet) checkValidity(isbn);

		List<Book> listBooks = new ArrayList<Book>();

		// Get the books
		for (Integer isbn : isbnSet) {
			listBooks.add(bookMap.get(isbn).immutableBook());
		}
		return listBooks;
	}

	public synchronized List<Book> getEditorPicks(int numBooks)
			throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks
					+ ", but it must be positive");
		}

		List<BookStoreBook> listAllEditorPicks = new ArrayList<BookStoreBook>();
		List<Book> listEditorPicks = new ArrayList<Book>();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet()
				.iterator();
		BookStoreBook book;

		// Get all books that are editor picks
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = (Entry<Integer, BookStoreBook>) it
					.next();
			book = (BookStoreBook) pair.getValue();
			if (book.isEditorPick()) {
				listAllEditorPicks.add(book);
			}
		}

		// Find numBooks random indices of books that will be picked
		Random rand = new Random();
		Set<Integer> tobePicked = new HashSet<Integer>();
		int rangePicks = listAllEditorPicks.size();
		if (rangePicks <= numBooks) {
			// We need to add all the books
			for (int i = 0; i < listAllEditorPicks.size(); i++) {
				tobePicked.add(i);
			}
		} else {
			// We need to pick randomly the books that need to be returned
			int randNum;
			while (tobePicked.size() < numBooks) {
				randNum = rand.nextInt(rangePicks);
				tobePicked.add(randNum);
			}
		}

		// Get the numBooks random books
		for (Integer index : tobePicked) {
			book = listAllEditorPicks.get(index);
			listEditorPicks.add(book.immutableBook());
		}
		return listEditorPicks;

	}

	@Override
	public synchronized List<Book> getTopRatedBooks(int numBooks)
			throws BookStoreException {

		if (numBooks < 1) {
			throw new BookStoreException("numBooks = " + numBooks
					+ ", but it must be positive");
		}

		// Create a sorted set with a comparator to ensure books sorted by rating.
		RatingsComparator ratingsComparator = new RatingsComparator();
		TreeSet<BookStoreBook> allBooksByRating =
		    new TreeSet<BookStoreBook>(ratingsComparator);

		// Add all books from bookmap. They will be sorted by average rating.
		allBooksByRating.addAll(bookMap.values());
		numBooks = Math.min(allBooksByRating.size(), numBooks);
		List<Book> topRated = new ArrayList<Book>(numBooks);

		Iterator<BookStoreBook> bookItr = allBooksByRating.descendingIterator();
		for (int i = 0; i < numBooks; ++i) {
			topRated.add(bookItr.next().immutableBook());
		}

		return topRated;
	}

	@Override
	public synchronized List<StockBook> getBooksInDemand()
			throws BookStoreException {
		List<StockBook> booksInDemand = new ArrayList<StockBook>();
		Collection<BookStoreBook> bookMapValues = bookMap.values();
		for (BookStoreBook book : bookMapValues) {
			if (book.hadSaleMiss()) {
				booksInDemand.add(book.immutableStockBook());
			}
		}
		return booksInDemand;
	}

	@Override
	public synchronized void rateBooks(Set<BookRating> bookRatings)
			throws BookStoreException {

		if (bookRatings == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check validity of input
		for (BookRating bookRating : bookRatings) {
			int isbn = bookRating.getISBN();
			checkValidity(isbn);
			int rating = bookRating.getRating();
			if (rating < 0 || rating > 5) {
				throw new BookStoreException("Invalid rating " + rating + " for book " +
				                             bookMap.get(isbn).getTitle());
			}
		}

		// Add ratings to store books
		for (BookRating bookRating : bookRatings) {
			bookMap.get(bookRating.getISBN()).addRating(bookRating.getRating());
		}

	}

	public synchronized void removeAllBooks() throws BookStoreException {
		bookMap.clear();
	}

	public synchronized void removeBooks(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		for (Integer isbn : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(isbn))
				throw new BookStoreException(BookStoreConstants.ISBN + isbn
						+ BookStoreConstants.INVALID);
			if (!bookMap.containsKey(isbn))
				throw new BookStoreException(BookStoreConstants.ISBN + isbn
						+ BookStoreConstants.NOT_AVAILABLE);
		}

		for (int isbn : isbnSet) {
			bookMap.remove(isbn);
		}
	}
}
