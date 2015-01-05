package com.acertainbank;

public class Account {

  private final int accountId;
  private double balance;

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

  public synchronized double getBalance() {
    return balance;
  }

  public synchronized void credit(double amount)
      throws NegativeAmountException {
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    balance += amount;
  }

  public synchronized void debit(double amount) throws NegativeAmountException {
    if (amount < 0) {
      throw new NegativeAmountException(amount);
    }
    balance -= amount;
  }

}
