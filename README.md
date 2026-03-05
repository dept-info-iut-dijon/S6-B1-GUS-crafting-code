# Simulateur

## Choix technique et amélioration continue
Cette section est disponible dans le fichier [`Projet.md`](https://github.com/dept-info-iut-dijon/S6-B1-GUS-crafting-code/blob/main/Projet.md).

## User Stories
L'entreprise qui a développé le simulateur pour le compte de l'état a utilisé le backlog suivant pour avancer sur ce logiciel.

Backlog disponible [ici](USER_STORIES.md).

Le système est exposé à l'aide d'une `API REST`.

Le Swagger est disponible ici : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
![API](img/api.webp)

## Nouveau Besoin
### **User Story: Ajouter une nouvelle tranche pour les hauts revenus**

**En tant que** gouvernement ou administrateur du système,  
**Je veux** ajouter une nouvelle tranche d'imposition pour les revenus supérieurs à 500 000 EUR,  
**Afin de** mieux taxer les très hauts revenus et d’assurer une meilleure équité fiscale.

---

#### **Critères d'acceptation :**

1. **Définition de la tranche :**
   - Les revenus supérieurs à **500 000 EUR** doivent être taxés à un taux spécifique (**par exemple 48%**).
   - Les revenus inférieurs à 500 000 EUR ne doivent pas être affectés par ce changement.

2. **Impact minimal :**
   - La modification doit être rétrocompatible et ne pas affecter les calculs existants pour les autres tranches.

3. **Exactitude des calculs :**
   - Le système doit correctement appliquer cette nouvelle tranche sans affecter les tranches inférieures.

4. **Facilité de mise à jour :**
   - L'ajout ou la modification des tranches doit être simple et s'appuyer sur des données centralisées.

---

#### **Cas de tests :**

1. **Revenus inférieurs au seuil :**
   - Situation familiale : "Célibataire"  
   - Salaire mensuel : 20 000 EUR (revenu annuel 240 000 EUR)  
   - Résultat attendu : Aucun changement par rapport au calcul précédent (pas de taxe appliquée à 48%).

2. **Revenus supérieurs au seuil :**
   - Situation familiale : "Célibataire"  
   - Salaire mensuel : 45 000 EUR (revenu annuel 540 000 EUR)  
   - Résultat attendu :  
     - Taxe pour les 500 000 premiers EUR basée sur les anciennes tranches.  
     - Taxe pour les 40 000 EUR restants à 48%.

> 223 508.56 EUR

3. **Revenus avec enfants (affectés par le quotient familial) :**
   - Situation familiale : "Marié/Pacsé"  
   - Salaire mensuel conjoint : 25 000 EUR  
   - Salaire mensuel principal : 30 000 EUR  
   - Nombre d'enfants : 2  
   - Résultat attendu :  
     - Application correcte de la nouvelle tranche après division par les parts fiscales.

> 234 925.68 EUR
   
## Conseils
> First make the change easy, then make the easy change

Voici quelques conseils pour réaliser l'ajout de cette fonctionnalité:

- Prenez le temps de découvrir le code
	- Identifier les code smells
	- Lire la documentation (vivante dans le cadre des tests)
- Mettez vous en confiance pour préparer l'accueil de cette fonctionnalité
- Ajouter la fonctionnalité quand votre code sera prêt à la recevoir


Barème disponible [ici](BAREME.md).
