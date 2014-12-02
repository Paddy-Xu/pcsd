package com.acertainbookstore.client.tests;

// import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class BuyBooksProcess implements Runnable {

  private final BookStore bookStore;
  private final Set<BookCopy> bookCopies;
  private final int repetitions;

  public BuyBooksProcess(BookStore bookStore, Set<BookCopy> bookCopies,
                         int repetitions) {
    this.bookStore = bookStore;
    this.bookCopies = bookCopies;
    this.repetitions = repetitions;
  }

  public void run() {
    // Random rng = new Random();
    // int waitMin = 0;
    // int waitDiff = 50 - waitMin + 1;
    for (int i = 0; i < repetitions; ++i) {
      try {
        bookStore.buyBooks(bookCopies);
      } catch (BookStoreException err) {
        return;
      }
      // try {
      //   Thread.sleep(waitMin + rng.nextInt(waitDiff));
      // } catch (InterruptedException err) {
      //   ;
      // }
    }
  }

}
