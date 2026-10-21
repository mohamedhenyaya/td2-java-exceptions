package org.example;

public class InvalidAccountException extends BankingException {

    private final String accountNumber;

    public InvalidAccountException(String message, String accountNumber) {
        super(message);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() { return accountNumber; }
}