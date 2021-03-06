package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Test class to test the BookStore interface
 *
 */
public class BookStoreTest {

	private static final int TEST_ISBN = 3044560;
	private static final int NUM_COPIES = 5;
	private static boolean localTest = true;
	private static StockManager storeManager;
	private static BookStore client;

	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System
					.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean
					.parseBoolean(localTestProperty) : localTest;
			if (localTest) {
				System.out.println("Running test locally...");
				// CertainBookStore store = new CertainBookStore();
				ConcurrentCertainBookStore store = new ConcurrentCertainBookStore();
				storeManager = store;
				client = store;
			} else {
				System.out.println("Running test on HTTP proxies...");
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
			storeManager.removeAllBooks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to add some books
	 */
	public void addBooks(int isbn, int copies) throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book = new ImmutableStockBook(isbn, "Test of Thrones",
				"George RR Testin'", (float) 10, copies, 0, 0, 0, false);
		booksToAdd.add(book);
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Helper method to get the default book used by initializeBooks
	 */
	public StockBook getDefaultBook() {
		return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit",
				"JK Unit", (float) 10, NUM_COPIES, 0, 0, 0, false);
	}

	/**
	 * Method to add a book, executed before every test case is run
	 */
	@Before
	public void initializeBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(getDefaultBook());
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Method to clean up the book store, execute after every test case is run
	 */
	@After
	public void cleanupBooks() throws BookStoreException {
		storeManager.removeAllBooks();
	}

