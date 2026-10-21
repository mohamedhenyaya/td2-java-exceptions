package org.example;

/**
 * Exécute les scénarios de test attendus du TD sans saisie clavier.
 */
public class TransferScenarios {

    /** Compte qui simule une panne inattendue au moment du crédit. */
    static class FailingBankAccount extends BankAccount {
        public FailingBankAccount(String accountNumber, double balance) {
            super(accountNumber, balance);
        }

        @Override
        public void credit(double amount) {
            throw new IllegalStateException("Panne simulée pendant le crédit");
        }
    }

    private static final TransferService service = new TransferService();

    public static void main(String[] args) {
        // 1. Virement de 500 € depuis un solde de 2 000 €
        BankAccount a = new BankAccount("FR001", 2000);
        BankAccount b = new BankAccount("FR002", 0);
        run("1. Virement de 500 € (solde 2 000 €)", a, b, 500);
        System.out.println("   Soldes : source = " + a.getBalance() + " €, destinataire = " + b.getBalance() + " €");

        // 2. Virement de 3 000 € depuis un solde de 2 000 €
        run("2. Virement de 3 000 € (solde 2 000 €)",
                new BankAccount("FR001", 2000), new BankAccount("FR002", 0), 3000);

        // 3. Virement de 12 000 €
        run("3. Virement de 12 000 €",
                new BankAccount("FR001", 20000), new BankAccount("FR002", 0), 12000);

        // 4. Virement de -100 €
        run("4. Virement de -100 €",
                new BankAccount("FR001", 2000), new BankAccount("FR002", 0), -100);

        // 5. Compte source identique au compte destinataire
        BankAccount same = new BankAccount("FR001", 2000);
        run("5. Source = destinataire", same, same, 100);

        // 6. Saisie « abc » comme montant
        System.out.println("6. Saisie « abc »");
        try {
            double amount = Double.parseDouble("abc");
            service.transfer(new BankAccount("FR001", 2000), new BankAccount("FR002", 0), amount);
        } catch (NumberFormatException e) {
            System.out.println("   -> NumberFormatException gérée : saisie invalide, pas de plantage");
        } catch (BankingException e) {
            System.out.println("   -> " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }

        // 7. Défaillance entre débit et crédit
        BankAccount src = new BankAccount("FR001", 2000);
        BankAccount failing = new FailingBankAccount("FR002", 0);
        run("7. Panne entre débit et crédit (500 €, solde 2 000 €)", src, failing, 500);
        System.out.println("   Solde source après compensation : " + src.getBalance() + " €");
    }

    private static void run(String label, BankAccount source, BankAccount destination, double amount) {
        System.out.println(label);
        try {
            service.transfer(source, destination, amount);
            System.out.println("   -> Succès");
        } catch (IllegalArgumentException e) {
            System.out.println("   -> Virement rejeté (IllegalArgumentException) : " + e.getMessage());
        } catch (BankingException e) {
            System.out.println("   -> " + e.getClass().getSimpleName() + " : " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("      cause : " + e.getCause().getMessage());
            }
        }
    }
}
