package org.example;

public class InsufficientFundsException extends BankingException {

    private final double balance;
    private final double requestedAmount;

    public InsufficientFundsException(double balance, double requestedAmount) {
        super("Fonds insuffisants : solde " + balance + " €, montant demandé " + requestedAmount + " €");
        this.balance = balance;
        this.requestedAmount = requestedAmount;
    }

    public double getBalance() { return balance; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getMissingAmount() { return requestedAmount - balance; }
}