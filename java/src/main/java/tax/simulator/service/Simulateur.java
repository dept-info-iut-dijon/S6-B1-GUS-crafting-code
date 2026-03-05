package tax.simulator.service;

import org.springframework.stereotype.Service;

import tax.simulator.model.BaremeFiscal;
import tax.simulator.service.strategy.ICalculFamilialStrategy;
import tax.simulator.service.strategy.CelibataireStrategyI;
import tax.simulator.service.strategy.MariePacseStrategyI;

/**
 * Implémentation du service de calcul des impôts annuels.
 * Applique le barème progressif par tranche et le mécanisme du quotient familial.
 * <p>
 * Utilise le pattern Strategy pour déléguer les règles variant selon la
 * situation familiale (revenu, quotient, validation) à des stratégies
 * spécialisées ({@link CelibataireStrategyI}, {@link MariePacseStrategyI}).
 * <p>
 * La couche métier ne connaît pas la source des données fiscales : elle délègue
 * leur récupération au {@link IBaremeRepository} (principe DIP).
 */
@Service
public class Simulateur implements ICalculImpotService {

    private final IBaremeRepository _iBaremeRepository;

    /**
     * Constructeur du service.
     *
     * @param _iBaremeRepository dépôt de barème
     */
    public Simulateur(IBaremeRepository _iBaremeRepository) {
        this._iBaremeRepository = _iBaremeRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculerImpotsAnnuel(String situationFamilialeString, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        ICalculFamilialStrategy strategy = getStrategy(situationFamilialeString);

        strategy.validerSalaires(salaireMensuel, salaireMensuelConjoint);
        validerNombreEnfants(nombreEnfants);

        double revenuAnnuel = strategy.calculerRevenuAnnuel(salaireMensuel, salaireMensuelConjoint);
        double partsFiscales = strategy.getBaseQuotient() + nombreEnfants * 0.5;
        double revenuImposableParPart = revenuAnnuel / partsFiscales;

        double impot = calculerImpotParPart(revenuImposableParPart);

        return Math.round(impot * partsFiscales * 100.0) / 100.0;
    }

    /**
     * Retourne la stratégie de calcul familial correspondant à la situation.
     */
    private ICalculFamilialStrategy getStrategy(String situationFamiliale) {
        return switch (situationFamiliale) {
            case "Célibataire" -> new CelibataireStrategyI();
            case "Marié/Pacsé" -> new MariePacseStrategyI();
            default -> throw new IllegalArgumentException("Situation familiale invalide.");
        };
    }

    private void validerNombreEnfants(int nombreEnfants) {
        if (nombreEnfants < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif.");
        }
    }

    private double calculerImpotParPart(double revenuImposableParPart) {
        BaremeFiscal bareme = _iBaremeRepository.getBareme();
        double[] tranches = bareme.tranches();
        double[] taux = bareme.taux();

        double impot = 0;
        for (int i = 0; i < tranches.length; i++) {
            double montantTranche = Math.min(revenuImposableParPart, tranches[i]) - (i > 0 ? tranches[i - 1] : 0);
            if (montantTranche > 0) {
                impot += montantTranche * taux[i];
            }
        }

        if (revenuImposableParPart > tranches[tranches.length - 1]) {
            impot += (revenuImposableParPart - tranches[tranches.length - 1]) * taux[taux.length - 1];
        }

        return impot;
    }
}

