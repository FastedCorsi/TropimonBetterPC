# Vérification 0.4.1 — 15 septembre 2026

By FastedCorsi

Les cartes doubles types utilisent la texture complète de l'emplacement Cobblemon pour les deux moitiés, contour compris. La diagonale ne laisse plus de liseré du premier type à droite ou en bas de la seconde moitié. Les boutons et les menus ont des textes agrandis, ajustés à leur largeur. L'aide permanente en bas à gauche est retirée ; les confirmations et erreurs d'action restent disponibles.

Le bouton TeamBuilder est présent uniquement si ce mod est installé. La liaison facultative ouvre son écran existant et redirige son retour au PC vers la même fenêtre Better PC. Aucune dépendance obligatoire ni modification de TeamBuilder. Le déplacement reste bloqué pendant cette navigation. Une application d'équipe en arrière-plan conserve le verrouillage des actions Better PC jusqu'à sa fin. Une fermeture du PC par le serveur invalide la liaison.

Deux corrections de filtres : les paquets de mise à jour individuelle d'un Pokémon invalident les données de recherche et de tri mises en mémoire ; une durée positive inférieure à une milliseconde ne devient plus une absence de filtre. Un test passe un véritable paquet de renommage au gestionnaire client Cobblemon et vérifie que le Pokémon apparaît dans la recherche sans rouvrir le PC.

Les 27 tests unitaires passent. Scénarios en client de production autonome français à 1400 × 900 et avec TeamBuilder 0.59.10 / Catch Preview en anglais à 1280 × 720 : critères cumulés type, sexe, shiny, objet, nature après menthe et talent caché ; critères incompatibles ; type secondaire ; favoris par UUID ; recherches enregistrées et réinitialisation. Les transferts PC/équipe/boîte, protections et deux relâchements confirmés par le serveur intégré restent vérifiés.

La navigation avec TeamBuilder vérifie l'ouverture par le bouton, le retour par Échap avec conservation du filtre, le retour par son bouton PC et la validité de la session après ces allers-retours. Elle vérifie également les déplacements bloqués, une fermeture serveur sans rapport ignorée et la fermeture de la session concernée sans réouverture automatique.

Contrôle visuel des cartes et boutons ; contrôle de confidentialité des sources, archives finales et accompagnements locaux ; comparaison SHA-256 des deux livrables. L'état réel de l'installation est contrôlé séparément dans `build/delivery/local/install-status.json`.

Les scénarios utilisent un monde jetable et des Pokémon fictifs. Les autres versions de TeamBuilder, ses applications d'équipes complètes en arrière-plan, les autres résolutions et tous les packs tiers ne sont pas couverts par cette vérification. La date de capture reste une estimation locale. Les contrôles ne couvrent pas les historiques ni les copies déjà diffusées.
