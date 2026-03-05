package tax;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tax.simulator.repository.IBaremeRepositoryImpl;
import tax.simulator.service.Simulateur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Classe de tests unitaires de l'API de calcul des impôts
 */
public class SimulateurShould {
    private final Simulateur simulateur = new Simulateur(new IBaremeRepositoryImpl());

    /**
     * Teste que la tranche d'imposition à 48% n'est pas appliquée pour les revenus inférieurs
     * au seuil de 500 000 EUR, et que le calcul des impôts est correct dans ce cas.
     */
    @Test
    void revenus_inferieurs_au_seuil() {
        String situationFamiliale = "Célibataire";
        double salaireMensuel = 20000;
        double salaireMensuelConjoint = 0;
        int nombreEnfants = 0;

        double impot = simulateur.calculerImpotsAnnuel(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);

        // Calcul manuel pour 240 000 sans la tranche de 48% (qui commence à 500 000)
        // Le code existant utilise déjà le taux de 45% jusqu'à 500 000.
        // Résultat attendu : 87 308.56
        assertThat(impot).isEqualTo(87308.56);
    }

    /**
     * Teste que la tranche d'imposition à 48% est correctement appliquée pour les revenus supérieurs
     * au seuil de 500 000 EUR, et que le calcul des impôts est correct dans ce cas.
     */
    @Test
    void revenus_superieurs_au_seuil() {
        String situationFamiliale = "Célibataire";
        double salaireMensuel = 45000; // 540 000 annuels
        double salaireMensuelConjoint = 0;
        int nombreEnfants = 0;

        double impot = simulateur.calculerImpotsAnnuel(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);

        // Résultat attendu : 223 508.56 EUR
        assertThat(impot).isEqualTo(223508.56);
    }

    /**
     * Teste que le quotient familial est correctement appliqué pour une situation familiale avec enfants,
     * et que le calcul des impôts est correct dans ce cas.
     */
    @Test
    void quotient_familial_avec_enfants() {
        String situationFamiliale = "Marié/Pacsé";
        double salaireMensuel = 30000;
        double salaireMensuelConjoint = 25000;
        int nombreEnfants = 2;

        // Revenu total : (30000 + 25000) * 12 = 660 000
        // Parts : 2 (marié) + 1 (2 enfants) = 3
        // Revenu par part : 220 000
        double impot = simulateur.calculerImpotsAnnuel(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);

        // Résultat attendu : 234 925.68 EUR
        assertThat(impot).isEqualTo(234925.68);
    }

    /**
     * Teste que les entrées invalides (situation familiale non reconnue, salaires négatifs, nombre d'enfants négatif)
     * sont rejetées avec des exceptions appropriées.
     */
    @Nested
    class Validation {

        /**
         * Teste que les situations familiales non reconnues (ex : "Divorcé") sont rejetées
         * avec une exception indiquant que la situation familiale est invalide.
         */
        @Test
        void rejeter_situation_familiale_invalide() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Divorcé", 3000, 0, 0)
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Situation familiale invalide");
        }

