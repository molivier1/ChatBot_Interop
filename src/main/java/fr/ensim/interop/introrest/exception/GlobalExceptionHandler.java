package fr.ensim.interop.introrest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erreur côté API externe (Telegram, OpenWeatherMap...)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientError(HttpClientErrorException e) {
        Map<String, String> error = new HashMap<>();
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            error.put("error", "Ressource introuvable (ville ou chatId inexistant)");
        } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            error.put("error", "Requête invalide : " + e.getResponseBodyAsString());
        } else {
            error.put("error", "Erreur API externe : " + e.getMessage());
        }
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }

    // Erreur inattendue
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erreur interne du serveur : " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
