/**
 *
 */
package com.acertainbookstore.client.workloads;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.business.Book;
import com.acertainbookstore.utils.BookStoreException;

/**
 *
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 *
 */
public class Worker implements Callable<WorkerRunResult> {

	private final BookSetGenerator generator;
	private final WorkloadConfiguration configuration;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;

	public Worker(WorkloadConfiguration config) {
		generator = new BookSetGenerator();
		configuration = config;
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 *
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 *
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			if (chooseInteraction < configuration
					.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration
					.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction();
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction();
				numSuccessfulFrequentBookStoreInteraction++;
			}
		} catch (BookStoreException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 *
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
		Set<StockBook> newBooks =
		    generator.nextSetOfStockBooks(configuration.getNumBooksToBuy());
		List<StockBook> existingBooks = configuration.getStockManager().getBooks();
		for (StockBook book : existingBooks) {
			newBooks.remove(book);
		}
		configuration.getStockManager().addBooks(newBooks);
	}

	/**
	 * Runs the stock replenishment interaction
	 *
	 * @throws BookStoreException
	 */
	private void runFrequentStockManagerInteraction() throws BookStoreException {
		List<StockBook> allBooks = configuration.getStockManager().getBooks();
		TreeMap<Long, StockBook> lowestStock = new TreeMap<Long, StockBook>();
		for (StockBook book : allBooks) {
			lowestStock.put(book.getTotalRating(), book);
		}
		int numCopies = configuration.getNumAddCopies();
		HashSet<BookCopy> copiesToAdd = new HashSet<BookCopy>(numCopies);
		Iterator<StockBook> lowestIterator = lowestStock.values().iterator();
		for (int i = 0; i < numCopies; ++i) {
			copiesToAdd.add(new BookCopy(lowestIterator.next().getISBN(),
			                             numCopies));
		}
		configuration.getStockManager().addCopies(copiesToAdd);
	}

	/**
	 * Runs the customer interaction
	 *
	 * @throws BookStoreException
	 */
	private void runFrequentBookStoreInteraction() throws BookStoreException {
		List<Book> editorPicks = configuration.getBookStore().getEditorPicks(
		    configuration.getNumEditorPicksToGet());
		HashSet<Integer> editorISBNs =
		    new HashSet<Integer>(configuration.getNumEditorPicksToGet());
		for (Book book : editorPicks) {
			editorISBNs.add(book.getISBN());
		}
		Set<Integer> sampleISBNs =
		    generator.sampleFromSetOfISBNs(editorISBNs,
				                               configuration.getNumBooksToBuy());
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>(sampleISBNs.size());
		for (Book book : editorPicks) {
			int isbn = book.getISBN();
			if (sampleISBNs.contains(isbn)) {
				booksToBuy.add(
				    new BookCopy(isbn, configuration.getNumBookCopiesToBuy()));
			}
		}
		configuration.getBookStore().buyBooks(booksToBuy);
	}

}
