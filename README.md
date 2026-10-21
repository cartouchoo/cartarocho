# Cartarocho

Application Android pour compter les points au tarot, sans papier ni calculatrice.

## Fonctionnalités

- **Joueurs** : crée ta liste de joueurs une fois, réutilise-la à chaque partie.
- **Parties à 3, 4 ou 5 joueurs** : choisis les joueurs, donne un nom à la partie si tu veux.
- **Saisie des donnes** : preneur, appelé (à 5), contrat, nombre de bouts, points réalisés (demi-points acceptés), petit au bout, poignée, chelem et misères. Le score de chacun est calculé automatiquement.
- **Points par photo des plis** : prends en photo les cartes du preneur, étalées avec les coins visibles. L'app lit les numéros d'atouts et les lettres des têtes (R, D, C, V), puis affiche un écran de vérification avant de remplir les points et les bouts. La lecture se fait sur le téléphone, sans connexion. L'Excuse et les cartes basses n'ont pas d'index lisible : on les complète à la main.
- **Tableau de la partie** : scores cumulés, historique des donnes, modification ou suppression d'une donne.
- **Statistiques** par joueur : parties jouées, victoires, prises et taux de réussite, chelems, poignées et misères.
- **Règles** du tarot consultables dans l'app.

Les données restent sur le téléphone (base locale), aucun compte ni connexion nécessaire.

## Règles de calcul

Calcul selon les règles officielles de la FFT :

| Contrat      | Coefficient |
|--------------|-------------|
| Petite       | ×1          |
| Garde        | ×2          |
| Garde sans   | ×4          |
| Garde contre | ×6          |

- Points à réaliser : 56 (0 bout), 51 (1), 41 (2), 36 (3).
- Valeur de la donne : (25 + écart) × coefficient, plus petit au bout (10 × coefficient), poignée (20 / 30 / 40) et chelem (400 annoncé, 200 non annoncé, −200 annoncé et chuté).
- Répartition : à 3 et 4 joueurs le preneur marque contre chaque défenseur ; à 5, le preneur marque double et l'appelé simple (quadruple si le preneur s'appelle lui-même).

**Règle maison — la misère** : un joueur peut annoncer une misère d'atout ou de têtes. Il gagne 10 points de chaque autre joueur, indépendamment du résultat du contrat.

## Prérequis

- Android 8.0 (API 26) ou plus récent
- Pour compiler : Android Studio récent, JDK 17+

## Compiler et installer

```bash
# Compiler l'APK de debug
./gradlew assembleDebug

# Installer sur un téléphone branché (débogage USB activé)
./gradlew installDebug

# Lancer les tests unitaires (calcul des scores, statistiques)
./gradlew test
```

Sous Windows, remplace `./gradlew` par `gradlew.bat`.

## Technique

- Kotlin, Jetpack Compose et Material 3
- Room pour la base de données locale
- ML Kit (reconnaissance de texte sur l'appareil) pour la lecture des cartes
- Navigation Compose, architecture ViewModel + repository
- Le moteur de calcul est isolé dans `domain/TarotScoreCalculator.kt` et couvert par des tests unitaires, comme le comptage des cartes (`domain/CardCount.kt`, `domain/TrickPhotoParser.kt`).
