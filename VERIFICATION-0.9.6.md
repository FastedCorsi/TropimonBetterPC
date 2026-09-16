# Vérification Tropimon Better PC 0.9.6

By FastedCorsi

Le filtre Légendaires inclut désormais les fabuleux. La synchronisation des espèces et formes de Cobblemon 1.7.2 ne transmet pas leurs catégories au client : les méthodes natives seules ne suffisaient donc pas en multijoueur. Le filtre conserve ces méthodes et utilise, en complément, les identifiants officiels des 94 espèces légendaires et fabuleuses présentes dans cette version. Ce repli est limité à l'espace de noms Cobblemon et fonctionne pour leurs différentes formes.

Le test passe par l'encodage puis le décodage réel de SpeciesRegistrySyncPacket. Il vérifie l'absence des catégories natives après réception et le classement de Lugia, Rayquaza, Marshadow, Keldeo et Mew, ainsi que l'exclusion de Dragonite et Nihilego. Les catégories fournies par les données restent prioritaires ; les espèces personnalisées sans catégories transmises ne bénéficient pas de la liste officielle.

L'insigne et son survol sont affichés uniquement si le Pokémon possède au moins un insigne. Les simples marques géométriques du PC ne créent plus d'icône d'insigne ; leur protection contre le relâchement est conservée. Le rendu a été contrôlé avec des Pokémon sans insigne, avec des marques géométriques et avec un véritable insigne.

52 tests Java réussis, dont 17 consacrés au relâchement et à ses protections. Une série simulée de 120 cibles vérifie l'ordre exact, l'attente de chaque confirmation et l'absence de répétition ou de saut. Les tests de l'installateur différé passent également, y compris le refus d'une ancienne mise à jour après l'installation d'une version plus récente.

Deux parcours du client de production réussis : français en 1400 × 900 sans intégrations, puis anglais en 1280 × 720 avec TeamBuilder et CatchPreview. Ils couvrent l'interaction avec le PC, les filtres, la sélection cumulée sur plusieurs pages, les annulations, les transferts dans les deux sens, les protections et la fermeture. Chaque parcours relâche 13 Pokémon via l'interface de confirmation et attend les réponses du serveur. Les UUID de tous les autres Pokémon du PC et de l'équipe sont conservés. Les essais utilisent un monde de vérification isolé ; aucun Pokémon du compte de jeu habituel n'est relâché.

Deux JAR identiques, local et partageable, portent la version 0.9.6. Les sources, ressources, métadonnées, archives finales et scripts d'accompagnement sont contrôlés pour la confidentialité. L'état de l'installation locale est enregistré dans install-status-0.9.6.json et doit être confronté à l'empreinte du JAR réellement installé. L'installateur attend l'arrêt du jeu concerné et conserve l'ancien JAR hors des mods chargés.

Ces contrôles ne constituent pas un test exhaustif de tous les serveurs ou extensions. Le cas réseau est reproduit avec le sérialiseur officiel, sans accès aux données du serveur de jeu. Les fichiers personnels d'origine, l'historique Git et les copies déjà diffusées ne sont pas modifiés.
