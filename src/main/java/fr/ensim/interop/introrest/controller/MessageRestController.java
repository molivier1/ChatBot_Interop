package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.MessageRequest;
import fr.ensim.interop.introrest.model.MessageResponse;
import fr.ensim.interop.introrest.model.telegram.ApiResponseTelegram;
import fr.ensim.interop.introrest.model.telegram.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageRestController {

	@Value("${telegram.api.url}")
	private String telegramApiUrl;

	@Value("${telegram.bot.id}")
	private String telegramBotId;

	@Autowired
	private RestTemplate restTemplate;

	@PostMapping
	public ResponseEntity<MessageResponse> sendMessage(@RequestBody MessageRequest request) {
		String url = telegramApiUrl + telegramBotId + "/sendMessage";

		Map<String, String> body = new HashMap<>();
		body.put("chat_id", request.getChatId());
		body.put("text", request.getText());

		ResponseEntity<ApiResponseTelegram<Message>> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				new HttpEntity<>(body),
				new ParameterizedTypeReference<ApiResponseTelegram<Message>>() {}
		);

		ApiResponseTelegram<Message> responseBody = response.getBody();
		if (responseBody != null && responseBody.getOk()) {
			Message msg = responseBody.getResult();
			MessageResponse result = new MessageResponse(
					String.valueOf(msg.getChatId()),
					msg.getMessageId(),
					msg.getText(),
					"SENT",
					Instant.now().toString()
			);
			return ResponseEntity.ok(result);
		}
		return ResponseEntity.badRequest().build();
	}
}