	/**
	 * Tests basic buyBook() functionality
	 */
	@Test
	public void testBuyAllCopiesDefaultBook() throws BookStoreException {
		// Set of books to buy
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES));

		// Try to buy books
		client.buyBooks(booksToBuy);

		List<StockBook> listBooks = storeManager.getBooks();
		assertTrue(listBooks.size() == 1);
		StockBook bookInList = listBooks.get(0);
		StockBook addedBook = getDefaultBook();

		assertTrue(bookInList.getISBN() == addedBook.getISBN()
				&& bookInList.getTitle().equals(addedBook.getTitle())
				&& bookInList.getAuthor().equals(addedBook.getAuthor())
				&& bookInList.getPrice() == addedBook.getPrice()
				&& bookInList.getSaleMisses() == addedBook.getSaleMisses()
				&& bookInList.getAverageRating() == addedBook
						.getAverageRating()
				&& bookInList.getTimesRated() == addedBook.getTimesRated()
				&& bookInList.getTotalRating() == addedBook.getTotalRating()
				&& bookInList.isEditorPick() == addedBook.isEditorPick());

	}

	/**
	 * Tests that books with invalid ISBNs cannot be bought
	 */
	@Test
	public void testBuyInvalidISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with invalid isbn
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(-1, 1)); // invalid

		// Try to buy the books
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		// Check pre and post state are same
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());

	}

	/**
	 * Tests that books can only be bought if they are in the book store
	 */
	@Test
	public void testBuyNonExistingISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with isbn which does not exist
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(100000, 10)); // invalid

		// Try to buy the books
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		// Check pre and post state are same
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());

	}

	/**
	 * Tests that you can't buy more books than there are copies
	 */
	@Test
	public void testBuyTooManyBooks() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy more copies than there are in store
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES + 1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());

	}

	/**
	 * Tests that you can't buy a negative number of books
	 */
	@Test
	public void testBuyNegativeNumberOfBookCopies() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a negative number of copies
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());

	}

	/**
	 * Tests that all books can be retrieved
	 */
	@Test
	public void testGetBooks() throws BookStoreException {
		Set<StockBook> booksAdded = new HashSet<StockBook>();
		booksAdded.add(getDefaultBook());

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1,
				"The Art of Computer Programming", "Donald Knuth", (float) 300,
				NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2,
				"The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES,
				0, 0, 0, false));

		booksAdded.addAll(booksToAdd);

		storeManager.addBooks(booksToAdd);

		// Get books in store
		List<StockBook> listBooks = storeManager.getBooks();

		// Make sure the lists equal each other
		assertTrue(listBooks.containsAll(booksAdded)
				&& listBooks.size() == booksAdded.size());
	}

	/**
	 * Tests that a list of books with a certain feature can be retrieved
	 */
	@Test
	public void testGetCertainBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1,
				"The Art of Computer Programming", "Donald Knuth", (float) 300,
				NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2,
				"The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES,
				0, 0, 0, false));

		storeManager.addBooks(booksToAdd);

		// Get a list of ISBNs to retrieved
		Set<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN + 1);
		isbnList.add(TEST_ISBN + 2);

		// Get books with that ISBN

		List<Book> books = client.getBooks(isbnList);
		// Make sure the lists equal each other
		assertTrue(books.containsAll(booksToAdd)
				&& books.size() == booksToAdd.size());

	}

	/**
	 * Tests that books cannot be retrieved if ISBN is invalid
	 */
	@Test
	public void testGetInvalidIsbn() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Make an invalid ISBN
		HashSet<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN); // valid
		isbnList.add(-1); // invalid

		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.getBooks(isbnList);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());

	}

	/**
	 * Spawns two threads that buy books and remove copies in an indeterministic
	 * fashion, verifying that the final amount of books correspondings to the sum
	 * of operations performed by the two threads.
	 */
	@Test
	public void testConcurrency1() throws BookStoreException {

		// Parameters to concurrency test
		int totalBooks = 1000;
		int iterations = 50;
		int booksPerIteration = totalBooks / iterations;

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1,
		               "The Art of Computer Programming", "Donald Knuth",
									 (float) 300, totalBooks, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2,
		               "The C Programming Language",
									 "Dennis Ritchie and Brian Kerninghan", (float) 50,
									 totalBooks, 0, 0, 0, false));
    storeManager.removeAllBooks();
		storeManager.addBooks(booksToAdd);

		// Add and buy same amount of books in batches determined by the number of
		// operations
		HashSet<BookCopy> copies = new HashSet<BookCopy>(2);
		copies.add(new BookCopy(TEST_ISBN+1, booksPerIteration));
		copies.add(new BookCopy(TEST_ISBN+2, booksPerIteration));
		Thread buyBooksThread =
		    new Thread(new BuyBooksProcess(client, copies, iterations));
		Thread addCopiesThread =
		    new Thread(new AddCopiesProcess(storeManager, copies, iterations));
		buyBooksThread.start();
		addCopiesThread.start();
		try {
			buyBooksThread.join();
			addCopiesThread.join();
		} catch (InterruptedException err) {
			fail();
		}

		List<StockBook> bookList = storeManager.getBooks();
		for (StockBook book : bookList) {
			assertEquals(totalBooks, book.getNumCopies());
		}

	}

	/**
	 * Continously buys/adds copies of books to the store in one thread, while
	 * another thread probes the stock a number of times to verify that the result
	 * is always in a valid state.
	 */
	@Test
	public void testConcurrency2() throws BookStoreException {

		// Number of iterations before accepting
		int repetitions = 1000;

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1,
									 "The Art of Computer Programming", "Donald Knuth",
									 (float) 300, 100, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2,
									 "The C Programming Language",
									 "Dennis Ritchie and Brian Kerninghan", (float) 50,
		               100, 0, 0, 0, false));
		storeManager.removeAllBooks();
		storeManager.addBooks(booksToAdd);

		HashSet<BookCopy> copies = new HashSet<BookCopy>(2);
		copies.add(new BookCopy(TEST_ISBN+1, 50));
		copies.add(new BookCopy(TEST_ISBN+2, 50));

		// The modifying thread continously buys/adds books until stopped
		Thread modifyThread =
		    new Thread(new ModifyProcess(client, storeManager, copies));
    modifyThread.start();

		// This thread acts as the one verifying results, no reason to spawn a new
		// one
		for (int i = 0; i < repetitions; ++i) {
			List<StockBook> bookList = storeManager.getBooks();
			int numberOfCopies = bookList.get(0).getNumCopies();
			assertTrue(numberOfCopies == 50 || numberOfCopies == 100);
			assertEquals(numberOfCopies, bookList.get(1).getNumCopies());
		}

		modifyThread.interrupt();

	}

	/**
	 * Adds and removes the same set of books in several threads, ensuring that
	 * all operations successfully release their locks, even when throwing
	 * exceptions. The main thread continously checks that the books are in a
	 * valid state.
	 */
	@Test
	public void testConcurrency3() throws BookStoreException {

		// Times to check valid state of database
		int repetitions = 1000;

		HashSet<StockBook> stockBooks = new HashSet<StockBook>(2);
		HashSet<Integer> isbns = new HashSet<Integer>(2);
		stockBooks.add(new ImmutableStockBook(TEST_ISBN + 1,
							  	 "The Art of Computer Programming", "Donald Knuth",
							 		 (float) 300, 100, 0, 0, 0, false));
		stockBooks.add(new ImmutableStockBook(TEST_ISBN + 2,
					  			 "The C Programming Language",
										"Dennis Ritchie and Brian Kerninghan", (float) 50,
										100, 0, 0, 0, false));
    isbns.add(TEST_ISBN+1);
		isbns.add(TEST_ISBN+2);

		storeManager.removeAllBooks();

		Thread addThread0 =
		    new Thread(new AddBooksProcess(storeManager, stockBooks));
		Thread addThread1 =
		    new Thread(new AddBooksProcess(storeManager, stockBooks));
		Thread removeThread0 =
		    new Thread(new RemoveBooksProcess(storeManager, isbns));
		Thread removeThread1 =
		    new Thread(new RemoveBooksProcess(storeManager, isbns));
		addThread0.start();
		addThread1.start();
		removeThread0.start();
		removeThread1.start();

		for (int i = 0; i < repetitions; ++i) {
			int size = storeManager.getBooks().size();
			assertTrue(size == 0 || size == 2);
			try {
				size = storeManager.getBooksByISBN(isbns).size();
				assertEquals(size, 2);
			} catch (BookStoreException err) {
				;
			}
		}

		addThread0.interrupt();
		addThread1.interrupt();
		removeThread0.interrupt();
		removeThread1.interrupt();

	}

	/**
	 * Stress test to perform all operations manipulating both stock books and
	 * book copies concurrently to make sure no deadlocks or unexpected exceptions
	 * occur.
	 */
	@Test
	public void testConcurrency4() throws BookStoreException {

		int repetitions = 10000;
		int threadsPerMethod = 5;

		HashSet<StockBook> stockBooks = new HashSet<StockBook>(2);
		HashSet<Integer> isbns = new HashSet<Integer>(2);
		stockBooks.add(new ImmutableStockBook(TEST_ISBN + 1,
							  	 "The Art of Computer Programming", "Donald Knuth",
							 		 (float) 300, 100, 0, 0, 0, false));
		stockBooks.add(new ImmutableStockBook(TEST_ISBN + 2,
					  			 "The C Programming Language",
										"Dennis Ritchie and Brian Kerninghan", (float) 50,
										100, 0, 0, 0, false));
    isbns.add(TEST_ISBN+1);
		isbns.add(TEST_ISBN+2);

		HashSet<BookCopy> copies = new HashSet<BookCopy>(2);
		copies.add(new BookCopy(TEST_ISBN+1, 50));
		copies.add(new BookCopy(TEST_ISBN+2, 50));

		storeManager.removeAllBooks();

		ArrayList<Thread> endlessThreads =
		    new ArrayList<Thread>(2*threadsPerMethod);
		ArrayList<Thread> limitedThreads =
		    new ArrayList<Thread>(2*threadsPerMethod);
    for (int i = 0; i < threadsPerMethod; ++i) {
			endlessThreads.add(
				new Thread(new AddBooksProcess(storeManager, stockBooks))
			);
			endlessThreads.add(
			  new Thread(new RemoveBooksProcess(storeManager, isbns))
			);
			limitedThreads.add(
				new Thread(new BuyBooksProcess(client, copies, repetitions))
			);
			limitedThreads.add(
				new Thread(new AddCopiesProcess(storeManager, copies, repetitions))
			);
		}

		for (Thread thread : endlessThreads) thread.start();
		for (Thread thread : limitedThreads) thread.start();
		for (Thread thread : limitedThreads) {
			try {
				thread.join();
			} catch (InterruptedException err) {
				fail();
			}
		}
		for (Thread thread : endlessThreads) thread.interrupt();

	}

	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}

}
