# Vérification Tropimon Better PC 0.6.0

By FastedCorsi

## Fonctionnalité

Quatre tags locaux cumulables : À vendre, À moveset, À EV train et PvPable. Ajout/retrait individuel ou sur la sélection, retrait de tous les tags, menu à états absent/partiel/présent et repères sur les cartes avec libellés au survol. Filtre tous/sans tag/par tag, combiné aux autres critères et sauvegardé dans les recherches.

Persistance par UUID dans les préférences existantes du serveur/monde et compte. Les anciens fichiers et recherches restent compatibles : tag absent signifie aucun filtre de tag. Les valeurs sont bornées à quatre bits et le nombre d’entrées à 100 000. Retirer le dernier tag supprime l’entrée de stockage locale. Les favoris et exclusions sont indépendants, et les tags ne déclenchent aucun paquet serveur.

## Tests

45 tests automatisés : les 39 précédents restent présents, avec six tests de tags : cumul/rechargement/retrait indépendant et séparation des mondes ; modification de groupe sans altérer les autres tags ; filtres tous/sans tag/chacun des quatre tags ; préservation des favoris et exclusions ; sauvegarde et migration des recherches ; fichier invalide conservé et actions bloquées.

Scénario en client de production, monde neuf et spécimens fictifs : ajout des quatre tags par le menu ; état partiel puis ajout à plusieurs Pokémon ; retrait d’un seul tag ; filtre À vendre combiné au type ; enregistrement et rappel d’une recherche avec tag ; retrait de tous les tags avec disparition du résultat et de sa sélection ; filtre sans tag ; rechargement des préférences après ajout et retrait.

Les vérifications précédentes restent actives : filtres, nature après menthe, doublons et comparaison, couleur accessible persistante, suppression de recherches, protections après sélection/confirmation/mise en file, transferts, deux relâchements autorisés confirmés par le serveur et retour des commandes à la fermeture du PC. Captures locales pour contrôle visuel des cartes et menus. Aucun Pokémon réel utilisé.

## Livraison

Deux JAR identiques, local avec mise à jour différée externe et partageable. Contrôle de confidentialité des sources, ressources, constantes compilées, archives et fichiers d’accompagnement ; attribution By FastedCorsi et crédits tiers conservés. État réel dans install-status-0.6.0.json : l’armement ne prouve pas l’installation.

Les tags restent propres à ce client, ce compte et ce serveur/monde ; ils ne sont pas synchronisés entre machines. Les essais ne couvrent pas tous les packs ni toutes les versions des mods facultatifs. Les contrôles ne réécrivent pas l’historique et ne suppriment pas les copies déjà diffusées.
