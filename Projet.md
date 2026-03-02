# Explication des choix techniques

Nous sommes partis sur le projet Java avec Spring Boot. Spring Boot est préféré par l'équipe pour plusieurs raisons.

Premièrement, il bénéficie d'un écosystème très vaste : intégration facile de composants (tels qu'ils soient), sécurité,
et une communauté afin de trouver de nombreux tuto en ligne. Cela accélère le développement de l'API.

Spring Boot offre aussi une portabilité : l'application peut tourner sur n'importe quel serveur ou système
d'exploitation supportant la JVM, contrairement à C# qui, historiquement, était plus lié à Windows, même si .NET
Core/.NET 5+ a largement corrigé cette limitation. De plus, Spring Boot est très flexible grâce à ses modules, ce qui
est un avantage.

Nous sommes tous déjà formés à Java. C# peut être préféré pour des applications fortement intégrées à Windows ou à
Azure, mais pour une API indépendante et portable, Spring Boot reste un bon choix.

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

Privilégier un framework qui maximise la réutilisation des compétences existantes.
Garantir une bonne architecture.

Pour des besoins spécifiques à Windows ou Azure, C# peut être envisagé.

### Conséquences de la décision :

L'API sera développée en Java avec Spring Boot, cela facilite le développement. Les compétences des membres seront
utilisées.

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
