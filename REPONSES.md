# Réponses aux questions – TD2 Exceptions

## Partie 1 – Exceptions standard
À ce stade (avant la hiérarchie métier), les exceptions standard adaptées sont :
- `IllegalArgumentException` pour un montant ≤ 0, un montant > 10 000 € ou des comptes identiques (argument invalide) ;
- `NullPointerException` / `IllegalArgumentException` pour un compte `null` (ex. `Objects.requireNonNull`) ;
- `IllegalStateException` pour des fonds insuffisants (l'état du compte ne permet pas l'opération).

Dans la version finale, les règles métier utilisent les exceptions personnalisées. Seul le montant ≤ 0 reste une `IllegalArgumentException`, car c'est une donnée d'entrée invalide et non un refus métier.

## Partie 3 – Pourquoi `finally` ?
Le bloc `finally` s'exécute **dans tous les cas** : succès, exception interceptée ou non interceptée. C'est donc le bon endroit pour le code de fin de traitement qui doit toujours s'exécuter (message, nettoyage).
Dans une application réelle, on y libère : connexions à la base de données, transactions ouvertes, verrous (`Lock.unlock()`), fichiers, flux réseau/sockets, `Scanner`/flux d'entrée. En pratique, on préfère `try-with-resources` pour les objets `AutoCloseable`.

## Partie 4 – Analyse de la défaillance (2 000 €, virement de 500 €)
1. **Soldes finaux :** source = 1 500 €, destinataire inchangé. 500 € ont « disparu ».
2. **Le try/catch a-t-il annulé le débit ?** Non. Une exception interrompt le flux d'exécution mais n'annule **aucune** modification déjà faite en mémoire. Java ne fait pas de rollback automatique.
3. **Le système est-il cohérent ?** Non. L'invariant « la somme des soldes est constante » est violé.
4. **Comment repenser l'opération :** valider tout avant de modifier, puis soit compenser (re-créditer la source si le crédit échoue), soit rendre l'opération atomique (transaction avec commit/rollback, ou calculer les nouveaux soldes puis les appliquer en une seule étape).

## Partie 5 – Justification de la compensation
- Le `try` entoure **uniquement** `destination.credit(amount)`, la seule instruction dont l'échec laisse un état incohérent. Les erreurs de validation se produisent avant tout débit et n'ont rien à annuler.
- On intercepte `RuntimeException` (pannes techniques imprévues), pas `Exception` : les exceptions métier checked ne sont pas masquées et on ne capture pas les `Error` (ex. `OutOfMemoryError`).
- L'erreur initiale n'est pas ignorée : elle est transmise comme **cause** de la `BankingException` relancée. Si la compensation échoue aussi, cette seconde erreur est attachée avec `addSuppressed`.

## Partie 6 – Option A ou Option B ?
- **Application utilisateur : Option B.** Chaque cas donne un message précis et une action différente (« il vous manque X € », « le plafond est de 10 000 € », « vérifiez le numéro de compte »). On garde un `catch (BankingException)` final pour les cas non prévus.
- **Couche de journalisation : Option A.** Le logger traite toutes les erreurs métier de la même façon (type, message, cause, stack trace), et un seul `catch` suffit.
- **Contrôleur API :** en général **Option B** (ou un gestionnaire global qui fait la correspondance type → code HTTP) : 422/409 pour fonds insuffisants ou plafond, 404/400 pour compte invalide, avec un code d'erreur propre à chaque cas. L'option A suffit si l'API renvoie une réponse générique unique.

## Questions de discussion
1. **Exception métier spécifique vs `throw Exception` :** le type porte le sens de l'erreur. L'appelant peut la traiter de façon ciblée, elle porte des données utiles (solde, plafond…), la signature est explicite, et on évite de forcer `catch (Exception)`.
2. **Si `BankingException` hérite de `RuntimeException` :** elle devient *unchecked*. Le compilateur n'oblige plus à la déclarer (`throws`) ni à la gérer, donc l'appelant peut l'oublier et le programme plante. Le code est plus léger mais le contrat est moins visible. Les `catch` spécifiques fonctionnent toujours.
3. **Lever / déclarer / gérer :** *lever* = `throw new X()` (signaler l'erreur) ; *déclarer* = `throws X` dans la signature (prévenir l'appelant que la méthode peut la propager) ; *gérer* = `try/catch` (intercepter et réagir).
4. **`catch (Exception e)` systématique :** il masque les bugs (`NullPointerException`…) et mélange erreurs métier et techniques. On perd la réaction spécifique, on avale souvent l'erreur silencieusement, et l'état peut rester incohérent sans que personne le sache.
5. **Exceptions techniques vs métier au même niveau ?** Non. Les exceptions métier se gèrent près de l'utilisateur (message, nouvelle saisie). Les exceptions techniques (BD, réseau) se gèrent plutôt à un niveau global : log, retry, rollback, message générique. Elles sont souvent encapsulées dans une exception de plus haut niveau.
6. **Intercepter garantit-il la cohérence ?** Non. Le `catch` reprend seulement le contrôle du flux ; les modifications déjà faites restent. C'est au développeur de restaurer l'état (compensation) ou d'utiliser un mécanisme atomique (voir Partie 4).
7. **En production :** les **transactions de la base de données** (ACID : `BEGIN` / `COMMIT` / `ROLLBACK`), par ex. via JDBC `setAutoCommit(false)` + `rollback()` ou `@Transactional` (Spring). Pour des systèmes distribués : commit à deux phases ou pattern Saga.
