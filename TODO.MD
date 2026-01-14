# Minestom-MiniGames - Plan d'Amélioration & TODO

Ce fichier liste les améliorations, correctifs et nouvelles fonctionnalités prévues pour le projet.

## 🔴 Priorité Haute (Bugs & Core Gameplay)

### SheepWars - Système de Collision
- [ ] **Fix Self-Collision** : Empêcher le joueur de collisionner avec son propre mouton instantanément au lancer (ajouter une "grace period" ou ignorer le tireur).
- [ ] **Kill Credit** : Passer l'attaquant (`shooter`) aux explosions pour que les kills par moutons soient attribués au bon joueur.
- [ ] **Void Check** : Faire mourir les moutons projetés dans le vide (`Y < voidY`) pour éviter qu'ils ne chargent des chunks à l'infini.

### Système de Dégâts & Teams
- [ ] **Friendly Fire** : Désactiver les dégâts entre alliés pour les flèches et les explosions.
- [ ] **Kill Messages** : Améliorer les messages de mort en indiquant l'arme/mouton utilisé (ex: "X a été pulvérisé par le Mouton Explosif de Y").

## 🟡 Priorité Moyenne (Features & Polissage)

### SheepWars - Feedback Visuel
- [ ] **Wool Recovery** : Ajouter un son (`ENTITY_EXPERIENCE_ORB_PICKUP`) et des particules lors de la récupération d'un mouton tué.
- [ ] **Action Bar** : Afficher le nom du mouton tenu en main dans l'ActionBar avec sa couleur associée.

### Map & Structure System
- [ ] **Structure Preview** : Ajouter un mode "ghost" ou des particules pour visualiser où une structure va être placée avant de valider.
- [ ] **Config.json étendu** : Permettre de définir des flags par map (ex: `canFly`, `dayTime`, `weather`).

### BedWars (Futur)
- [ ] **Shop System** : Créer un système de PNJ marchand avec interface d'achat (GUI).
- [ ] **Generator System** : Implémenter les générateurs de ressources (Fer, Or, Diamant, Emeraude) avec timers configurables.

## 🟢 Priorité Basse (Optimisation & Refactoring)

### Architecture
- [ ] **SheepRegistry Enum** : Remplacer les IDs en String par un Enum pour éviter les typos et faciliter les switchs.
- [ ] **Batch Block Placing** : Utiliser `AbsoluteBlockBatch` pour le placement des grosses structures afin d'optimiser les performances.
- [ ] **Localization** : Passer les messages (fr/en) dans un fichier de configuration pour faciliter la traduction.

---
*Dernière mise à jour : 13 Janvier 2026*
