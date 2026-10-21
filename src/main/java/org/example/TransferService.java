package org.example;

public class TransferService {

    private static final double PLAFOND = 10_000;

    public void transfer(BankAccount source, BankAccount destination, double amount)
            throws BankingException {

        // Comptes null -> compte invalide
        if (source == null) {
            throw new InvalidAccountException("Le compte source est null", null);
        }
        if (destination == null) {
            throw new InvalidAccountException("Le compte destinataire est null", null);
        }

        // Montant <= 0 -> reste une IllegalArgumentException (erreur de programmation)
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif");
        }

        // Même compte -> compte invalide
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new InvalidAccountException(
                    "Le compte source et le compte destinataire sont identiques",
                    source.getAccountNumber());
        }

        // Plafond dépassé
        if (amount > PLAFOND) {
            throw new TransferLimitExceededException(PLAFOND, amount);
        }

        // Fonds insuffisants
        if (amount > source.getBalance()) {
            throw new InsufficientFundsException(source.getBalance(), amount);
        }

        // Toutes les règles sont validées : on peut modifier les comptes
        source.debit(amount);
        try {
            // Seule l'opération qui peut échouer après le débit est protégée
            destination.credit(amount);
        } catch (RuntimeException e) {
            // Compensation : panne après le débit, on restaure le compte source
            try {
                source.credit(amount);
            } catch (RuntimeException rollbackError) {
                // La compensation elle-même a échoué : on garde les deux erreurs
                e.addSuppressed(rollbackError);
                throw new BankingException(
                        "Échec du virement ET de l'annulation du débit : intervention manuelle requise", e);
            }
            // On conserve l'erreur initiale comme cause au lieu de l'ignorer
            throw new BankingException(
                    "Échec du virement, le débit a été annulé", e);
        }
    }
}