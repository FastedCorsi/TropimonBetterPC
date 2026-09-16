# Vérification Tropimon Better PC 0.8.0

By FastedCorsi

## Changements

Comparaison réduite de 632 × 478 à 584 × 434 unités, avec les statistiques calculées par Cobblemon avant les IV et EV. Colonnes rapprochées et écarts signés compacts, sans indication supplémentaire lorsque l'écart est nul. Cœurs, tags, natures effectives et attaques connues conservés.

Icône native des insignes dans la comparaison et sur les cartes. Noms et descriptions officiels des insignes possédés au survol ; insigne actif en premier, insignes potentiels exclus. Implémentation locale autonome et accès aux traductions Cobblemon, sans dépendance à Catch Preview.

Suppression des boutons Nature et Toutes les boîtes. L'ancien filtre de nature n'est pas réactivé par une recherche enregistrée. La destination de boîte reste accessible par une cible temporaire pendant le glisser-déposer. Doublons est remplacé par Espèce : tous les exemplaires d'une espèce, même unique, avec combinaison et sauvegarde des critères.

Recherche de talent avec autocomplétion parmi les talents présents dans le PC, noms localisés ou identifiants anglais, normalisation de la casse et des accents. Validation par clic, Entrée ou Tab ; flèches et molette pour parcourir ; Échap pour annuler. Un résultat vide ne modifie pas le filtre. Les options Tous et HA restent accessibles.

## Vérifications

45 tests unitaires, dont les protections de relâchement et la migration des préférences et recherches. Le filtre Espèce est sauvegardé et son absence dans une ancienne recherche signifie toutes les espèces.

Scénarios en client de production : français 1400 × 900, puis anglais 1280 × 720 avec TeamBuilder et Catch Preview. Monde neuf et Pokémon fictifs uniquement. Contrôle visuel de la collection, de la comparaison, du survol d'insigne et de l'autocomplétion.

Vérifications des talents par saisie, Tab, Entrée, clic, requête inconnue et annulation ; sélection d'une espèce à un seul exemplaire et d'une espèce à plusieurs exemplaires ; combinaison avec le type, rechargement d'une recherche et réinitialisation. Statistiques calculées contrôlées sur un Pikachu de niveau 10 avec IV/EV fixés et menthe : 36 / 14 / 14 / 17 / 17 / 32.

Insigne possédé ajouté à un spécimen de comparaison, accès aux deux textes traduits et cas sans insigne. Maintien des tests de tags, favoris, OT, filtre temporel, protections après sélection et mise en file, annulation, transferts PC/équipe/boîte et exactement deux relâchements autorisés confirmés par le serveur. L'insigne possédé supplémentaire est exclu de la confirmation de relâchement.

## Livraison et limites

Deux JAR identiques de version 0.8.0, local avec installation différée externe et partageable. Contrôle des sources, ressources, constantes compilées, archives et fichiers d'accompagnement. État réel dans install-status-0.8.0.json ; l'armement seul ne prouve pas l'installation.

Les essais ne couvrent pas tous les packs de ressources, résolutions ni versions des mods facultatifs. Le contrôle de confidentialité ne réécrit pas l'historique et n'efface pas les copies déjà diffusées.
