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

  public int getAccountId() {
    return accountId;
  }

  public synchronized double getBalance() {
    return balance;
  }

  /**
   * @param amount Amount to add (positive value) or subtract (negative value)
   *               from the account.
   * @return Balance after the change.
   */
  public synchronized double changeBalance(double amount) {
    balance += amount;
    return balance;
  }

}
