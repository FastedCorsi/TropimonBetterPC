# Vérification 0.4.0 — 15 septembre 2026

By FastedCorsi

La fenêtre est réduite de 5 % supplémentaires, sans assombrissement extérieur. La police native utilise un rendu aligné sur les pixels. Les cartes gardent leurs proportions et affichent les abréviations compactes des six statistiques, les IV/EV, la nature effective après menthe, le talent actif, les icônes de sexe Cobblemon et l'objet tenu. Les favoris ont un cœur rouge vide ou plein. La seconde couleur d'un double type reste à l'intérieur de la carte, séparée en diagonale. Le cadre Tropimon reste réservé au contour extérieur.

Le survol du bouton des attaques et son panneau fixe affichent seulement les attaques connues. Les champs de niveau, le filtre d'IV parfaits et la mention de précision au survol sont retirés. Les anciens critères supprimés sont ignorés au chargement des recherches, sans exclure les niveaux supérieurs à 100. Les libellés suivent la langue du jeu, avec français et anglais et repli anglais pour les autres langues. Les 136 clés sont présentes dans chaque catalogue.

Compilation Java 21 / Minecraft 1.21.1 / Cobblemon 1.7.2 et 26 tests unitaires. Vérification en client de production autonome français à 1400 × 900 et avec TeamBuilder / Catch Preview en anglais à 1280 × 720 : ouverture par interaction avec un PC, capacité synchronisée, noms localisés, nature après menthe, objet tenu, saisie accentuée et placement du curseur, recherches enregistrées, sélection sur plusieurs pages, favoris, protections et annulation du relâchement. Les transferts entre PC, équipe et boîte ainsi que deux relâchements sont confirmés par le serveur intégré.

Le scénario maintient les touches d'avance et de saut pendant l'ouverture du PC : aucune progression horizontale et aucune entrée de saut. Il vérifie aussi le retour du déplacement après fermeture. Cette protection reste cliente ; elle ne supprime ni la gravité ni les décisions du serveur.

Les captures de collection et d'attaques sont contrôlées visuellement. Les textes et constantes des sources, ressources et archives compilées sont contrôlés pour la confidentialité ; les deux JAR de livraison sont comparés par SHA-256. Le mécanisme externe de mise à jour conserve une sauvegarde hors des mods chargés et attend uniquement le jeu concerné. L'état effectif de copie est vérifié séparément dans `build/delivery/local/install-status.json`.

Les tests utilisent un monde jetable et des Pokémon fictifs. Ils ne valident pas une collection réelle du serveur Tropimon, tous les packs de ressources ni tous les mods de déplacement. Catch Preview peut toujours afficher sa propre notification de capture à côté de Better PC. Le filtre temporel reste une estimation locale ; la capacité est celle synchronisée par le serveur. Les contrôles de confidentialité ne couvrent pas les copies et historiques déjà diffusés.
