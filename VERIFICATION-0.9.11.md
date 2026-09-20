# Vérification 0.9.11

By FastedCorsi

Les cartes affichent une ligne Baron / Non-Baron (Alpha / Non-Alpha en anglais), calculée depuis les données natives de Cobblemon. Un statut indisponible est annoncé comme tel ; il n'est pas transformé en réponse négative. Les cartes conservent leurs dimensions, leurs tags et les six IV/EV.

- Compilation et 58 tests réussis avec Cobblemon 1.8.1 actif et la version minimale 1.7.2.
- Capture du client français en 1280 × 720 inspectée : un Baron avec les cinq tags et des Pokémon non-Barons sur la même page, statut distinct, lignes de statistiques conservées.
- Scénario complet du client isolé réussi : filtres, tags, sélection, transferts et protections des relâchements.
- Métadonnées des deux JAR vérifiées : version 0.9.11, environnement client, Cobblemon >=1.7.2 et attribution By FastedCorsi. Les deux copies et leurs SHA-256 correspondent.
- Contrôle de confidentialité réussi sur les sources, JAR et accompagnements locaux. Aucun outil local ni classe de test dans les JAR.

Cette modification concerne l'affichage des cartes. L'installation gérée conserve son mécanisme existant ; aucune modification de l'auto-updater embarqué. Les contrôles ne modifient ni l'historique ni les copies déjà diffusées.
