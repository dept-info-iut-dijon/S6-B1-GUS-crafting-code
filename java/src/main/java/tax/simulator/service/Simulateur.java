package tax.simulator.service;

import org.springframework.stereotype.Service;

import tax.simulator.model.BaremeFiscal;
import tax.simulator.service.strategy.CalculFamilialStrategy;
import tax.simulator.service.strategy.CelibataireStrategy;
import tax.simulator.service.strategy.MariePacseStrategy;

/**
 * Implémentation du service de calcul des impôts annuels.
 * Applique le barème progressif par tranche et le mécanisme du quotient familial.
 * <p>
 * Utilise le pattern Strategy pour déléguer les règles variant selon la
 * situation familiale (revenu, quotient, validation) à des stratégies
 * spécialisées ({@link CelibataireStrategy}, {@link MariePacseStrategy}).
 * <p>
 * La couche métier ne connaît pas la source des données fiscales : elle délègue
 * leur récupération au {@link BaremeRepository} (principe DIP).
 */
@Service
public class Simulateur implements CalculImpotService {

    private final BaremeRepository baremeRepository;

    public Simulateur(BaremeRepository baremeRepository) {
        this.baremeRepository = baremeRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculerImpotsAnnuel(String situationFamilialeString, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        CalculFamilialStrategy strategy = getStrategy(situationFamilialeString);

        strategy.validerSalaires(salaireMensuel, salaireMensuelConjoint);
        validerNombreEnfants(nombreEnfants);

        double revenuAnnuel = strategy.calculerRevenuAnnuel(salaireMensuel, salaireMensuelConjoint);
        double partsFiscales = strategy.getBaseQuotient() + nombreEnfants * 0.5;
        double revenuImposableParPart = revenuAnnuel / partsFiscales;

        double impot = calculerImpotParPart(revenuImposableParPart);

        return Math.round(impot * partsFiscales * 100.0) / 100.0;
    }

    // --- Méthodes privées (SRP : chaque méthode a une seule responsabilité) ---

    /**
     * Retourne la stratégie de calcul familial correspondant à la situation.
     */
    private CalculFamilialStrategy getStrategy(String situationFamiliale) {
        return switch (situationFamiliale) {
            case "Célibataire" -> new CelibataireStrategy();
            case "Marié/Pacsé" -> new MariePacseStrategy();
            default -> throw new IllegalArgumentException("Situation familiale invalide.");
        };
    }

    private void validerNombreEnfants(int nombreEnfants) {
        if (nombreEnfants < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif.");
        }
    }

    private double calculerImpotParPart(double revenuImposableParPart) {
        BaremeFiscal bareme = baremeRepository.getBareme();
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

