package tax.simulator.api;

import java.time.LocalDateTime;

/**
 * Réponse d'erreur normalisée renvoyée par l'API.
 *
 * @param timestamp  date et heure de l'erreur
 * @param status     code HTTP
 * @param error      libellé du statut HTTP
 * @param message    message d'erreur détaillé
 */
public record ApiError(LocalDateTime timestamp, int status, String error, String message) {
}
