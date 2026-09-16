# Vérification 0.3.1 — 15 septembre 2026

By FastedCorsi

Interface centrée, réduite de 20 % en largeur et en hauteur. La grille garde ses douze emplacements et ses proportions ; coordonnées des clics, survols et glisser-déposer suivent la nouvelle échelle.

Les doubles types se séparent désormais en diagonale. La seconde couleur réutilise le centre de la texture Cobblemon dans un unique triangle, sans découpage ligne par ligne. Le cadre Tropimon est réservé au panneau extérieur ; les cartes, filtres, menus, informations et confirmations utilisent les cadres Cobblemon.

Compilation Java 21 / Minecraft 1.21.1 / Cobblemon 1.7.2 réussie. Les 26 tests unitaires passent. Le scénario en client de production autonome passe également : interaction PC, capacité synchronisée, nature après menthe, saisie accentuée, filtres enregistrés, sélection de page/tous résultats, panneau des attaques, protections, annulation, glisser-déposer entre PC/équipe/boîte et deux relâchements confirmés par le serveur intégré.

Contrôle visuel de la collection en 1400 × 900 : marges extérieures visibles, séparation diagonale des doubles types et cadre Tropimon limité au contour global. Contrôle de confidentialité des sources, des JAR et des scripts locaux ; comparaison des deux livrables par SHA-256. L'installation effective est contrôlée séparément dans `build/delivery/local/install-status.json`.

Les vérifications utilisent un monde jetable et des Pokémon fictifs, pas une collection réelle du serveur Tropimon. Les autres résolutions et tous les packs tiers ne sont pas vérifiés sur cette révision. Le filtre temporel conserve son estimation locale et les capacités restent celles envoyées par le serveur. Les contrôles ne couvrent pas les historiques et copies déjà diffusées.
