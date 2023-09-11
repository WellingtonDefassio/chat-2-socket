package wdefassi.io.chat2.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import wdefassi.io.chat2.config.RedisConfig;
import wdefassi.io.chat2.entity.SessionMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class Publisher {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishChatMessage(SessionMessage sessionsMessage) {
        try {
            String payload = objectMapper.writeValueAsString(sessionsMessage);
            redisTemplate
                    .convertAndSend(RedisConfig.CHAT_MESSAGES_CHANNEL, payload)
                    .subscribe();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
