# Explication des choix techniques

Nous sommes partis sur le projet Java avec Spring Boot. Spring Boot est préféré par l’équipe pour plusieurs raisons.

Premièrement, il bénéficie d’un écosystème très vaste : intégration facile de composants (tels qu’ils soient), sécurité, et une communauté afin de trouver de nombreux tuto en ligne. Cela accélère le développement de l’API.

Spring Boot offre aussi une portabilité : l’application peut tourner sur n’importe quel serveur ou système d’exploitation supportant la JVM, contrairement à C# qui, historiquement, était plus lié à Windows, même si .NET Core/.NET 5+ a largement corrigé cette limitation. De plus, Spring Boot est très flexible grâce à ses modules, ce qui est un avantage.

Nous sommes tous déjà formés à Java. C# peut être préféré pour des applications fortement intégrées à Windows ou à Azure, mais pour une API indépendante et portable, Spring Boot reste un bon choix.

## Format ADR

### Qui décide ?

L'équipe chargée du projet.

### Quel est le contexte ?

Le projet consiste à développer une nouvelle fonctionnalité et faire un “refactoring” une API. L'équipe possède une
expertise en Java et doit choisir un framework pour le développement tout en assurant sécurité et flexibilité.

### Quelles sont les options identifiées ?

* Utiliser Spring Boot (Java)
* Utiliser C# avec .NET Core / .NET

### Avantages et inconvénients :

#### Spring Boot

* Avantages : large écosystème, intégration facile de composants, forte communauté pour support et tutoriels,
  portabilité sur tout serveur ou système d'exploitation, flexibilité, expertise existante dans l'équipe.
* Inconvénients : dépendance à la JVM et à l'écosystème Java.

#### C# (.NET Core / .NET)

* Avantages : bien intégré à l'écosystème Microsoft, performant pour des applications fortement liées à Windows ou
  Azure.
* Inconvénients : moins d'expérience dans l'équipe, l'écosystème est moins vaste pour des besoins d'API comparé à Spring
  Boot.

### Décision prise :

L'équipe décide d'utiliser Spring Boot pour le développement de l'API. Les critères principaux sont la flexibilité,
l'expertise existante en Java et la richesse de Java / Spring Boot.

Conseils récoltés :

* Privilégier un framework qui maximise la réutilisation des compétences existantes.
* Garantir une bonne architecture.
* Pour des besoins spécifiques à Windows ou Azure, C# peut être envisagé.

### Conséquences de la décision :

L'API sera développée en Java avec Spring Boot, cela facilite le développement. Les compétences des membres seront
utilisées.

## Choix d'architecture

### Architecture en couches (choisie)

L'application est divisée en **couches verticales** : API → Service → Repository. Chaque couche dépend de **l'abstraction de la couche inférieure** (via des interfaces), ce qui assure un **couplage faible**.

Cette architecture présente plusieurs avantages :
- **simple et directe à comprendre**,
- **bien supportée par Spring Boot**,
- **facile à prendre en main par toute l'équipe**.

---

### Architecture hexagonale (Ports & Adapters)

Dans une **architecture hexagonale**, le **domaine métier est placé au centre** et est totalement isolé de l'infrastructure (HTTP, base de données, etc.).

La communication se fait via :
- des **ports** (interfaces),
- des **adapters** (implémentations concrètes).

Le domaine ne possède **aucune dépendance vers l'extérieur**.

Cette architecture est :
- **plus stricte** sur la séparation des responsabilités,
- **plus facilement testable en isolation**.

Cependant, elle est également :
- **plus complexe à mettre en place**,
- nécessite **plus de classes et plus d'indirections**.

---

### Pourquoi l'architecture en couches ?

Nous avons choisi une **architecture en couches** pour plusieurs raisons :

- **Le projet est de taille modeste** : l'overhead d'une architecture hexagonale ne serait pas justifié.
- **L'équipe maîtrise déjà ce modèle avec Spring Boot**, ce qui réduit la courbe d'apprentissage.
- Certains **principes clés de l'architecture hexagonale sont déjà partiellement respectés**, notamment :
  - l'utilisation du **principe d'inversion de dépendance (DIP)** via des interfaces,
  - `CalculImpotService` et `BaremeRepository` sont définis comme **interfaces**,
  - le **Simulateur ne dépend pas directement de Spring**, ce qui limite le couplage avec l'infrastructure.


# Conventions de code


