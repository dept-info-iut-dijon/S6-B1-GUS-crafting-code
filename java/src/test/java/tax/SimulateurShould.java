package tax;

import org.junit.jupiter.api.Test;
import tax.simulator.Simulateur;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Classe de tests unitaires de l'API de calcul des impôts
 */
public class SimulateurShould {
    private final Simulateur simulateur = new Simulateur();

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
}
