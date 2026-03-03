package tax.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application Spring Boot
 */
@SpringBootApplication
public class TaxSimulatorApplication {
    /**
     * Fonction principale appelée au lancement de l'API
     *
     * @param args arguments donnés au lancement de l'API
     */
    public static void main(String[] args) {
        SpringApplication.run(TaxSimulatorApplication.class, args);
    }
}