1. **Méthodes :** camelCase, nom = verbe + complément, responsabilité unique.
2. **Interfaces :** nom commençant par I + PascalCase. Documenter uniquement les méthodes propres à l’interface ; les classes filles utilisent @inheritDoc si nécessaire.
3. **Classes** : PascalCase, un fichier par classe. Les sous-classes ne répètent pas la documentation des méthodes de l’interface.
4. **Constantes :** static final en UPPER_CASE.
5. **Membres privés :** _camelCase pour les champs privés.
6. **Fichiers et dossiers :** nom du fichier = nom de la classe. Dossiers en PascalCase, privilégier le pluriel (src/, tests/, config/, data/).
7. **Commentaires :** Javadoc en français pour classes et méthodes publiques. Inline (//) pour expliquer le “pourquoi”, pas le “quoi”. Commenter les méthodes privées seulement si le nom n’est pas explicite.
8. **Tests :** indépendants, nom décrivant le comportement exact testé.
9. **Branches GitHub :** “rôle/nom” en minuscule, en français. Espace avec des “-”.
10. **Commits :** feat: / refactor: / fix: / test : + courte description en français ≤ 50 caractères.
11. **Revues GitHub :** minimum 1 reviewer, blocage du merge sans validation.



## Reflect

### 1. Ce qui a bien fonctionné

#### Appropriation du code existant
Avant d’implémenter la nouvelle tranche, nous avons pris le temps d’analyser l’existant : lecture du code, compréhension des tests et autres vérifications.
En tant qu’étudiants, cette phase nous a permis de mieux comprendre la logique avant de modifier l’API, ce qui a donc limité les erreurs.

#### Refactorisation avant implémentation
Nous avons appliqué le principe :
> **“First make the change easy, then make the easy change.”**

Concrètement, nous avons :
- centralisé la définition des tranches,
- supprimé les valeurs codées en dur,
- clarifié la logique de calcul.

#### Tests
Nous avons utilisé les tests existants comme base.

Nous avons ajouté les tests suivants :
- le seuil exact de **500 000 €**,
- les revenus **supérieurs au seuil**,
- le **cas avec quotient familial**,
- les revenus **inférieurs à 500 000 €** (tests de **non-régression**).

---

### 2. Ce que nous avons appris

Nous avons vu que l’ajout d’une tranche fiscale ne consiste pas uniquement à ajouter un seuil et un taux.
Le **mécanisme du quotient familial** impacte la manière dont le seuil doit être appliqué.

Cela nous a appris à ne pas modifier une **règle métier** sans tout comprendre, et à tester tous les cas. Même une modification simple peut produire des **effets secondaires**. Les tests nous ont permis de sécuriser l’évolution et de travailler avec plus de confiance.

Nous avons également compris qu’un **barème fiscal** doit être structuré comme une **donnée configurable** plutôt que comme une suite de conditions codées en dur. Cette approche facilite les évolutions futures.

---

### 3. Comment nous pourrions être plus efficaces

#### Mettre en place une intégration continue dès le début
Une **intégration continue** permettrait d’automatiser :
- l’exécution des tests,
- la vérification que l’application se lance correctement.

Cela améliorerait notre **réactivité** et notre **organisation**.

De plus, l’utilisation de **tests paramétrés** ou de **jeux de données plus variés** permettrait :
- d’augmenter la couverture de tests,
- de réduire la duplication,
- d’améliorer la lisibilité.

---

### 4. Amélioration

**Amélioration retenue :** externaliser la configuration des tranches fiscales (par exemple via un **fichier JSON** ou une **base de données**).

Cette amélioration permettrait :
- de modifier les **seuils et taux sans changer le code**,
- de simplifier l’ajout de **futures tranches**,
- de rendre les fonctions **plus adaptables aux évolutions**.

#### Architecture

Il s’agit d’une **API REST Spring Boot de simulation d’impôts** organisée en **trois couches** :

- **API**
  - Expose le endpoint `GET /api/tax/calculate`
  - Délègue au service via l’interface `CalculImpotService` (**DIP**).

- **Service (Simulateur)**
  - Applique le **barème progressif par tranches** et le **quotient familial**.
  - Utilise le **pattern Strategy** (`CelibataireStrategy` / `MariePacseStrategy`) pour isoler les règles propres à chaque situation familiale.

- **Repository (`BaremeRepositoryImpl`)**
  - Source unique des **données fiscales** (tranches et taux).
  - Accessible via l’interface `BaremeRepository`.

Les **erreurs de validation** sont interceptées globalement par `GlobalExceptionHandler` et renvoyées sous forme d’`ApiError`, construite avec un **pattern Builder**.

---

### 5. Conclusion

Finalement, ce travail nous a permis de mettre en pratique plusieurs compétences :

- comprendre un **code existant**,
- réaliser une **refactorisation progressive**,
- comprendre l’importance de créer de **bons tests**, notamment de **non-régression**.

Au-delà de l’ajout de la nouvelle fonctionnalité, ce projet nous a permis de progresser dans notre manière d’aborder une **évolution sur une API existante**, en adoptant une démarche **plus structurée et plus professionnelle**.