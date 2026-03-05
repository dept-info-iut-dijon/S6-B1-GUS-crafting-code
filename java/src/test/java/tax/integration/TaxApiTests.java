package tax.integration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForClassTypes.within;

/**
 * Classe de tests d'intégration de l'API de calcul des impôts
 */
@SpringBootTest(classes = tax.simulator.TaxSimulatorApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaxApiTests {
    private final RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int port;

    /**
     * Teste que l'API de calcul des impôts retourne le résultat attendu pour différentes situations familiales, salaires et nombre d'enfants.
     *
     * @param situationFamiliale     Situation familiale de la personne (Célibataire ou Marié/Pacsé)
     * @param salaireMensuel         Salaire mensuel de la personne
     * @param salaireMensuelConjoint Salaire mensuel du conjoint de la personne (0 si célibataire)
     * @param nombreEnfants          Nombre d'enfants de la personne
     * @param expectedResult         Résultat attendu du calcul des impôts annuels
     */
    @ParameterizedTest
    @CsvSource({
            "Célibataire, 2000, 0, 0, 1515.25",
            "Marié/Pacsé, 3000, 3000, 3, 3983.37",
            "Célibataire, 20000, 0, 0, 87308.56",
            "Célibataire, 45000, 0, 0, 223508.56",
            "Marié/Pacsé, 30000, 25000, 2, 234925.68"
    })
    void calculateTax_ReturnsOkStatusCode(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants, double expectedResult) {
        var url = formatUrl(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
        var response = restTemplate.getForObject(url, Double.class);

        assertThat(response).isCloseTo(expectedResult, within(0.01));
    }

    /**
     * Teste que l'API retourne un code d'erreur 400 Bad Request pour des entrées invalides.
     *
     * @param situationFamiliale     Situation familiale de la personne (Célibataire ou Marié/Pacsé)
     * @param salaireMensuel         Salaire mensuel de la personne
     * @param salaireMensuelConjoint Salaire mensuel du conjoint de la personne (0 si célibataire)
     * @param nombreEnfants          Nombre d'enfants de la personne
     * @param expectedResult         Résultat attendu du calcul des impôts annuels
     */
    @ParameterizedTest
    @CsvSource({
            "Célibataire, -1000, 0, 0",
            "Célibataire, 3000, 0, -1",
            "Célibataire, 0, 0, 0",
            "Marié/Pacsé, -500, 3000, 0",
            "Marié/Pacsé, 3000, -500, 0",
            "Divorcé, 3000, 0, 0"
    })
    void calculateTax_ReturnsBadRequest_ForInvalidInputs(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        var url = formatUrl(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
        HttpClientErrorException exception = catchThrowableOfType(
                () -> restTemplate.getForEntity(url, String.class),
                HttpClientErrorException.class
        );
        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/tax/calculate";
    }

    private String formatUrl(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        return String.format(Locale.US, "%s?situationFamiliale=%s&salaireMensuel=%f&salaireMensuelConjoint=%f&nombreEnfants=%d",
                getBaseUrl(), situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
    }

    /**
     * Tests d'intégration ciblant les cas limites des tranches d'imposition
     * et les comportements extrêmes de l'API.
     */
    @Nested
    class CasLimitesIntegration {

        /**
         * Revenu dans la première tranche (taux 0%) → impôt = 0.
         * 852 * 12 = 10 224 < 10 225.
         */
        @Test
        void revenu_dans_premiere_tranche_impot_zero() {
            var url = formatUrl("Célibataire", 852, 0, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(0.0, within(0.01));
        }

        /**
         * Revenu juste au-dessus de la première tranche.
         * 853 * 12 = 10 236 → (10 236 - 10 225) * 0.11 = 1.21.
         */
        @Test
        void revenu_juste_au_dessus_premiere_tranche() {
            var url = formatUrl("Célibataire", 853, 0, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(1.21, within(0.01));
        }

        /**
         * Revenu exactement au seuil de la première tranche.
         */
        @Test
        void revenu_exactement_au_seuil_premiere_tranche() {
            var url = formatUrl("Célibataire", 10225.0 / 12.0, 0, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(0.0, within(0.01));
        }

        /**
         * Revenu très élevé (tranche 48%).
         * 100 000 * 12 = 1 200 000 EUR → 540 308.56 EUR.
         */
        @Test
        void revenu_tres_eleve_tranche_48_pourcent() {
            var url = formatUrl("Célibataire", 100000, 0, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(540308.56, within(0.01));
        }
    }

    /**
     * Tests d'intégration visant à vérifier que les propriétés fondamentales du calcul de l'impôt sont respectées,
     * notamment le quotient familial, la monotonie croissante de l'impôt avec le revenu, et la non-négativité de l'impôt.
     */
    @Nested
    class NonRegressionIntegration {

        /**
         * Marié/Pacsé sans enfant.
         * Revenu : (3000 + 2000) * 12 = 60 000, Parts : 2, Revenu/part : 30 000.
         */
        @Test
        void marie_sans_enfant() {
            var url = formatUrl("Marié/Pacsé", 3000, 2000, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(5843.90, within(0.01));
        }

        /**
         * Marié/Pacsé avec conjoint sans revenu → même résultat qu'un couple totalisant le même revenu.
         * 5000 * 12 = 60 000, Parts : 2.
         */
        @Test
        void marie_conjoint_sans_revenu() {
            var url = formatUrl("Marié/Pacsé", 5000, 0, 0);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(5843.90, within(0.01));
        }

        /**
         * Célibataire avec 1 enfant.
         * Revenu : 36 000, Parts : 1.5, Revenu/part : 24 000.
         */
        @Test
        void celibataire_avec_1_enfant() {
            var url = formatUrl("Célibataire", 3000, 0, 1);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isCloseTo(2272.88, within(0.01));
        }

        /**
         * Un marié paie moins qu'un célibataire à revenu total égal (quotient familial).
         */
        @Test
        void marie_paie_moins_que_celibataire_a_revenu_egal() {
            var urlCelibataire = formatUrl("Célibataire", 5000, 0, 0);
            var urlMarie = formatUrl("Marié/Pacsé", 5000, 0, 0);

            var impotCelibataire = restTemplate.getForObject(urlCelibataire, Double.class);
            var impotMarie = restTemplate.getForObject(urlMarie, Double.class);

            assertThat(impotMarie).isLessThan(impotCelibataire);
        }

        /**
         * Plus il y a d'enfants, moins on paie d'impôts (à revenu constant).
         */
        @Test
        void plus_enfants_moins_impots() {
            var impot0 = restTemplate.getForObject(formatUrl("Célibataire", 5000, 0, 0), Double.class);
            var impot1 = restTemplate.getForObject(formatUrl("Célibataire", 5000, 0, 1), Double.class);
            var impot2 = restTemplate.getForObject(formatUrl("Célibataire", 5000, 0, 2), Double.class);
            var impot3 = restTemplate.getForObject(formatUrl("Célibataire", 5000, 0, 3), Double.class);

            assertThat(impot0).isGreaterThan(impot1);
            assertThat(impot1).isGreaterThan(impot2);
            assertThat(impot2).isGreaterThan(impot3);
        }

        /**
         * L'impôt augmente quand le revenu augmente (monotonie croissante).
         */
        @Test
        void impot_augmente_avec_revenu() {
            var impot1 = restTemplate.getForObject(formatUrl("Célibataire", 2000, 0, 0), Double.class);
            var impot2 = restTemplate.getForObject(formatUrl("Célibataire", 5000, 0, 0), Double.class);
            var impot3 = restTemplate.getForObject(formatUrl("Célibataire", 20000, 0, 0), Double.class);
            var impot4 = restTemplate.getForObject(formatUrl("Célibataire", 45000, 0, 0), Double.class);

            assertThat(impot1).isLessThan(impot2);
            assertThat(impot2).isLessThan(impot3);
            assertThat(impot3).isLessThan(impot4);
        }

        /**
         * L'impôt est toujours positif ou nul via l'API.
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
            var url = formatUrl(situation, salaire, salaireConjoint, enfants);
            var response = restTemplate.getForObject(url, Double.class);
            assertThat(response).isGreaterThanOrEqualTo(0.0);
        }
    }
}
