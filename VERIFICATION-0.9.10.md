# Vérification 0.9.10

By FastedCorsi

## Changements

- Le menu Espèces / Species propose Barons / Alpha, à partir du statut natif de Cobblemon. Le critère se combine avec les autres filtres et se conserve dans les recherches et après reconnexion.
- Le tag Raid apparaît dans les menus, sur les cartes et dans la comparaison. Ajout, retrait, filtre et retrait de tous les tags incluent ce cinquième tag. Les quatre bits historiques restent inchangés.
- La livraison locale utilise une copie autonome de l'installateur géré : copies importée et chargée, suivi du launcher, sauvegardes et contrôles d'intégrité. La dépendance Cobblemon active est identifiée par ses métadonnées dans le profil, même lorsque son nom de fichier est une empreinte.

## Contrôles

- Compilation et 58 tests JUnit réussis avec Cobblemon 1.8.1 installé et la version minimale 1.7.2 ; aucune borne supérieure mineure dans les métadonnées.
- Sauvegarde du tag Raid, conservation des anciens tags, filtres mémorisés, recherches enregistrées et protections de relâchement contrôlés.
- Client isolé français 1400 × 900 avec les JAR de production et Cobblemon 1.8.1 : scénario complet réussi, filtre Baron et Raid combinés, rechargement des critères, menus de tags et comparaison, sélection cumulative, transferts et relâchements confirmés. Captures de la collection et de la comparaison inspectées ; les cinq tags tiennent sur la carte.
- Treize scénarios isolés de l'installateur géré réussis : précontrôle, mise à jour, réparation des deux copies, import initial, intégrité, identité, versions, doublons, suivi, collisions, restauration et verrouillage.
- Sources, JAR et fichiers d'accompagnement contrôlés par le vérificateur de confidentialité. Aucun outil local ni classe de test dans les JAR livrés ; crédit développeur et licences tiers conservés.

Les contrôles portent sur cette livraison ; ils ne réécrivent ni l'historique ni les copies déjà diffusées. La synchronisation gérée de l'installateur LOCAL ne modifie pas l'auto-updater embarqué, dont l'adaptation au stockage géré reste distincte.
