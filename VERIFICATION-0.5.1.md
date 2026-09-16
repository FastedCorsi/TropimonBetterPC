# Vérification Tropimon Better PC 0.5.1

By FastedCorsi

## Périmètre

- Marges supérieures et inférieures équilibrées ; texte des boutons centré sur les deux axes.
- Taille générale de police native ; retrait du réglage de taille et de son ancien champ de préférences.
- Icônes de sexe Cobblemon en proportions natives 6 × 8 et pixels entiers.
- Mode daltonien désactivé par défaut, activable dans la fenêtre, conservé après rechargement des préférences du serveur/monde et compte. Désactivation également conservée. Palette cyan/orange pour IV et nature, contour cyan de sélection, repères numériques et flèches conservés.
- Retrait du rangement automatique, de ses classes, de ses traductions et de ses tests spécifiques. Transferts manuels préservés.
- Suppression directe d’une recherche personnelle enregistrée ; critères actifs, autres recherches, favoris et protections préservés.

## Tests automatisés

39 cas JUnit : historique 7, filtres 5, préférences 7, sélection 4, relâchement 16.

Les tests de relâchement couvrent séparément favori, shiny, marquage, objet et préférences indisponibles. Chaque protection est activée après constitution du lot, puis dans un second scénario pendant l’attente du résultat de la première requête. Aucun envoi n’est autorisé pour la cible nouvellement protégée ; le lot s’arrête, sans répétition ni poursuite après l’arrêt. Les tests existants de changement de session, de cible déplacée, de temporisation, de confirmation serveur et d’annulation restent présents.

Le test de préférences vérifie activation et désactivation persistantes du mode daltonien, compatibilité avec l’ancien champ textScale, suppression d’une seule recherche après rechargement, préservation des favoris et blocage en cas de fichier illisible.

## Vérification en jeu

Scénario isolé dans un monde neuf avec Pokémon fictifs, sous Minecraft 1.21.1, Cobblemon 1.7.2 et Java 21. Aucun Pokémon réel ni serveur externe utilisé.

- Ouverture par interaction avec un vrai bloc PC ; capacités et IV/EV synchronisés.
- Cinq catégories de protection vérifiées avec la session réelle : sélection avant activation, activation après ouverture de la confirmation, activation après mise en file et avant le tick d’envoi. Les spécimens sont conservés dans le PC.
- Suppression d’une recherche par son bouton puis vérification du fichier rechargé et des critères actifs.
- Activation/désactivation du mode daltonien par le bouton, vérification des couleurs et de leur persistance sur disque.
- Doublons, comparaison, favoris, filtres combinés, nature après menthe, attaque seule au survol, sélection multipage, annulation, transfert PC → équipe → boîte.
- Deux relâchements de spécimens non protégés confirmés par le serveur ; tous les autres spécimens conservés.
- Blocage des déplacements dans le PC, fermeture serveur et restitution des commandes.

Captures de la collection, comparaison, doublons, couleurs accessibles, sélection et confirmation générées dans les répertoires de vérification locaux. Contrôle visuel des marges, du centrage et des icônes. Les exécutions FR autonome et EN avec TeamBuilder/Catch Preview utilisent le JAR de production et un auxiliaire de test séparé, absent de la livraison.

## Livraison et limites

Deux copies identiques de 0.5.1, locale et partageable ; contrôles automatisés sur les sources, ressources, constantes compilées, archives et fichiers d’accompagnement. Attribution By FastedCorsi et crédits tiers préservés.

Installation locale différée via le mécanisme existant : attente de Minecraft ou d’une mise à jour précédente, sauvegarde hors des mods chargés, vérification des empreintes après copie. L’état réel de cette version se lit dans install-status-0.5.1.json. Le simple lancement de l’installateur ne prouve pas l’installation.

Les essais couvrent les versions et résolutions testées, pas tous les packs de ressources ni toutes les versions de mods facultatifs. Une protection ajoutée après l’envoi d’une requête déjà partie au serveur ne peut pas annuler rétroactivement cette requête ; les cibles suivantes sont revérifiées avant chaque envoi. Le journal de capture reste une estimation locale. Les contrôles de confidentialité ne suppriment pas les anciens artefacts déjà diffusés et ne réécrivent pas l’historique.
