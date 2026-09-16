# Vérification 0.2.0 — 14 septembre 2026

By FastedCorsi

## Changements livrés

Interface colorée avec les textures de Cobblemon, neuf cartes détaillées par page, six IV et six EV directement visibles, talent actif normal/caché, icônes de types et de Poké Ball. Le pourcentage global et l'emplacement ont été retirés des cartes. Les couleurs des IV reprennent celles utilisées dans les mods Tropimon, avec une implémentation indépendante.

Menus de filtres dédiés, recherches enregistrées, favoris protégés, exclusions configurables, sélection Ctrl/Maj, sélection par page ou sur tous les résultats et glisser-déposer vers l'équipe ou une boîte nommée avec son occupation. Les compteurs utilisent la capacité synchronisée par le serveur et les boîtes contenant au moins un Pokémon.

## Vérifications réalisées

- Compilation Java 21 / Fabric, Minecraft 1.21.1 et Cobblemon 1.7.2.
- 26 tests automatisés réussis : historique, filtres, file de relâchement, favoris et recherches persistants, séparation des comptes/serveurs, fichiers illisibles conservés, exclusions indépendantes, Ctrl, Maj, plages inversées et sélection entre plusieurs pages.
- Client de production dans un monde jetable avec Pokémon fictifs, sans autre mod Tropimon, puis avec TeamBuilder 0.59.10 et Catch Preview 0.6.6.
- PC serveur limité et synchronisé à trois boîtes : affichage de 14/90 Pokémon et 1/3 boîtes utilisées, puis 2/3 après glisser-déposer dans une deuxième boîte.
- Distinction du talent caché Paratonnerre de Pikachu et du second talent normal Paratonnerre d'Osselait. IV et EV de valeurs connues transmis par le serveur et affichés sur la carte.
- Filtre de type et filtre de talent caché via les menus ; enregistrement et rappel d'une recherche via l'interface.
- Sélection de neuf Pokémon de la page, puis des quatorze résultats ; exclusion du favori et du shiny de la liste de relâchement ; annulation conservant tous les Pokémon.
- Glisser-déposer du PC vers l'équipe, puis de l'équipe vers une boîte choisie dans le menu ; confirmations réelles du serveur intégré.
- Deux relâchements confirmés, conservation des autres Pokémon et fermeture limitée au PC concerné.
- Contrôle visuel des cartes, des modèles, couleurs, statistiques et confirmations. Contrôle de confidentialité des sources et des JAR ; aucun asset ni code d'un autre mod Tropimon embarqué.

## Limites

Les tests utilisent le serveur intégré officiel, pas une collection réelle du serveur Tropimon. Aucun grade n'est deviné : le serveur doit transmettre sa capacité autorisée au client. Le filtre temporel reste une première détection locale, pas une date de capture certifiée. Les favoris, exclusions et recherches restent locaux au compte et au serveur/monde.

Cette vérification porte sur le module et ses nouveaux livrables, pas sur les anciens historiques ou copies diffusées. L'installation réelle est consignée dans `build/delivery/local/install-status.json` et contrôlée par SHA-256.
