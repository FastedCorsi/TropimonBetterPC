# Vérification Tropimon Better PC 0.7.0

By FastedCorsi

## Changements

Équipe au-dessus des quinze cartes, filtres et actions de collection dans le panneau inférieur. Comparaison compacte avec deux cartes colorées, cœurs cliquables et ajout/retrait des tags. Suppression des boutons favoris, du bouton Déplacer / échanger et du mode daltonien. Modèle Pokémon attaché au curseur pendant le glisser-déposer. Nom du talent caché en jaune, sans suffixe ; les formes à un seul talent distinct gardent la couleur normale.

Les captures récentes exigent un DO/OT joueur dont l'UUID correspond au compte connecté. Les OT étrangers ou inconnus sont exclus, y compris les anciennes arrivées journalisées. La date reste une estimation locale : ce critère ne distingue pas une capture d'un retour par échange d'un Pokémon ayant le même DO.

## Validation

45 tests unitaires réussis, dont 16 tests de relâchement protégé et les tests de tags, filtres, recherches, historique et préférences. Les anciens réglages d'affichage sont ignorés sans perdre les favoris.

Deux scénarios réussis dans un client de production avec monde neuf et Pokémon fictifs : français en 1400 × 900, puis anglais en 1280 × 720 avec TeamBuilder 0.59.10 et Catch Preview. Le marqueur final de réussite est présent dans les deux journaux locaux. Contrôle visuel des captures de grille, comparaison et glisser-déposer.

Vérifications exécutées : quinze cartes par page, tags individuels et multiples, filtres combinés et recherches persistantes, cœur et tags dans la comparaison, retour Échap, OT propre/étranger/inconnu et période expirée, absence de date sur nouvelle arrivée étrangère, talent caché jaune et forme à talent unique normale. Déplacement PC vers équipe et retour dans une boîte confirmés par le serveur ; annulation du relâchement puis exactement deux relâchements autorisés confirmés. Les protégés restent conservés. Les fermetures serveur, le retour TeamBuilder et le blocage/rétablissement des commandes restent vérifiés.

## Livraison et limites

Deux JAR de la même version, local avec mise à jour différée externe et partageable. Contrôle de confidentialité des sources, ressources, constantes compilées, archives et fichiers d'accompagnement. Attribution By FastedCorsi et crédits tiers conservés. L'état réel de l'installation figure dans install-status-0.7.0.json ; l'armement seul ne prouve pas l'installation.

Les tests utilisent uniquement des spécimens fictifs. Ils ne couvrent pas tous les packs de ressources ni toutes les versions des mods facultatifs. Les contrôles ne réécrivent pas l'historique et n'effacent pas les copies déjà diffusées.
