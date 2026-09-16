# Vérification 0.1.0 — 14 septembre 2026

By FastedCorsi

- Compilation Java 21 / Fabric, Minecraft 1.21.1 et Cobblemon 1.7.2 réussie.
- 17 tests automatisés réussis : historique initial/inconnu, dates et transferts, filtres, file de relâchements, changements de cible, interruption et absence de confirmation.
- Scénario en client de production dans des mondes plats jetables avec Pokémon fictifs : interaction avec un PC physique complet, remplacement de l'écran, synchronisation de 14 Pokémon, filtre de type officiel, sélection sur plusieurs pages, annulation sans relâchement, transfert PC vers équipe puis retour, exactement deux relâchements confirmés par le serveur intégré, conservation des autres Pokémon et fermeture du bon PC.
- Scénario réussi sans autre mod Tropimon, puis avec TeamBuilder 0.59.10 et Catch Preview 0.6.6. La dernière version de l'habillage Cobblemon a été contrôlée visuellement et le scénario complet a été rejoué avec ces deux mods présents.
- Textures référencées exclusivement dans les ressources de Cobblemon. Aucun import ni dépendance d'exécution vers un autre mod Tropimon. Le code et les Pokémon fictifs du scénario restent dans un JAR de test séparé, absent de la livraison.
- Installateur vérifié dans des dossiers jetables : intégrité de la copie et refus d'écraser un fichier étranger portant le nom cible.
- Contrôle automatisé des sources, ressources, scripts, constantes compilées et archives imbriquées. Attribution publique vérifiée et exclusions des configurations, historiques locaux, journaux, sauvegardes et métadonnées Git.

Les tests utilisent le serveur intégré officiel de Cobblemon ; aucune session du serveur Tropimon ni collection réelle n'a été utilisée. Les règles propres à ce serveur restent à valider en situation réelle. Le journal temporel reflète une première arrivée observée localement, pas une date de capture certifiée ni un historique rétroactif.

La vérification porte sur ce module et ses nouveaux livrables. Elle ne constitue pas un audit des anciens dépôts, historiques ou copies déjà diffusées. L'état réel de l'installation locale est écrit séparément dans `build/delivery/local/install-status.json`.
