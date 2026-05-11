package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.Joke;
import fr.ensim.interop.introrest.model.WeatherResponse;
import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Update;
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

	private int offset = 0;

	@Override
	public void run(String... args) throws Exception {
		Logger.getLogger("ListenerUpdateTelegram").log(Level.INFO, "Démarrage du listener d'updates Telegram...");

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				pollUpdates();
			}
		}, 0, 3000);
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

				if (text == null) continue;

				String normalized = normalize(text);

				if (normalized.contains("meteo")) {
					handleMeteo(chatId, text);
				} else if (normalized.contains("blague")) {
					handleBlague(chatId);
				} else if (normalized.contains("maya")) {
					sendMessage(chatId, "Maya est une super étudiante en informatique à l'ENSIM (oui oui tqt 🧕🏼)!");
				} else if (normalized.contains("gabriel") || normalized.contains("salut")) {
					sendMessage(chatId, "euhhhh salut je suis gay et fier de l'être 🏳️‍🌈");
				} else {
					sendMessage(chatId, "Désolé, je n'ai pas compris votre message.");
				}
			}
		} catch (Exception e) {
			Logger.getLogger("ListenerUpdateTelegram").log(Level.WARNING, "Erreur lors du polling : " + e.getMessage());
		}
	}

	private void handleMeteo(Long chatId, String text) {
		// Cherche le mot après "meteo" comme ville, ex: "meteo Paris" → "Paris"
		String[] words = text.trim().split("\\s+");
		String city = null;
		for (int i = 0; i < words.length; i++) {
			if (normalize(words[i]).contains("meteo") && i + 1 < words.length) {
				city = String.join(" ", java.util.Arrays.copyOfRange(words, i + 1, words.length));
				break;
			}
		}

		if (city == null) {
			sendMessage(chatId, "donne moi le nom de ta ville ! Exemple : meteo Paris");
			return;
		}

		try {
			WeatherResponse weather = weatherService.getWeather(city, false);
			if (weather == null) {
				sendMessage(chatId, "Impossible de récupérer la météo pour " + city + ".");
				return;
			}
			String reply = "Météo à " + weather.getCity() + " : "
					+ weather.getTemperature() + "°C, "
					+ weather.getCondition();
			sendMessage(chatId, reply);
		} catch (Exception e) {
			sendMessage(chatId, "Ville introuvable : " + city);
		}
	}

	private void handleBlague(Long chatId) {
		Joke joke = jokeService.getRandomJoke();
		String reply = joke.getTitle() + "\n" + joke.getText() + "\nNote : " + joke.getRating() + "/10";
		sendMessage(chatId, reply);
	}

	private String normalize(String text) {
		String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}", "").toLowerCase();
	}

	private void sendMessage(Long chatId, String text) {
		String url = telegramApiUrl + telegramBotId + "/sendMessage";
		Map<String, String> body = new HashMap<>();
		body.put("chat_id", String.valueOf(chatId));
		body.put("text", text);
		restTemplate.postForObject(url, body, String.class);
	}
}
