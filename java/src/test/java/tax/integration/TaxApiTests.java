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

    @ParameterizedTest
    @CsvSource({
            "Célibataire, 2000, 0, 0, 1515.25",
            "Marié/Pacsé, 3000, 3000, 3, 3983.37"
    })
    void calculateTax_ReturnsOkStatusCode(String situationFamiliale, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants, double expectedResult) {
        var url = formatUrl(situationFamiliale, salaireMensuel, salaireMensuelConjoint, nombreEnfants);
        var response = restTemplate.getForObject(url, Double.class);

        assertThat(response).isCloseTo(expectedResult, within(0.01));
    }

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
