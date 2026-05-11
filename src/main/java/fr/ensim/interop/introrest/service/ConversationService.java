package fr.ensim.interop.introrest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ConversationService {

    private static final String SYSTEM_PROMPT =
            "Tu es un assistant chatbot sympa et décontracté qui s'appelle BotEnsim. " +
            "Tu réponds toujours en français, de manière courte et conviviale. " +
            "Tu sais que tu peux aussi donner la météo d'une ville (commande : meteo <ville>) " +
            "et raconter une blague (commande : blague). " +
            "Si quelqu'un te demande quelque chose que tu ne sais pas faire, oriente-le vers ces commandes.";

    @Value("${grok.api.url}")
    private String groqApiUrl;

    @Value("${grok.api.token}")
    private String groqApiToken;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public String chat(String userMessage) {
        try {
            // Headers avec le token d'authentification
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiToken);

            // Corps de la requête
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", "llama-3.1-8b-instant");

            ArrayNode messages = mapper.createArrayNode();
            messages.add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", SYSTEM_PROMPT));
            messages.add(mapper.createObjectNode()
                    .put("role", "user")
                    .put("content", userMessage));
            requestBody.set("messages", messages);

            HttpEntity<ObjectNode> request = new HttpEntity<>(requestBody, headers);
            JsonNode response = restTemplate.postForObject(groqApiUrl, request, JsonNode.class);

            if (response == null) return "Je ne suis pas disponible pour l'instant.";

            return response
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText("Je ne sais pas quoi répondre.");

        } catch (Exception e) {
            Logger.getLogger("ConversationService")
                    .log(Level.SEVERE, "Erreur Groq : " + e.getMessage(), e);
            return "Oups, j'ai eu un problème pour répondre !";
        }
    }
}
