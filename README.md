# TD2 – Gestion des exceptions en Java

**Stratégie :** `TransferService.transfer()` valide toutes les règles **avant** de toucher aux comptes et lève (`throw`) une exception dès qu'une règle est violée. Les erreurs métier héritent de `BankingException` (checked, déclarée par `throws`) : `InsufficientFundsException`, `TransferLimitExceededException`, `InvalidAccountException`. Un montant ≤ 0 lève une `IllegalArgumentException` standard (argument invalide).
Chaque exception métier porte des attributs utiles (solde, plafond, numéro de compte) pour que l'appelant affiche un message précis.
`Main` (Scanner) utilise plusieurs `catch` spécifiques (saisie invalide, fonds, plafond, compte), puis un `catch (BankingException)` de repli et un `finally` qui affiche « Traitement de la transaction terminé. » et ferme le Scanner.

**Cohérence :** seule l'opération `credit()` qui suit le débit est entourée d'un `try/catch (RuntimeException)`. En cas de panne, le débit est compensé (`source.credit(amount)`) et une `BankingException` est relancée avec l'erreur d'origine comme **cause**. Si la compensation échoue elle aussi, l'erreur est ajoutée en `suppressed` pour ne rien perdre.

**Exécution :** `Main` (interactif) et `TransferScenarios` (les 7 scénarios de test, dont la panne simulée entre débit et crédit). Les réponses aux questions sont dans [REPONSES.md](REPONSES.md).
