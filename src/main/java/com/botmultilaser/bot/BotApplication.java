package com.botmultilaser.bot;

import com.botmultilaser.bot.service.EchoBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class BotApplication {

	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
		try{
			TelegramBotsApi telegramApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramApi.registerBot(new EchoBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
