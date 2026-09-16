# Vérification 0.2.1 — 14 septembre 2026

By FastedCorsi

## Typographie

Police native conservée avec ses remplacements par les packs de ressources. Titres agrandis, noms en gras, ombres de contraste et valeurs IV/EV en gras centrées sous leurs statistiques. La largeur des noms est mesurée avec leur style et leur taille avant troncature. Couleurs et textures Cobblemon conservées, aucune police ni ressource externe ajoutée.

## Vérifications

- Compilation Java 21 / Minecraft 1.21.1 / Cobblemon 1.7.2 et 26 tests unitaires réussis.
- Scénario complet dans le client de production autonome et un monde jetable : filtres, recherche enregistrée, favoris, confirmation et annulation, glisser-déposer PC/équipe/boîte, deux relâchements confirmés et fermeture du PC.
- Contrôle visuel des cartes et de la confirmation : noms, six colonnes IV/EV, titres et textes sans chevauchement dans la fenêtre de vérification de 1400 × 900.
- Le premier passage a révélé un shiny aléatoire supplémentaire dans les données fictives. Le générateur du test définit désormais explicitement le statut shiny de chaque Pokémon ; le scénario complet repasse.
- Contrôle de confidentialité des sources et des artefacts par le contrôle de build. Les deux exemplaires livrés sont comparés par SHA-256 et contrôlés avec les scripts externes locaux.

## Périmètre

Test en serveur intégré, sans connexion au serveur Tropimon ni utilisation de collection réelle. Les remplacements de police de tous les packs tiers et toutes les résolutions ne sont pas vérifiés. La compatibilité TeamBuilder/Catch Preview a été testée sur 0.2.0 ; cette révision modifie uniquement la présentation, la documentation et les données du scénario de vérification.

Le filtre temporel reste une première détection locale et les capacités restent celles synchronisées par le serveur. Cette vérification ne couvre pas les historiques ou anciennes copies distribuées. L’état réel de la mise à jour locale est consigné dans `build/delivery/local/install-status.json`, puis la copie installée est vérifiée par SHA-256.
