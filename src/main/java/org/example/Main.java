package org.example;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Quelques comptes existants
        Map<String, BankAccount> comptes = new HashMap<>();
        comptes.put("FR001", new BankAccount("FR001", 500));
        comptes.put("FR002", new BankAccount("FR002", 100));

        TransferService service = new TransferService();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Numéro du compte source (FR001, FR002) : ");
            BankAccount source = comptes.get(scanner.next());          // null si inexistant

            System.out.print("Numéro du compte destinataire (FR001, FR002) : ");
            BankAccount destination = comptes.get(scanner.next());     // null si inexistant

            System.out.print("Montant du virement : ");
            double montant = scanner.nextDouble();                     // peut lever InputMismatchException

            service.transfer(source, destination, montant);

            System.out.println("Virement de " + montant + " € effectué avec succès.");
            System.out.println("Nouveau solde source : " + source.getBalance() + " €");
            System.out.println("Nouveau solde destinataire : " + destination.getBalance() + " €");

        } catch (InputMismatchException e) {
            System.out.println("Saisie invalide : veuillez entrer un nombre (ex : 150,50).");
        } catch (IllegalArgumentException e) {
            System.out.println("Montant invalide : " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Fonds insuffisants : il manque " + e.getMissingAmount() + " €.");
        } catch (TransferLimitExceededException e) {
            System.out.println("Plafond dépassé : le maximum autorisé est " + e.getLimit() + " €.");
        } catch (InvalidAccountException e) {
            System.out.println("Compte invalide : " + e.getMessage());
        } catch (BankingException e) {
            // Autres erreurs bancaires, par ex. panne pendant le crédit (débit annulé)
            System.out.println("Erreur bancaire : " + e.getMessage());
        } finally {
            System.out.println("Traitement de la transaction terminé.");
            scanner.close();
        }
    }
}