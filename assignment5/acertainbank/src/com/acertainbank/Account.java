package com.acertainbank;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {

  private final int accountId;
  private double balance;
  private final ReentrantReadWriteLock balanceLock =
      new ReentrantReadWriteLock(true);

  public Account(int accountId, double balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  public Account(int accountId) {
    this.accountId = accountId;
    balance = 0;
  }

  public Account(Account other) {
    accountId = other.getAccountId();
    balance = other.getBalance();
  }

  public int getAccountId() {
    return accountId;
  }

  public double getBalance() {
    balanceLock.readLock().lock();
    try {
      return balance;
    } finally {
      balanceLock.readLock().unlock();
    }
  }

  public void credit(double amount) throws NegativeAmountException {
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    balanceLock.writeLock().lock();
    try {
      balance += amount;
    } finally {
      balanceLock.writeLock().unlock();
    }
  }

  public void debit(double amount) throws NegativeAmountException {
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    balanceLock.writeLock().lock();
    try {
      balance -= amount;
    } finally {
      balanceLock.writeLock().unlock();
    }
  }

}
