package org.example;

public class TransferLimitExceededException extends BankingException {

    private final double limit;
    private final double amount;

    public TransferLimitExceededException(double limit, double amount) {
        super("Plafond dépassé : maximum " + limit + " €, montant demandé " + amount + " €");
        this.limit = limit;
        this.amount = amount;
    }

    public double getLimit() { return limit; }
    public double getAmount() { return amount; }
}