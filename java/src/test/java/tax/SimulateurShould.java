package tax;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
}
