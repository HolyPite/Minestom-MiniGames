# Minestom-MiniGames - Plan d'Amélioration & TODO

Ce fichier liste les améliorations, correctifs et nouvelles fonctionnalités prévues pour le projet.

## 🔴 Priorité Haute (Bugs & Core Gameplay)

### SheepWars - Améliorations
- [ ] **Sheep Cooldowns** : Ajouter un cooldown par type de mouton pour éviter le spam (ex: 20s pour Black Hole).
- [ ] **Sheep Balancing** : Ajuster les rayons d'explosion et les dégâts des moutons les plus puissants.
- [ ] **Better Registry** : Migrer les IDs de moutons vers un `Enum` pour une meilleure sécurité du code.
- [x] **Dynamic Probabilities** : Système de poids évolutif (Time-Scaled) pour l'obtention des moutons durant la partie.

### Architecture & Stabilité
- [ ] **Memory Leak Audit** : Vérifier que les `Sidebar` et `EventNode` sont bien supprimés lors de la destruction d'une instance.
- [ ] **Async Structure Loading** : Charger les gros fichiers NBT de manière asynchrone pour ne pas figer le serveur lors d'un `/structure load`.

## 🟡 Priorité Moyenne (Features & Polissage)

### Nouveaux Jeux
- [ ] **Splatoon (Projet Suivant)** : Combat de peinture territorial. Utilisation de paquets de blocs rapides pour l'encre, mécanique de nage dans l'encre (vitesse + recharge), et armes custom.
- [ ] **Dropper Modulaire** : Jeu de chute verticale généré procéduralement par empilement de segments (Structures NBT). Système de checkpoints dynamiques et "Instant Retry" à la mort.

### Idées de Mécaniques "Minestom-Only"
- [ ] **Champs de Vecteurs** : Zones de vent ou ventilateurs appliquant une vélocité fluide aux joueurs (trampolines d'air, courants latéraux).
- [ ] **Monde Per-Player** : Objets ou blocs visibles/solides uniquement pour certains joueurs (ex: plateformes d'équipe, illusions d'optique).
- [ ] **Véhicules Custom** : Systèmes de conduite (karts, drones) avec physique d'inertie en interceptant les touches directionnelles.

### Système de Jeu
- [ ] **Kit Selector GUI** : Créer une interface d'inventaire pour choisir son kit au Lobby.
- [ ] **Team Chat** : Implémenter un canal de discussion réservé à l'équipe.
- [ ] **Spectator Compass** : Un item pour les spectateurs permettant de se téléporter rapidement aux joueurs encore en vie.

### Nouveaux types de moutons (Idées)
- [x] **Mouton Arc-en-ciel** : Sans gravité, génère un pont de verre coloré (15s) sur son passage.
- [x] **Mouton Géant** : Taille gigantesque, casse les blocs au sol à l'impact, rebondit et frappe le sol 3 fois.
- [x] **Mouton Apocalypse** : Met la nuit, déclenche une pluie de météorites (incendiaires) dans un large rayon.
- [x] **Mouton Constructeur** : Reconstruit/Restaure les blocs de la map dans sa zone d'effet.

### Map & Structure System
- [ ] **Wireframe Preview** : Pour les structures > 500 blocs, n'afficher que les coins et les contours via des particules.
- [ ] **Map Voting** : Système de vote au Lobby si plusieurs maps sont disponibles pour un mode.

## 🟢 Priorité Basse (Optimisation & Cosmétique)

### Visuals
- [ ] **Custom Win Titles** : Titre personnalisé avec animation de feux d'artifice à la victoire.
- [ ] **Kill Streaks** : Annonces dans le chat lors de séries de kills (Triple Kill, Dominating, etc.).

### Optimisations
- [ ] **TKit Refactoring** : Utiliser des itérateurs de blocs optimisés pour les sphères et rayons.
- [ ] **Localization** : Système de traduction pour les messages et noms d'items.

---

## ✅ Tâches Terminées

### Core & Dégâts
- [x] **Water Clutch Fix** : Réinitialisation des dégâts de chute dans l'eau.
- [x] **Kill Credit** : Attribution des kills au lanceur du mouton.
- [x] **Kill Counter & Scoreboard** : Affichage des statistiques en temps réel.
- [x] **Friendly Fire Protection** : Blocage des dégâts CàC/Arc entre alliés.
- [x] **Improved Death System** : Mode spectre, titres, sons et gestion du vide.

### Moutons & IA
- [x] **Mob AI 2.0** : Hitbox-aware reach, pathfinding optimisé et leap attacks.
- [x] **GretaSheep** : Croissance animée basée sur des structures NBT et probabilités de taille.
- [x] **CloneSheep** : Spawn aléatoire dans la zone d'effet.
- [x] **Activation Delay** : Généralisation du délai d'activation des moutons.
- [x] **Self-Collision Fix** : Période de grâce au lancer.

### Technique
- [x] **Structure System** : Save, Load, Rotation, Mirror et Preview en temps réel.
- [x] **Robust NBT Loading** : Support du GZIP manuel et détection récursive des tags pour compatibilité Vanilla.
- [x] **Config.json étendu** : Paramètres de monde et règles de jeu par map.
- [x] **Potion Visual Sync** : Correction Glowing/Invisibilité.

---
*Dernière mise à jour : 16 Janvier 2026*