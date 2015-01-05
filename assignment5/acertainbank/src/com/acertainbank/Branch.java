package com.acertainbank;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Branch {

  HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();
  // The lock is for modification of the account table only, not for the
  // individual accounts which are synchronized internally.
  ReentrantReadWriteLock accountsLock = new ReentrantReadWriteLock(true);

  public void credit(int accountId, double amount)
      throws InexistentAccountException, NegativeAmountException {
     if (amount < 0) {
       throw new NegativeAmountException(amount);
     }
     accountsLock.readLock().lock();
     try {
       if (!accounts.containsKey(accountId)) {
         throw new InexistentAccountException(accountId);
       }
       accounts.get(accountId).changeBalance(amount);
     } finally {
       accountsLock.readLock().unlock();
     }
   }

  public void debit(int accountId, double amount)
      throws InexistentAccountException, NegativeAmountException {
     if (amount < 0) {
       throw new NegativeAmountException(amount);
     }
     accountsLock.readLock().lock();
     try {
       if (!accounts.containsKey(accountId)) {
         throw new InexistentAccountException(accountId);
       }
       accounts.get(accountId).changeBalance(-amount);
     } finally {
       accountsLock.readLock().unlock();
     }
  }

  public void transfer(int accountIdOrig, int accountIdDest, double amount)
      throws InexistentAccountException, NegativeAmountException {
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    accountsLock.readLock().lock();
    try {
      if (!accounts.containsKey(accountIdOrig)) {
        throw new InexistentAccountException(accountIdOrig);
      }
      if (!accounts.containsKey(accountIdDest)) {
        throw new InexistentAccountException(accountIdDest);
      }
      accounts.get(accountIdOrig).changeBalance(-amount);
      accounts.get(accountIdDest).changeBalance(amount);
    } finally {
      accountsLock.readLock().unlock();
    }
  }

  public double calculateExposure(int branchId)
      throws InexistentBranchException {
    // NYI
    return 0f;
  }

}
