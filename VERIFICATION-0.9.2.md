# Vérification Tropimon Better PC 0.9.2

By FastedCorsi

Les flèches de nature sur les cartes sont décalées de quatre unités vers la gauche, pour les rapprocher du libellé de leur statistique. Les effets de nature et les valeurs restent inchangés.

Compilation et 45 tests unitaires réussis. Contrôle visuel du client de production en anglais, 1280 × 720 : flèches rapprochées, sans chevauchement avec les libellés. Le premier scénario a échoué sur une lecture immédiate des tags sauvegardés ; le fichier final contenait les tags attendus. La relance complète a réussi, y compris persistance des tags, protections, transferts et relâchements confirmés. La cause de cet échec ponctuel n’est pas établie ; aucune modification de persistance n’est incluse dans cette correction visuelle.

Deux JAR identiques de version 0.9.2, local avec installateur différé externe et partageable. Contrôle de confidentialité des sources, des deux archives et des scripts d’accompagnement réussi. L’état réel de l’installation figure dans install-status-0.9.2.json.

Les autres résolutions et packs n’ont pas été revérifiés pour cette correction. L’historique et les copies déjà diffusées ne sont pas modifiés.
