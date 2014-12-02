/**
 *
 */
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * ConcurrentCertainBookStore implements the bookstore and its functionality
 * which is defined in the BookStore
 */
public class ConcurrentCertainBookStore implements BookStore, StockManager {

	private Map<Integer, BookStoreBook> bookMap;
	private ReentrantReadWriteLock bookMapLock;

	public ConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<Integer, BookStoreBook>();
		bookMapLock = new ReentrantReadWriteLock(true);
	}

	public void addBooks(Set<StockBook> bookSet) throws BookStoreException {

		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		bookMapLock.writeLock().lock();
		try {
			// Check if all are there
			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				String bookTitle = book.getTitle();
				String bookAuthor = book.getAuthor();
				int noCopies = book.getNumCopies();
				float bookPrice = book.getPrice();
				if (BookStoreUtility.isInvalidISBN(isbn)
						|| BookStoreUtility.isEmpty(bookTitle)
						|| BookStoreUtility.isEmpty(bookAuthor)
						|| BookStoreUtility.isInvalidNoCopies(noCopies)
						|| bookPrice < 0.0) {
					throw new BookStoreException(BookStoreConstants.BOOK
							+ book.toString() + BookStoreConstants.INVALID);
				} else if (bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.DUPLICATED);
				}
			}
			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				bookMap.put(isbn, new BookStoreBook(book));
			}
		} finally {
			bookMapLock.writeLock().unlock();
		}

	}

	public void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {

		int isbn, numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		bookMapLock.writeLock().lock();
		try {
			for (BookCopy bookCopy : bookCopiesSet) {
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();
				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.INVALID);
				}
				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.NOT_AVAILABLE);
				}
				if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
					throw new BookStoreException(BookStoreConstants.NUM_COPIES
							+ numCopies + BookStoreConstants.INVALID);
				}
			}
			BookStoreBook book;
			// Update the number of copies
			for (BookCopy bookCopy : bookCopiesSet) {
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();
				book = bookMap.get(isbn);
				book.addCopies(numCopies);
			}
		} finally {
			bookMapLock.writeLock().unlock();
	  }
	}

	public List<StockBook> getBooks() {
		List<StockBook> listBooks = new ArrayList<StockBook>();
		bookMapLock.readLock().lock();
		try {
			Collection<BookStoreBook> bookMapValues = bookMap.values();
			for (BookStoreBook book : bookMapValues) {
				listBooks.add(book.immutableStockBook());
			}
			return listBooks;
	  } finally {
			bookMapLock.readLock().unlock();
		}
	}

	public void updateEditorPicks(Set<BookEditorPick> editorPicks)
			throws BookStoreException {
		// Check that all isbns that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbnVal;

		bookMapLock.writeLock().lock();
		try {
			for (BookEditorPick editorPickArg : editorPicks) {
				isbnVal = editorPickArg.getISBN();
				if (BookStoreUtility.isInvalidISBN(isbnVal)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnVal
							+ BookStoreConstants.INVALID);
				}
				if (!bookMap.containsKey(isbnVal)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnVal
							+ BookStoreConstants.NOT_AVAILABLE);
				}
			}
			for (BookEditorPick editorPickArg : editorPicks) {
				bookMap.get(editorPickArg.getISBN()).setEditorPick(
						editorPickArg.isEditorPick());
			}
		} finally {
			bookMapLock.writeLock().unlock();
		}
	}

	public void buyBooks(Set<BookCopy> bookCopiesToBuy)
			throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all isbns that we buy are there first.
		int isbn;
		BookStoreBook book;
		Boolean saleMiss = false;
		bookMapLock.writeLock().lock();
		try {
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				isbn = bookCopyToBuy.getISBN();
				if (bookCopyToBuy.getNumCopies() < 0) {
					throw new BookStoreException(BookStoreConstants.NUM_COPIES
							+ bookCopyToBuy.getNumCopies()
							+ BookStoreConstants.INVALID);
				}
				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.INVALID);
				}
				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.NOT_AVAILABLE);
				}
				book = bookMap.get(isbn);
				if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
					book.addSaleMiss(); // If we cannot sell the copies of the book
										// its a miss
					saleMiss = true;
				}
			}

			// We throw exception now since we want to see how many books in the
			// order incurred misses which is used by books in demand
			if (saleMiss) {
				throw new BookStoreException(BookStoreConstants.BOOK
						+ BookStoreConstants.NOT_AVAILABLE);
			}

			// Then make purchase
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				book = bookMap.get(bookCopyToBuy.getISBN());
				book.buyCopies(bookCopyToBuy.getNumCopies());
			}

		} finally {
		  bookMapLock.writeLock().unlock();
	  }

	}


	public List<StockBook> getBooksByISBN(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		bookMapLock.readLock().lock();
		try {
			for (Integer isbn : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.INVALID);
				}
				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.NOT_AVAILABLE);
				}
			}
			List<StockBook> listBooks = new ArrayList<StockBook>();
			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableStockBook());
			}
			return listBooks;
		} finally {
			bookMapLock.readLock().unlock();
		}

	}

	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		bookMapLock.readLock().lock();
		// Check that all isbns that we rate are there first.
		try {
			for (Integer isbn : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.INVALID);
				}
				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn
							+ BookStoreConstants.NOT_AVAILABLE);
				}
			}
			List<Book> listBooks = new ArrayList<Book>();
			// Get the books
			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableBook());
			}
			return listBooks;
		} finally {
			bookMapLock.readLock().unlock();
		}

	}

	public List<Book> getEditorPicks(int numBooks)
			throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks
					+ ", but it must be positive");
		}

		List<BookStoreBook> listAllEditorPicks = new ArrayList<BookStoreBook>();
		List<Book> listEditorPicks = new ArrayList<Book>();

		bookMapLock.readLock().lock();
		try {
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

		} finally {
			bookMapLock.readLock().unlock();
		}

	}

	@Override
	public List<Book> getTopRatedBooks(int numBooks)
			throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	@Override
	public List<StockBook> getBooksInDemand()
			throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	@Override
	public void rateBooks(Set<BookRating> bookRating)
			throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	public void removeAllBooks() throws BookStoreException {
		bookMapLock.writeLock().lock();
		try {
			bookMap.clear();
		} finally {
			bookMapLock.writeLock().unlock();
		}
	}

	public void removeBooks(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		bookMapLock.writeLock().lock();
		try {
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
		} finally {
			bookMapLock.writeLock().unlock();
		}
	}
}
