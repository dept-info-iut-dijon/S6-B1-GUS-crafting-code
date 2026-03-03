package tax.simulator;

import org.springframework.stereotype.Service;

/**
 * Simulateur de calcul des impôts annuels
 */
@Service
public class Simulateur {
    private static final double[] TRANCHES_IMPOSITION = {10225, 26070, 74545, 160336};
    private static final double[] TAUX_IMPOSITION = {0.0, 0.11, 0.30, 0.41, 0.45};
    private static final int NOMBRE_MOIS = 12;

    /**
     * Calcule les impôts annuels d'une personne
     *
     * @param situationFamilialeString situation familiale sous forme d'une chaîne de caractères
     * @param salaireMensuel           salaire mensuel de la personne
     * @param salaireMensuelConjoint   salaire mensuel du conjoint de la personne
     * @param nombreEnfants            nombre d'enfants de la personne
     * @return impôts annuels de la personne
     */
    public double calculerImpotsAnnuel(String situationFamilialeString, double salaireMensuel, double salaireMensuelConjoint, int nombreEnfants) {
        SituationFamiliale situationFamiliale;

        if (situationFamilialeString.equals("Célibataire")) {
            situationFamiliale = SituationFamiliale.CELIBATAIRE;
        } else if (situationFamilialeString.equals("Marié/Pacsé")) {
            situationFamiliale = SituationFamiliale.MARIE_PACSE;
        } else {
            throw new IllegalArgumentException("Situation familiale invalide.");
        }

        if (salaireMensuel <= 0) {
            throw new IllegalArgumentException("Les salaires doivent être positifs.");
        }

        boolean isMariePacse = situationFamiliale == SituationFamiliale.MARIE_PACSE;

        if (isMariePacse && salaireMensuelConjoint < 0) {
            throw new IllegalStateException("Les salaires doivent être positifs.");
        }

        if (nombreEnfants < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif.");
        }

        double revenuAnnuel = isMariePacse ?
                (salaireMensuel + salaireMensuelConjoint) * NOMBRE_MOIS :
                salaireMensuel * NOMBRE_MOIS;

        int baseQuotient = isMariePacse ? 2 : 1;
        double quotientEnfants = 1.0 + (nombreEnfants - 2) * 0.5;

        double partsFiscales = baseQuotient + quotientEnfants;
        double revenuImposableParPart = revenuAnnuel / partsFiscales;

        double impot = 0;
        for (int i = 0; i < TRANCHES_IMPOSITION.length; i++) {
            double montantTranche = Math.min(revenuImposableParPart, TRANCHES_IMPOSITION[i]) - (i > 0 ? TRANCHES_IMPOSITION[i - 1] : 0);
            if (montantTranche > 0) {
                impot += montantTranche * TAUX_IMPOSITION[i];
            }
        }

        if (revenuImposableParPart > TRANCHES_IMPOSITION[TRANCHES_IMPOSITION.length - 1]) {
            impot += (revenuImposableParPart - TRANCHES_IMPOSITION[TRANCHES_IMPOSITION.length - 1]) * TAUX_IMPOSITION[TRANCHES_IMPOSITION.length];
        }

        return Math.round(impot * partsFiscales * 100.0) / 100.0;
    }
}

