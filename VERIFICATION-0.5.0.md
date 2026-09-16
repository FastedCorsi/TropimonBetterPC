# Vérification 0.5.0 — 15 septembre 2026

By FastedCorsi

Cette version ajoute les doublons par espèce/forme, la comparaison de deux Pokémon, un réglage local du texte de 90 % à 130 %, les flèches d’effet de nature après menthe et le rangement assisté. Aucun filtre IV ciblé n’est ajouté. Les dimensions de la fenêtre et des cartes restent identiques ; les ressources natives Cobblemon et le cadre extérieur Tropimon sont conservés.

Le mode Doublons respecte les autres filtres et affiche uniquement les groupes comportant au moins deux exemplaires. La comparaison aligne les IV/EV, leurs écarts, la nature, le talent actif, l’objet et les attaques connues. Les favoris peuvent être modifiés depuis les deux colonnes. Un Pokémon devenu invisible après filtrage ne reste plus ciblé par la barre d’actions.

Le rangement prépare une liste des destinations pour la boîte sélectionnée ou toutes les boîtes accessibles. Les modes utilisent l’espèce/forme, le type principal, ou la priorité favoris puis EV non nuls puis autres. Les emplacements interdits par la configuration native restent fixes. Les filtres de recherche ne réduisent pas ce périmètre, indiqué dans l’aperçu. La validation lance uniquement des paquets officiels de déplacement ou échange PC ; l’équipe ne fait pas partie du plan.

La file attend la confirmation des deux emplacements après chaque échange. Elle vérifie l’identité et les caractéristiques des cibles avant les demandes, la validité de la session et l’état final. Un changement incompatible, un refus d’envoi, une absence de confirmation pendant cinq secondes ou une annulation arrête les demandes restantes, sans réessai automatique. Une demande déjà envoyée peut encore aboutir ; aucun retour arrière automatique n’est tenté.

Les 35 tests unitaires couvrent notamment les plans avec cases vides et boîtes pleines, les emplacements fixes, les trois priorités de rangement, la conservation de chaque UUID, la stabilité d’un rangement déjà effectué, les confirmations reçues séparément, les changements externes, le délai maximal, l’annulation et la persistance de la taille du texte. Les plans sont également vérifiés sur des dispositions aléatoires reproductibles.

Scénarios en client de production autonome français à 1400 × 900 et en anglais à 1280 × 720 avec TeamBuilder 0.59.10 et Catch Preview : filtres combinés, doublons, comparaison et favoris, texte à 130 % puis retour à 100 %, nature après menthe, sélection multiple, transferts PC/équipe/boîte et protections de relâchement. Le rangement réel du stockage fictif est exécuté par le serveur intégré : tous les emplacements finaux correspondent à l’aperçu, les Pokémon sont conservés et l’équipe reste identique. Le retour depuis TeamBuilder et les fermetures serveur restent vérifiés.

Les captures de doublons, comparaison, texte agrandi et aperçu de rangement sont contrôlées visuellement. Les sources, archives finales et accompagnements locaux passent le contrôle de confidentialité. Les deux JAR de livraison sont comparés par SHA-256.

L’installateur attend une éventuelle mise à jour précédente au lieu de forcer son arrêt ou de remplacer un JAR utilisé. Chaque version possède son propre fichier d’état. Un test isolé vérifie le verrou occupé, la conservation du fichier précédent, la copie finale unique, sa sauvegarde et les empreintes. L’état de la livraison réelle est vérifié séparément dans `build/delivery/local/install-status-0.5.0.json`.

Les tests utilisent uniquement des Pokémon fictifs dans un monde jetable. Les autres versions de TeamBuilder, tous les packs de ressources et les inventaires réels du serveur Tropimon ne sont pas couverts. La période de capture reste une estimation locale et les capacités proviennent du serveur. Les contrôles de confidentialité ne modifient pas les historiques ni les copies déjà diffusées.
