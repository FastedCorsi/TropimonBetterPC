# Vérification Tropimon Better PC 0.9.4

By FastedCorsi

## Comportement

Le clic simple sur une carte n’est appliqué qu’au relâchement du bouton de souris : commencer un glisser-déposer ne modifie plus la sélection. Les UUID cochés et leurs surbrillances restent visibles pendant le déplacement et après annulation. Un transfert ne retire de la sélection que les Pokémon qui quittent les résultats du PC.

Équipe → PC : dépôt dans une place libre de la boîte visée, même en survolant une carte occupée ; une zone vide de la grille accepte également le dépôt. Aucune requête d’échange équipe → PC n’est envoyée. Une boîte pleine bloque le dépôt. PC → équipe : déplacement vers une case vide ou échange avec son occupant. Les transferts entre cases de l’équipe et entre cases du PC restent disponibles.

Espèce propose Toutes, Doublons et Légendaires. La catégorie Légendaires repose sur `Pokemon.isLegendary()` de Cobblemon ; elle n’inclut pas automatiquement les fabuleux ni les pseudo-légendaires. Elle se cumule avec les autres filtres et se conserve dans les recherches enregistrées.

Les derniers filtres et le tri sont mémorisés automatiquement, indépendamment des recherches nommées. Filtres, favoris, tags, protections, recherches et historique sont communs à tous les serveurs et mondes pour le compte. Un changement de serveur ou une reconnexion ne réinitialise pas ces préférences. Les sélections de Pokémon et les opérations de stockage en cours ne sont pas restaurées après fermeture.

## Reprise des anciennes données

Lors de la première visite d’un ancien serveur ou monde, son fichier local de préférences est importé dans celui du compte : réunion des favoris, cumul des tags et conservation des recherches avec un suffixe en cas de conflit de nom. Les imports sont mémorisés pour ne pas rétablir plus tard des favoris, tags ou recherches supprimés. Les dernières préférences communes priment lors des imports suivants. Les dates déjà connues, y compris inconnues, ne sont pas remplacées par des dates héritées. Aucun fichier original n’est supprimé.

Les anciens noms de fichiers étant hachés, les données d’un serveur non revisité ne peuvent pas être rattachées automatiquement au compte ; leur reprise attend une connexion à ce serveur. Un ancien fichier de préférences illisible bloque le relâchement plutôt que d’ignorer ses protections. Les limites de fichiers et de collections restent bornées.

## Vérifications

51 tests unitaires réussis. Couverture ajoutée pour mémorisation et remise à zéro des filtres, restauration du tri, migration de plusieurs serveurs vers un compte commun, conservation des recherches en conflit, absence de réimport après retrait d’un tag ou favori, anciens fichiers intacts et illisibles, maintien des dates entre connexions.

Scénarios de production réussis en français 1400 × 900, puis en anglais 1280 × 720 avec TeamBuilder et Catch Preview. Vérification visuelle des surbrillances pendant le glissement. Tests d’annulation sans perte de sélection, de dépôt équipe → carte PC occupée sans échange, d’échange PC → équipe confirmé dans les deux emplacements, de dépôt dans une boîte choisie et de conservation de chaque autre Pokémon par UUID. Le premier scénario de développement utilisait un ancien total fixe après ajout d’un membre d’équipe au PC ; l’assertion a été remplacée par la vérification des UUID de toute la collection PC et équipe.

Menu Légendaires testé avec Lugia, Mew et des espèces ordinaires, cumul avec la recherche, restauration dans un nouvel écran, sauvegarde des derniers filtres et clé de stockage dépendant uniquement du compte. Tests de sélection sur plusieurs pages, tags, favoris, protections et deux relâchements autorisés confirmés. Retour TeamBuilder et isolation des fermetures serveur conservés.

## Livraison

Deux JAR identiques de version 0.9.4 : local avec installateur différé externe et partageable. Attribution, métadonnées, sources, ressources, archives et scripts d’accompagnement contrôlés pour la confidentialité. L’état réel de l’installation figure dans install-status-0.9.4.json ; un armement seul ne vaut pas installation.

Les autres résolutions, packs et serveurs réels ne sont pas couverts par ces scénarios. Les copies déjà diffusées et l’historique Git ne sont pas modifiés.
