package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

  private final Random rng;
	private Integer isbnCounter;

	public BookSetGenerator() {
		rng = new Random();
		isbnCounter = rng.nextInt();
	}

	public BookSetGenerator(int startIsbn) {
		rng = new Random();
		isbnCounter = startIsbn;
	}

	/**
	 * @param isbns Input set of ISBNs to sample from.
	 * @param num Number of ISBNs to sample.
	 * @return Set of sampled ISBNs.
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num)
	    throws BookStoreException {

		// First handle invalid or trivial input size
	  int maxSize = isbns.size();
		if (num < 1 || num > maxSize) {
			throw new BookStoreException("Invalid parameters received");
		}
		if (num == maxSize) return new HashSet<Integer>(isbns);

		// Now the actual sampling. This is done by shuffling a list of the input
		// set content and adding the desired amount to an output set
		HashSet<Integer> samples = new HashSet<Integer>(num);
		ArrayList<Integer> isbnList = new ArrayList<Integer>(isbns);
		java.util.Collections.shuffle(isbnList);
		for (int i = 0; i < num; ++i) {
			samples.add(isbnList.get(i));
		}

		return samples;
	}

	/**
	 * Generated random ImmutableStockBook instances. A given instantiation of
	 * this class is guaranteed to never return a book with the same ISBN as a
	 * previously generated one, limited to Integer.MAX_INT number of books, at
	 * which point numbers will start to reappear. 10% of the returned books will
	 * be editor picks. 20% will have sales misses.
	 *
	 * @param num Number of random stock books to generate.
	 * @return Set of randomly generated ImmutableStockBook instances.
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) throws BookStoreException {

		if (num < 1) throw new BookStoreException("Invalid parameters received");
		if (isbnCounter > Integer.MAX_VALUE - num) isbnCounter = 1;

		long timesRated = rng.nextLong() % 1000;
		HashSet<StockBook> samples = new HashSet<StockBook>(num);
		for (int i = 0; i < num; ++i) {
			samples.add(new ImmutableStockBook(
				isbnCounter,                                  // ISBN
				"Mastering C++ Vol. " + isbnCounter++,        // Title
				"Scott Meyers",                               // Author
				100 + 900*rng.nextFloat(),                    // Price
				100*(1 + rng.nextInt() % 10),                 // Number of copies
				rng.nextInt(6) > 4 ? rng.nextInt(20) : 0,	  	// Sales misses
				timesRated,                                   // Number of ratings
				(long)(5.*(float)timesRated*rng.nextFloat()), // Average rating
				rng.nextInt(11) > 9                           // Is an editor pick
			));
		}

		return samples;
	}

}
