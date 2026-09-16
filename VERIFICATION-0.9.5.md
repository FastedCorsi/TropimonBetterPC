# Vérification Tropimon Better PC 0.9.5

By FastedCorsi

Le correctif de sélection et glisser-déposer de 0.9.4 avait été remplacé par une ancienne mise à jour locale de 0.9.3. Le verrou de l’installateur empêchait les écritures simultanées mais ne garantissait pas l’ordre de ses workers. L’installateur compare désormais la version présente à la version demandée sous ce verrou : un worker plus ancien quitte avec l’état skipped-newer-installed, sans déplacer ni remplacer le JAR récent. Une version dont l’ordre est indéterminable bloque le remplacement.

Tests automatisés de l’installateur dans des instances fictives sous build : installation 0.9.2, mise à jour 0.9.4, tentative tardive 0.9.3 rejetée, fichier récent inchangé, un seul JAR chargé, sauvegarde hors mods conservée, nouvelle installation du même JAR sans effet supplémentaire, version indéterminable préservée. Ces tests font partie du check Gradle sous Windows.

51 tests Java réussis. Tous les fichiers internes du JAR 0.9.5 sont identiques à la 0.9.4 validée en jeu, sauf fabric.mod.json qui porte la nouvelle version. Les corrections de sélection, de transfert, les filtres Légendaires et les données communes à tous les serveurs sont conservés. Aucun nouveau comportement de rendu n’est introduit dans cette livraison ; aucun nouveau lancement du client de vérification n’a été nécessaire.

Deux JAR identiques local et partageable, version 0.9.5. Sources, ressources, métadonnées, archives finales et scripts d’accompagnement contrôlés pour la confidentialité. L’état réel de l’installation est dans install-status-0.9.5.json et doit être confronté au JAR réellement présent ; un statut historique installed-verified ne prouve pas qu’une ancienne version n’a jamais été remplacée ensuite.

Les fichiers d’origine et sauvegardes sont conservés. Les copies déjà diffusées et l’historique Git ne sont pas modifiés.
