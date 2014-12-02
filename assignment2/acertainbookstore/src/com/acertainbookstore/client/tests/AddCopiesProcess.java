package com.acertainbookstore.client.tests;

import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class AddCopiesProcess implements Runnable {

  private final StockManager stockManager;
  private final Set<BookCopy> bookCopies;
  private final int repetitions;

  public AddCopiesProcess(StockManager stockManager, Set<BookCopy> bookCopies,
                          int repetitions) {
    this.stockManager = stockManager;
    this.bookCopies = bookCopies;
    this.repetitions = repetitions;
  }

  public void run() {
    Random rng = new Random();
    int waitMin = 0;
    int waitDiff = 50 - waitMin + 1;
    for (int i = 0; i < repetitions; ++i) {
      try {
        stockManager.addCopies(bookCopies);
      } catch (BookStoreException err) {
        return;
      }
      try {
        Thread.sleep(waitMin + rng.nextInt(waitDiff));
      } catch (InterruptedException err) {
        ;
      }
    }
  }

}
