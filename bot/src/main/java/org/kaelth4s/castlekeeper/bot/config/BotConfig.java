package org.kaelth4s.castlekeeper.bot.config;

import org.kaelth4s.castlekeeper.bot.CastleKeeperBot;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateRepository;
import org.kaelth4s.castlekeeper.bot.dialog.InMemoryDialogStateRepository;
import org.kaelth4s.castlekeeper.bot.dialog.RedisDialogStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    @Bean
    public RestClient restClient(@Value("${api.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    @ConditionalOnProperty(name = "redis.host", havingValue = "false", matchIfMissing = true)
    public DialogStateRepository inMemoryRepo() {
        log.info("Using in-memory dialog state repository");
        return new InMemoryDialogStateRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "redis.host")
    public DialogStateRepository redisRepo(StringRedisTemplate redis) {
        log.info("Using Redis dialog state repository");
        return new RedisDialogStateRepository(redis);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(CastleKeeperBot bot) throws TelegramApiException {
        log.info("Registering bot: {}...", bot.getBotUsername());
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        log.info("Bot registered — polling started");
        return api;
    }
}
