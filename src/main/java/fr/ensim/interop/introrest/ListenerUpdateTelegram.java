package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.ForecastDay;
import fr.ensim.interop.introrest.model.Joke;
import fr.ensim.interop.introrest.model.WeatherResponse;
import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Update;
import fr.ensim.interop.introrest.service.ConversationService;
import fr.ensim.interop.introrest.service.JokeService;
import fr.ensim.interop.introrest.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ListenerUpdateTelegram implements CommandLineRunner {

	@Value("${telegram.api.url}")
	private String telegramApiUrl;

	@Value("${telegram.bot.id}")
	private String telegramBotId;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WeatherService weatherService;

	@Autowired
	private JokeService jokeService;

	@Autowired
	private ConversationService conversationService;

	private int offset = 0;

	@Override
	public void run(String... args) throws Exception {
		Logger.getLogger("ListenerUpdateTelegram").log(Level.INFO, "Démarrage du listener d'updates Telegram...");

		// On récupère le dernier update_id pour ignorer l'historique au démarrage
		skipOldUpdates();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollUpdates();
			}
		}, 0, 3000);
	}

	private void skipOldUpdates() {
		try {
			String url = telegramApiUrl + telegramBotId + "/getUpdates";
			ApiResponseUpdateTelegram response = restTemplate.getForObject(url, ApiResponseUpdateTelegram.class);
			if (response != null && response.getOk() && !response.getResult().isEmpty()) {
				List<Update> updates = response.getResult();
				offset = updates.get(updates.size() - 1).getUpdateId() + 1;
				Logger.getLogger("ListenerUpdateTelegram").log(Level.INFO, "Historique ignoré, offset initialisé à " + offset);
			}
		} catch (Exception e) {
			Logger.getLogger("ListenerUpdateTelegram").log(Level.WARNING, "Impossible d'initialiser l'offset : " + e.getMessage());
		}
	}

	private void pollUpdates() {
		try {
			String url = telegramApiUrl + telegramBotId + "/getUpdates?offset=" + offset;
			ApiResponseUpdateTelegram response = restTemplate.getForObject(url, ApiResponseUpdateTelegram.class);

			if (response == null || !response.getOk()) return;

			List<Update> updates = response.getResult();
			for (Update update : updates) {
				offset = update.getUpdateId() + 1;

				if (!update.hasMessage()) continue;

				String text = update.getMessage().getText();
				Long chatId = update.getMessage().getChatId();
				Integer messageId = update.getMessage().getMessageId();

				if (text == null) continue;

				String normalized = normalize(text);

				if (normalized.contains("meteo")) {
					handleMeteo(chatId, text, messageId);
				} else if (normalized.contains("blague")) {
					handleBlague(chatId, messageId);
				} else if (normalized.contains("maya")) {
					sendMessage(chatId, "Maya est une super étudiante en informatique à l'ENSIM (oui oui tqt 🧕🏼)!", messageId);
				} else if (normalized.contains("gabriel")) {
					sendMessage(chatId, "euhhhh salut je suis gay et fier de l'être 🏳️‍🌈", messageId);
				} else {
					sendMessage(chatId, conversationService.chat(text), messageId);
				}
			}
		} catch (Exception e) {
			Logger.getLogger("ListenerUpdateTelegram").log(Level.WARNING, "Erreur lors du polling : " + e.getMessage());
		}
	}

	private void handleMeteo(Long chatId, String text, Integer messageId) {
		String[] words = text.trim().split("\\s+");

		// Détecte si l'utilisateur veut les prévisions
		boolean forecast = normalize(text).contains("prevision");

		// Reconstruit les mots sans "meteo" et sans "prevision(s)"
		StringBuilder cityBuilder = new StringBuilder();
		for (String word : words) {
			String n = normalize(word);
			if (n.contains("meteo") || n.contains("prevision")) continue;
			if (cityBuilder.length() > 0) cityBuilder.append(" ");
			cityBuilder.append(word);
		}
		String city = cityBuilder.toString().trim();

		if (city.isEmpty()) {
			String hint = forecast
					? "Donne moi une ville ! Exemple : meteo previsions Paris"
					: "Donne moi une ville ! Exemple : meteo Paris";
			sendMessage(chatId, hint, messageId);
			return;
		}

		try {
			WeatherResponse weather = weatherService.getWeather(city, forecast);
			if (weather == null) {
				sendMessage(chatId, "Impossible de récupérer la météo pour " + city + ".", messageId);
				return;
			}

			StringBuilder reply = new StringBuilder();
			reply.append("Météo à ").append(weather.getCity()).append(" : ")
					.append(weather.getTemperature()).append("°C, ")
					.append(weather.getCondition());

			if (forecast && weather.getForecast() != null) {
				reply.append("\n\nPrévisions :");
				for (ForecastDay day : weather.getForecast()) {
					reply.append("\n📅 ").append(day.getDate())
							.append(" : ").append(day.getTemperature()).append("°C, ")
							.append(day.getCondition());
				}
			}

			sendMessage(chatId, reply.toString(), messageId);
		} catch (Exception e) {
			sendMessage(chatId, "Ville introuvable : " + city, messageId);
		}
	}

	private void handleBlague(Long chatId, Integer messageId) {
		Joke joke = jokeService.getRandomJoke();
		if (joke == null) {
			sendMessage(chatId, "Impossible de récupérer une blague pour l'instant.", messageId);
			return;
		}
		sendMessage(chatId, joke.getTitle() + "\n" + joke.getText(), messageId);
	}

	private String normalize(String text) {
		String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}", "").toLowerCase();
	}

	private void sendMessage(Long chatId, String text, Integer replyToMessageId) {
		String url = telegramApiUrl + telegramBotId + "/sendMessage";
		Map<String, String> body = new HashMap<>();
		body.put("chat_id", String.valueOf(chatId));
		body.put("text", text);
		body.put("reply_to_message_id", String.valueOf(replyToMessageId));
		restTemplate.postForObject(url, body, String.class);
	}
}
