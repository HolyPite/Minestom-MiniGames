# 📚 Documentation Technique & Features

Ce fichier recense les fonctionnalités implémentées, l'architecture globale et les choix techniques du projet **Minestom-MiniGames**.

## 🏗️ Architecture Globale
Le projet repose sur **Minestom** et utilise une architecture orientée **Managers**.
*   **Point d'entrée** : `me.holypite.Main` (Initialisation du serveur et des managers).
*   **Gestion des Jeux (`GameManager`)** : Gère les parties actives, les transitions d'état (Lobby -> Jeu -> Fin) et le cycle de vie.
*   **Hub (`HubManager`)** : Gère le spawn, la navigation (Boussole) et les resets joueurs.
*   **Cartes (`MapManager`)** : Charge les instances, gère les `config.json` (Spawns, Teams, Météo).

## 🎮 Mini-Jeux Implémentés

### 🐑 SheepWars
Jeu de bataille tactique utilisant des moutons aux propriétés explosives ou magiques.
*   **Core Gameplay** :
    *   **Lancer** : Projectiles custom avec physique. Attribution des Kills au lanceur.
    *   **Probabilités Dynamiques** : Système de poids évolutif (Time-Scaled) pour l'obtention des moutons (les plus forts apparaissent plus tard).
    *   **Protection** : Pas de Friendly Fire (CàC/Arc).
*   **Catalogue de Moutons (`SheepRegistry`)** :
    *   *Explosif / Fragmentation* : Dégâts de zone classiques.
    *   *Black Hole* : Attire les joueurs et blocs.
    *   *Glacé* : Gèle la zone.
    *   *Soigneur* : Zone de régénération pour les alliés.
    *   *Mouton Arc-en-ciel* : Sans gravité, génère un pont de verre coloré temporaire (15s).
    *   *Mouton Géant* : Taille énorme, casse les blocs à l'impact, rebondit et frappe le sol 3 fois.
    *   *Mouton Apocalypse* : Met la nuit, déclenche une pluie de météorites incendiaires.
    *   *Mouton Constructeur* : Reconstruit/Restaure les blocs de la map dans sa zone d'effet.
    *   *GretaSheep* : Croissance animée (NBT) et probabilités de taille.
    *   *CloneSheep* : Spawn aléatoire d'un autre mouton.

### ⚔️ Duel
Combat 1v1 classique.
*   Kit prédéfini.
*   Détection de victoire et arène fermée.

### 🪽 Elytra
Parcours d'obstacles en vol.
*   Checkpoints, Boosts de vitesse, Scoreboard (Temps).

## 🛠️ Systèmes Techniques & Transverses

### Map System & Structures
*   **Configuration** : Fichiers `config.json` par map (Règles, Spawns).
*   **Structures** :
    *   Support Save/Load, Rotation, Mirror.
    *   **Prévisualisation** : Affichage temps réel avant placement.
    *   **Robustesse** : Support GZIP manuel, chargement NBT récursif (Vanilla compatible).

### Joueur & Interaction
*   **Gestion de la Mort** : Mode spectateur, titres à l'écran, sons, gestion du vide (Void kill).
*   **Cosmétiques** : `CosmeticManager` (Particules, Chapeaux).
*   **Synchronisation Visuelle** : Correction des effets de potions (Glowing/Invisibilité).
*   **Water Clutch** : Réinitialisation correcte des dégâts de chute dans l'eau.