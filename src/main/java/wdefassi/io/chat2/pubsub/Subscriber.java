package wdefassi.io.chat2.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import wdefassi.io.chat2.config.RedisConfig;
import wdefassi.io.chat2.entity.SessionMessage;
import wdefassi.io.chat2.handler.WebSocketHandler;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class Subscriber {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper mapper;

    @PostConstruct
    private void init() {
        redisTemplate
                .listenTo(ChannelTopic.of(RedisConfig.CHAT_MESSAGES_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(this::onChatMessage);
    }

    private void onChatMessage(String payload) {
        try {
            SessionMessage sessionsMessage = mapper.readValue(payload, SessionMessage.class);
            webSocketHandler.sendToSessions(sessionsMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
