package com.acertainbank;

import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Branch {

  private final HashMap<Integer, Account> accounts;
  // The lock is for modification of the account table only, not for the
  // individual accounts, as they are synchronized internally.
  private final ReentrantReadWriteLock accountsLock =
      new ReentrantReadWriteLock(true);

  public Branch(List<Account> initialAccounts) {
    accounts = new HashMap<Integer, Account>(initialAccounts.size());
    for (Account account : initialAccounts) {
      accounts.put(account.getAccountId(), new Account(account));
    }
  }

  public Branch() {
    accounts = new HashMap<Integer, Account>();
  }

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
      accounts.get(accountId).credit(amount);
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
      accounts.get(accountId).debit(amount);
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
      accounts.get(accountIdOrig).debit(amount);
      accounts.get(accountIdDest).credit(amount);
    } finally {
      accountsLock.readLock().unlock();
    }
  }

  public double calculateExposure() {
    accountsLock.readLock().lock();
    double sum = 0;
    try {
      for (Account account : accounts.values()) {
        sum += Math.min(account.getBalance(), 0);
      }
    } finally {
      accountsLock.readLock().unlock();
    }
    return sum;
  }

  public void addAccount(int accountId, double initialBalance)
      throws ExistentAccountException, IllegalArgumentException {
    if (accountId < 0) {
      throw new IllegalArgumentException("Account id cannot be negative");
    }
    accountsLock.readLock().lock();
    try {
      if (accounts.containsKey(accountId)) {
        throw new ExistentAccountException(accountId);
      }
    } finally {
      accountsLock.readLock().unlock();
    }
    accountsLock.writeLock().lock();
    try {
      accounts.put(accountId, new Account(accountId, initialBalance));
    } finally {
      accountsLock.writeLock().unlock();
    }
  }

  public void addAccount(int accountId)
      throws ExistentAccountException, IllegalArgumentException {
    addAccount(accountId, 0);
  }

  public void removeAccount(int accountId)
      throws IllegalArgumentException, InexistentAccountException {
    if (accountId < 0) {
      throw new IllegalArgumentException("Account id cannot be negative");
    }
    accountsLock.readLock().lock();
    try {
      if (!accounts.containsKey(accountId)) {
        throw new InexistentAccountException(accountId);
      }
    } finally {
      accountsLock.readLock().unlock();
    }
    accountsLock.writeLock().lock();
    try {
      accounts.remove(accountId);
    } finally {
      accountsLock.writeLock().unlock();
    }
  }

}
