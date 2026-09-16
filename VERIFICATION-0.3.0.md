# Vérification 0.3.0 — 15 septembre 2026

By FastedCorsi

## Présentation livrée

- Dimensions des cartes conservées : 202 × 154 unités, pas de changement du facteur global de l'interface. Quatre colonnes et douze Pokémon par page remplacent la grille à trois colonnes et la fiche latérale.
- Cadre cyan Tropimon existant copié comme ressource autonome ; textures et modèles Cobblemon toujours référencés directement. Aucune dépendance à Casino, TeamBuilder ou Catch Preview.
- Fonds bicolores pour les doubles types ; teinte rouge, contour rouge et coche en sélection multiple. La coloration des valeurs IV reste distincte.
- Nature effective sur la carte, avec mention de menthe. Le panneau compact conserve la nature originale et les autres informations. Talent actif sans numéro « 1/2 », statut caché conservé.
- Attaques accessibles au survol ou au clic sur « Att. », fermeture au clic extérieur ou avec Échap. Les actions équipe, déplacement et favoris restent sous la grille.
- Police Unicode native utilisée pour tous les textes, boutons, menus et champs, avec taille et contraste adaptés. Aucun fichier de police externe. Curseur, saisie et défilement des champs mesurés avec cette police.
- Filtre IV minimum en pourcentage retiré, y compris sa valeur dans les anciennes recherches enregistrées. « Capturés depuis* » porte une mention d'estimation locale et une explication au survol.

## Contrôles réalisés

Compilation Java 21, Minecraft 1.21.1, Cobblemon 1.7.2 ; 26 tests unitaires réussis, sans erreur ni échec.

Scénario client de production autonome en 1400 × 900, puis avec TeamBuilder et Catch Preview en 1280 × 720. Les deux scénarios utilisent un monde jetable et des Pokémon fictifs, et passent :

- Ouverture par interaction avec le PC, capacité synchronisée de trois boîtes, puis compteur de boîtes occupées après transfert.
- Nature Modeste appliquée par menthe à un Pokémon initialement Rigide ; conservation de l'original et filtre de nature effective.
- Talent caché et second talent normal correctement distingués, sans indice numérique affiché.
- Saisie accentuée et positionnement du curseur à la souris ; ancien filtre IV 100 % ignoré après chargement.
- Sélection des douze Pokémon de la page et des quatorze résultats ; ouverture/fermeture des attaques sans modifier cette sélection.
- Exclusion du favori et du shiny dans la confirmation ; annulation sans relâchement.
- Glisser-déposer PC vers équipe puis équipe vers boîte choisie, deux relâchements confirmés par le serveur et fermeture limitée au PC concerné.

Captures contrôlées pour la collection, la sélection rouge, le panneau des attaques et la confirmation. Contrôle de confidentialité des sources et des nouveaux artefacts, comparaison des deux JAR par SHA-256 et contrôle des fichiers locaux d'accompagnement avant livraison.

## Limites

Tests sur serveur intégré, pas sur une collection réelle du serveur Tropimon. Les capacités dépendent toujours des informations envoyées par le serveur. L'estimation temporelle reste la première arrivée observée localement : un échange peut y figurer et aucune date ancienne de capture n'est inventée. Le changement de libellé ne transforme pas cet historique en preuve de capture.

Les deux tailles de fenêtre indiquées ont été contrôlées ; les autres résolutions et tous les packs de ressources tiers ne sont pas vérifiés. Les contrôles concernent les nouveaux livrables, pas les historiques ou copies déjà diffusées. L'état de la mise à jour locale figure dans `build/delivery/local/install-status.json` et la copie installée est comparée par SHA-256.
