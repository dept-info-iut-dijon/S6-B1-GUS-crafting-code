package tax.integration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
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
     * @param situationFamiliale Situation familiale de la personne (Célibataire ou Marié/Pacsé)
     * @param salaireMensuel Salaire mensuel de la personne
     * @param salaireMensuelConjoint Salaire mensuel du conjoint de la personne (0 si célibataire)
     * @param nombreEnfants Nombre d'enfants de la personne
     * @param expectedResult Résultat attendu du calcul des impôts annuels
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
     * Teste que l'API de calcul des impôts retourne un code d'erreur 400 Bad Request pour des entrées invalides, telles que des salaires négatifs ou un nombre d'enfants négatif.
     * @param situationFamiliale Situation familiale de la personne (Célibataire ou Marié/Pacsé)
     * @param salaireMensuel Salaire mensuel de la personne
     * @param salaireMensuelConjoint Salaire mensuel du conjoint de la personne (0 si célibataire)
     * @param nombreEnfants Nombre d'enfants de la personne
     */
    @ParameterizedTest
    @CsvSource({
            "Célibataire, -1000, 0, 0",
            "Célibataire, 3000, 0, -1"
    })
    void calculateTax_ReturnsBadRequest_ForInvalidInputs(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        var url = formatUrl(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/tax/calculate";
    }

    private String formatUrl(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        return String.format(Locale.US, "%s?situationFamiliale=%s&salaireMensuel=%f&salaireMensuelConjoint=%f&nombreEnfants=%d",
                getBaseUrl(), situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
    }
}
