package fr.ensim.interop.introrest.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.ensim.interop.introrest.model.Joke;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JokeService {

    @Value("${blagues.api.url}")
    private String blaguesApiUrl;

    @Value("${blagues.api.token}")
    private String blaguesApiToken;

    @Autowired
    private RestTemplate restTemplate;

    public Joke getRandomJoke() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(blaguesApiToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        JsonNode response = restTemplate.exchange(blaguesApiUrl, HttpMethod.GET, request, JsonNode.class).getBody();

        if (response == null) {
            return null;
        }

        Integer id = response.hasNonNull("id") ? response.get("id").asInt() : null;
        String type = response.hasNonNull("type") ? response.get("type").asText() : "blague";
        String question = response.hasNonNull("joke") ? response.get("joke").asText() : "";
        String answer = response.hasNonNull("answer") ? response.get("answer").asText() : "";
        String text = answer.isEmpty() ? question : question + "\n" + answer;

        return new Joke(id, "Blague " + type, text, null);
    }
}
