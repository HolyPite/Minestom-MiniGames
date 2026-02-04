# Minestom-MiniGames - TODO List

Ce fichier liste uniquement les tâches actives et les bugs connus.

## 🔴 Priorité Haute (Bugs & Core Gameplay)

### SheepWars
- [ ] **Sheep Cooldowns** : Ajouter un cooldown par type de mouton pour éviter le spam (ex: 20s pour Black Hole).
- [ ] **Sheep Balancing** : Ajuster les rayons d'explosion et les dégâts des moutons les plus puissants (trop OP actuellement).
- [ ] **Better Registry** : Migrer les IDs de moutons (String/Int) vers un `Enum` pour une meilleure sécurité du code et éviter les "magic strings".

### Architecture & Stabilité
- [ ] **Memory Leak Audit** : Vérifier que les `Sidebar`, `EventNode` et `Instances` sont bien nettoyés lors de la destruction d'une partie.
- [ ] **Async Structure Loading** : Charger les gros fichiers NBT de manière asynchrone (CompletableFuture) pour ne pas figer le Main Thread lors d'un `/structure load`.

## 🟡 Priorité Moyenne (Features & Polissage)

### Interface & QoL
- [ ] **Kit Selector GUI** : Créer une interface d'inventaire (Chest GUI) pour choisir son kit au Lobby avant le début de la partie.
- [ ] **Team Chat** : Implémenter un canal de discussion réservé à l'équipe (ex: préfixe `!` ou commande `/t`).
- [ ] **Spectator Compass** : Un item donné aux spectateurs permettant d'ouvrir un menu pour se téléporter aux joueurs en vie.
- [ ] **Map Voting** : Système de vote au Lobby si plusieurs maps sont disponibles pour un mode de jeu.

### Outils Admin / Map Maker
- [ ] **Wireframe Preview** : Optimiser la preview des structures > 500 blocs. N'afficher que les coins et les contours (lignes de particules) au lieu de tous les blocs pour réduire la charge client.

## 🟢 Priorité Basse (Optimisation & Cosmétique)

### Visuals
- [ ] **Custom Win Titles** : Titre plus impactant avec animation de feux d'artifice à la victoire.
- [ ] **Kill Streaks** : Annonces dans le chat lors de séries de kills (Double Kill, Rampage, Godlike, etc.).

### Technique
- [ ] **TKit Refactoring** : Utiliser des itérateurs de blocs optimisés pour les calculs de sphères et rayons (éviter les boucles `for` naïves si possible).
- [ ] **Localization** : Mettre en place un système de clés de traduction (`messages.properties`) pour supporter le Français/Anglais facilement.
