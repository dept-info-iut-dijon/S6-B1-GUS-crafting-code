package tax.simulator.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tax.simulator.service.CalculImpotService;

/**
 * Contrôleur REST pour le calcul des impôts.
 * Dépend de l'interface {@link CalculImpotService} et non d'une implémentation concrète (DIP).
 */
@RestController
@RequestMapping("/api/tax")
public class TaxController {
    private final CalculImpotService calculImpotService;

    /**
     * Constructeur du contrôleur.
     *
     * @param calculImpotService service de calcul des impôts
     */
    public TaxController(CalculImpotService calculImpotService) {
        this.calculImpotService = calculImpotService;
    }

    /**
     * Calcule les impôts annuels d'une personne
     *
     * @param situationFamiliale     situation familiale
     * @param salaireMensuel         salaire mensuel de la personne
     * @param salaireMensuelConjoint salaire mensuel du conjoint de la personne
     * @param nombreEnfants          nombre d'enfants de la personne
     * @return impôts annuels de la personne
     */
    @GetMapping("/calculate")
    public ResponseEntity<?> calculateTax(
            @RequestParam String situationFamiliale,
            @RequestParam double salaireMensuel,
            @RequestParam double salaireMensuelConjoint,
            @RequestParam int nombreEnfants) {
        try {
            return ResponseEntity.ok(
                    calculImpotService.calculerImpotsAnnuel(
                            situationFamiliale,
                            salaireMensuel,
                            salaireMensuelConjoint,
                            nombreEnfants
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
