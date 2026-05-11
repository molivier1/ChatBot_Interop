package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.model.telegram.ApiResponseUpdateTelegram;
import fr.ensim.interop.introrest.model.telegram.Update;
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
		}, 0, 3000); // démarre immédiatement, se répète toutes les 3 secondes
	}

	private void pollUpdates() {
		try {
			String url = telegramApiUrl + telegramBotId + "/getUpdates?offset=" + offset;
			ApiResponseUpdateTelegram response = restTemplate.getForObject(url, ApiResponseUpdateTelegram.class);

			if (response == null || !response.getOk()) return;

			List<Update> updates = response.getResult();
			for (Update update : updates) {
				// On met à jour l'offset pour ne pas retraiter cet update au prochain appel
				offset = update.getUpdateId() + 1;

				if (!update.hasMessage()) continue;

				String text = update.getMessage().getText();
				Long chatId = update.getMessage().getChatId();

				if (text == null) continue;

				String normalized = normalize(text);
				if (normalized.contains("meteo")) {
					sendMessage(chatId, "Météo non disponible pour l'instant.");
				} else if (normalized.contains("blague")) {
					sendMessage(chatId, "Blague non disponible pour l'instant.");
				}
				else if (normalized.contains("maya")) {
					sendMessage(chatId, "Maya est une super étudiante en informatique à l'ENSIM (oui oui tqt 🧕🏼)!");
				}
				else {
					sendMessage(chatId, "Désolé, je n'ai pas compris votre message.");
				}
			}
		} catch (Exception e) {
			Logger.getLogger("ListenerUpdateTelegram").log(Level.WARNING, "Erreur lors du polling : " + e.getMessage());
		}
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
