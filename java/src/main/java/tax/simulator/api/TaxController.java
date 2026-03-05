package tax.simulator.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tax.simulator.service.ICalculImpotService;

/**
 * Contrôleur REST pour le calcul des impôts.
 * Dépend de l'interface {@link ICalculImpotService} et non d'une implémentation concrète (DIP).
 */
@RestController
@RequestMapping("/api/tax")
public class TaxController {
    private final ICalculImpotService _iCalculImpotService;

    /**
     * Constructeur du contrôleur.
     *
     * @param _iCalculImpotService service de calcul des impôts
     */
    public TaxController(ICalculImpotService _iCalculImpotService) {
        this._iCalculImpotService = _iCalculImpotService;
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
        return ResponseEntity.ok(
                _iCalculImpotService.calculerImpotsAnnuel(
                        situationFamiliale,
                        salaireMensuel,
                        salaireMensuelConjoint,
                        nombreEnfants
                )
        );
    }
}