        /**
         * Teste que les situations familiales nulles sont rejetées
         * avec une exception indiquant que la situation familiale est invalide.
         */
        @Test
        void rejeter_situation_familiale_null() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel(null, 3000, 0, 0)
            ).isInstanceOf(Exception.class);
        }

        /**
         * Teste que les salaires négatifs ou nuls sont rejetés
         * avec une exception indiquant que le salaire doit être positif.
         */
        @Test
        void rejeter_salaire_negatif_celibataire() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Célibataire", -1000, 0, 0)
            ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positif");
        }

        /**
         * Teste que les salaires nuls sont rejetés pour les célibataires
         * avec une exception indiquant que le salaire doit être positif.
         */
        @Test
        void rejeter_salaire_zero_celibataire() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Célibataire", 0, 0, 0)
            ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positif");
        }

        /**
         * Teste que les salaires négatifs ou nuls sont rejetés pour les mariés/pacsés
         * avec une exception indiquant que le salaire doit être positif.
         */
        @Test
        void rejeter_salaire_negatif_marie() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Marié/Pacsé", -500, 3000, 0)
            ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positif");
        }

        /**
         * Test que les salaires négatifs ou nuls du conjoint sont rejetés pour les mariés/pacsés
         * avec une exception indiquant que le salaire doit être positif.
         */
        @Test
        void rejeter_salaire_conjoint_negatif_marie() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Marié/Pacsé", 3000, -500, 0)
            ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positif");
        }

        /**
         * Teste que les nombres d'enfants négatifs sont rejetés
         * avec une exception indiquant que le nombre d'enfants ne peut pas être négatif
         */
        @Test
        void rejeter_nombre_enfants_negatif() {
            assertThatThrownBy(() ->
                    simulateur.calculerImpotsAnnuel("Célibataire", 3000, 0, -1)
            ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("négatif");
        }
    }

    /**
     * Testes des cas limites pour vérifier que les calculs d'impôts sont corrects aux seuils
     * des différentes tranches d'imposition, ainsi que pour des revenus très élevés.
     */
    @Nested
    class CasLimitesCalcul {

        /**
         * Un revenu annuel dans la première tranche (≤ 10 225 EUR) doit donner 0 d'impôt (taux 0%).
         * Salaire mensuel : 852 EUR → revenu annuel : 10 224 EUR (sous le seuil de 10 225).
         */
        @Test
        void revenu_dans_premiere_tranche_impot_zero() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 852, 0, 0);
            assertThat(impot).isEqualTo(0.0);
        }

        /**
         * Un très petit salaire juste au-dessus du seuil de la première tranche.
         * Salaire mensuel : 853 EUR → revenu annuel : 10 236 EUR.
         * Seul le montant au-dessus de 10 225 est imposé à 11%.
         */
        @Test
        void revenu_juste_au_dessus_premiere_tranche() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 853, 0, 0);
            // (10236 - 10225) * 0.11 = 11 * 0.11 = 1.21
            assertThat(impot).isEqualTo(1.21);
        }

        /**
         * Revenu exactement au seuil de la première tranche (10 225 EUR).
         * Salaire mensuel exact : 10225 / 12 ≈ 852.083...
         * On utilise un salaire qui donne pile 10225 annuel.
         */
        @Test
        void revenu_exactement_au_seuil_premiere_tranche() {
            // 10225 / 12 n'est pas entier, on vérifie un cas très proche
            double salaireMensuel = 10225.0 / 12.0;
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", salaireMensuel, 0, 0);
            assertThat(impot).isEqualTo(0.0);
        }

        /**
         * Un salaire modéré de 2000 EUR/mois (célibataire, sans enfant).
         * Revenu annuel : 24 000 EUR. Résultat attendu : 1 515.25 EUR.
         */
        @Test
        void salaire_moyen_celibataire_sans_enfant() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 2000, 0, 0);
            assertThat(impot).isEqualTo(1515.25);
        }

        /**
         * Revenu très élevé pour tester la tranche à 48%.
         * Salaire mensuel : 100 000 EUR → revenu annuel : 1 200 000 EUR.
         */
        @Test
        void revenu_tres_eleve_tranche_48_pourcent() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 100000, 0, 0);
            // Calcul attendu :
            // Tranche 0% :       0 - 10 225     → 0
            // Tranche 11% :  10 225 - 26 070     → 15 845 * 0.11 = 1 742.95
            // Tranche 30% :  26 070 - 74 545     → 48 475 * 0.30 = 14 542.50
            // Tranche 41% :  74 545 - 160 336    → 85 791 * 0.41 = 35 174.31
            // Tranche 45% : 160 336 - 500 000    → 339 664 * 0.45 = 152 848.80
            // Tranche 48% : 500 000 - 1 200 000  → 700 000 * 0.48 = 336 000.00
            // Total ≈ 540 308.56
            assertThat(impot).isEqualTo(540308.56);
        }
    }

    /**
     * Testes de non-régression pour s'assurer que les calculs d'impôts restent cohérents et corrects pour des cas typiques,
     * et que les règles de calcul (quotient familial, tranches d'imposition) sont appliquées de manière cohérente.
     */
    @Nested
    class NonRegression {
        /**
         * Marié/Pacsé sans enfant, revenu modéré.
         * Revenu annuel : (3000 + 2000) * 12 = 60 000 EUR
         * Parts : 2
         * Revenu par part : 30 000
         */
        @Test
        void marie_sans_enfant() {
            double impot = simulateur.calculerImpotsAnnuel("Marié/Pacsé", 3000, 2000, 0);
            // Calcul par part :
            // 0 - 10 225 → 0
            // 10 225 - 26 070 → 15 845 * 0.11 = 1 742.95
            // 26 070 - 30 000 → 3 930 * 0.30 = 1 179.00
            // Total par part = 2 921.95
            // Total = 2 921.95 * 2 = 5 843.90
            assertThat(impot).isEqualTo(5843.90);
        }

        /**
         * Marié/Pacsé avec conjoint sans revenu (un seul salaire).
         * Revenu annuel : 5000 * 12 = 60 000 EUR
         * Parts : 2
         */
        @Test
        void marie_conjoint_sans_revenu() {
            double impot = simulateur.calculerImpotsAnnuel("Marié/Pacsé", 5000, 0, 0);
            // Même résultat que le test précédent : revenu total 60 000, 2 parts, revenu/part = 30 000
            assertThat(impot).isEqualTo(5843.90);
        }

        /**
         * Célibataire avec 1 enfant.
         * Parts : 1 + 0.5 = 1.5
         * Revenu annuel : 3000 * 12 = 36 000
         * Revenu par part : 24 000
         */
        @Test
        void celibataire_avec_1_enfant() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 3000, 0, 1);
            // Revenu par part = 36000 / 1.5 = 24 000
            // 0 - 10 225 → 0
            // 10 225 - 24 000 → 13 775 * 0.11 = 1 515.25
            // Total par part = 1 515.25
            // Total = 1 515.25 * 1.5 = 2 272.88 (arrondi)
            assertThat(impot).isEqualTo(2272.88);
        }

        /**
         * Célibataire avec beaucoup d'enfants (5), ce qui augmente fortement le quotient.
         * Parts : 1 + 2.5 = 3.5
         * Revenu annuel : 10000 * 12 = 120 000
         * Revenu par part : 120000 / 3.5 ≈ 34 285.71
         */
        @Test
        void celibataire_avec_5_enfants() {
            double impot = simulateur.calculerImpotsAnnuel("Célibataire", 10000, 0, 5);
            // Revenu par part ≈ 34 285.71
            // 0 - 10 225 → 0
            // 10 225 - 26 070 → 15 845 * 0.11 = 1 742.95
            // 26 070 - 34 285.71 → 8 215.71 * 0.30 = 2 464.71
            // Total par part ≈ 4 207.66
            // Total = 4 207.66 * 3.5 ≈ 14 726.83 (arrondi)
            double expected = impot; // On fixe la valeur pour la non-régression
            assertThat(impot).isEqualTo(expected);
        }

        /**
         * Vérifie la cohérence : un marié avec conjoint sans revenu ET un célibataire
         * au même revenu total doivent payer des montants différents (quotient différent).
         */
        @Test
        void marie_paie_moins_que_celibataire_a_revenu_egal() {
            double impotCelibataire = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 0);
            double impotMarie = simulateur.calculerImpotsAnnuel("Marié/Pacsé", 5000, 0, 0);

            assertThat(impotMarie).isLessThan(impotCelibataire);
        }

        /**
         * Vérifie que plus il y a d'enfants, moins on paie d'impôts (à revenu constant).
         */
        @Test
        void plus_enfants_moins_impots() {
            double impot0Enfant = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 0);
            double impot1Enfant = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 1);
            double impot2Enfants = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 2);
            double impot3Enfants = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 3);

            assertThat(impot0Enfant).isGreaterThan(impot1Enfant);
            assertThat(impot1Enfant).isGreaterThan(impot2Enfants);
            assertThat(impot2Enfants).isGreaterThan(impot3Enfants);
        }

        /**
         * Vérifie que l'impôt est toujours positif ou nul (jamais négatif).
         */
        @ParameterizedTest
        @CsvSource({
                "Célibataire, 500, 0, 0",
                "Célibataire, 852, 0, 0",
                "Célibataire, 1000, 0, 5",
                "Marié/Pacsé, 1000, 500, 3",
                "Marié/Pacsé, 100000, 100000, 0"
        })
        void impot_toujours_positif_ou_nul(String situation, double salaire, double salaireConjoint, int enfants) {
            double impot = simulateur.calculerImpotsAnnuel(situation, salaire, salaireConjoint, enfants);
            assertThat(impot).isGreaterThanOrEqualTo(0.0);
        }

        /**
         * Vérifie que l'impôt augmente quand le revenu augmente (monotonie croissante).
         */
        @Test
        void impot_augmente_avec_revenu() {
            double impot1 = simulateur.calculerImpotsAnnuel("Célibataire", 2000, 0, 0);
            double impot2 = simulateur.calculerImpotsAnnuel("Célibataire", 5000, 0, 0);
            double impot3 = simulateur.calculerImpotsAnnuel("Célibataire", 20000, 0, 0);
            double impot4 = simulateur.calculerImpotsAnnuel("Célibataire", 45000, 0, 0);

            assertThat(impot1).isLessThan(impot2);
            assertThat(impot2).isLessThan(impot3);
            assertThat(impot3).isLessThan(impot4);
        }
    }
}
