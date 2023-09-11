package wdefassi.io.chat2.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import wdefassi.io.chat2.entity.StateChat;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final Map<String, StateChat> sessionFather = new ConcurrentHashMap<>();
    private final Map<String, String> fatherBySessionId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("add session to list: {}", session.getId());
        log.info("session uri: {}", session.getUri());
        Optional<String> optionalSessionId = getFatherBySessionId(session);
        if (optionalSessionId.isEmpty()) {
            List<WebSocketSession> webSocketSessions = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();
            webSocketSessions.add(session);
            sessionFather.put(session.getId(), new StateChat(messages, webSocketSessions));
            fatherBySessionId.put(session.getId(), session.getId());
        } else {
            StateChat stateChat = sessionFather.get(optionalSessionId.get());
            stateChat.getSessions().add(session);
            fatherBySessionId.put(session.getId(), optionalSessionId.get());
            sessionFather.put(optionalSessionId.get(), stateChat);
            if(!stateChat.getMessages().isEmpty()) {
                stateChat.getMessages().forEach(m -> {
                    try {
                        session.sendMessage(new TextMessage(m));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionFatherId = fatherBySessionId.get(session.getId());
        StateChat stateChat = sessionFather.get(sessionFatherId);
        stateChat.getMessages().add(message.getPayload());

        for (WebSocketSession s : stateChat.getSessions()) {
            s.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if(sessionFather.containsKey(session.getId())){
            sessionFather.remove(session.getId());
        } else {
            String fatherId = fatherBySessionId.get(session.getId());
            List<WebSocketSession> webSocketSessions = sessionFather.get(fatherId).getSessions();
            webSocketSessions.remove(session);
        }
        fatherBySessionId.remove(session.getId());
    }

    private Optional<String> getFatherBySessionId(WebSocketSession session) {
        return Optional.ofNullable(session.getUri())
                .map(UriComponentsBuilder::fromUri)
                .map(UriComponentsBuilder::build)
                .map(UriComponents::getQueryParams)
                .map(id -> id.get("session"))
                .flatMap(id -> id.stream().findFirst())
                .map(String::trim)
                .filter(id -> !id.equals("null"));
    }
}
