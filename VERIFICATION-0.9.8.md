# Vérification Tropimon Better PC 0.9.8

By FastedCorsi

Le filtre Taille propose Toutes, XS, S, M, L, XL et Baron en français, ou Alpha en anglais. Il lit les catégories officielles de Cobblemon, calculées à partir des limites synchronisées par le serveur. Comme dans l'interface native de Cobblemon 1.8, l'état Alpha/Baron est prioritaire : un Alpha n'apparaît pas aussi dans sa catégorie intrinsèque M.

L'API de taille n'existe pas dans Cobblemon 1.7.2. Un petit adaptateur local détecte les méthodes publiques `isAlpha` et `getSizeCategory` sans lier le mod à leurs classes au chargement. Sous 1.7.2, le bouton indique que la taille est indisponible et le reste du PC demeure fonctionnel. La dépendance reste `cobblemon >=1.7.2`, sans borne maximale mineure.

Le critère Taille fait partie des derniers filtres du compte et des recherches enregistrées. Les anciens fichiers sans ce champ sont relus avec Toutes, sans perdre favoris, tags, protections ou recherches. Les paquets génériques de mise à jour Pokémon déjà observés provoquent aussi le recalcul du filtre après un changement de taille ou d'état Alpha.

La première intégration du bouton avait déplacé les champs de période sans déplacer leur habillage et leur libellé. Le bloc « Capturés depuis · votre DO » est désormais regroupé au-dessus de `X` et `Heures/Jours`, entre la recherche et le filtre Taille. Les fonds de champ suivent exactement les nouvelles coordonnées.

56 tests Java réussis contre le JAR Cobblemon 1.8.1 actif puis contre la dépendance minimale Cobblemon 1.7.2. Ils couvrent les cinq catégories, la priorité Alpha, une catégorie future inconnue, la détection facultative de l'API et la compatibilité des préférences.

Deux parcours complets du PC de production ont réussi sur Cobblemon 1.8.1 : français en 1400 × 900 et anglais en 1280 × 720. Ils vérifient notamment les catégories XS et XL, le filtre Alpha/Baron exclusif, le menu à sept choix, la recherche enregistrée et la restauration après reconnexion. Les parcours conservent également la sélection sur plusieurs pages, les transferts, les protections et le relâchement confirmé de 13 Pokémon fictifs ; les UUID de tous les autres Pokémon du PC et de l'équipe restent présents. Aucun Pokémon d'un compte de jeu habituel n'est utilisé.

Le script de vérification sélectionne maintenant l'unique JAR Cobblemon actif au lieu de coder le nom de la version 1.7.2. Il retire l'ancienne copie du dossier de test avant le lancement, ce qui permet de vérifier les futures mises à jour mineures sans charger deux versions ensemble.

Les deux JAR de livraison proviennent du même build et sont contrôlés après compilation. Le JAR partageable exclut les outils et données propres à la machine. Les seuils de taille restent sous l'autorité de Cobblemon et du serveur ; le mod ne les recalcule pas et n'ajoute aucun paquet réseau.
