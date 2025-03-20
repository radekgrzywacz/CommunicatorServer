package com.communicator.config.websockets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {

    /**
     * asd.
     */
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();

    /**
     * asd.
     * @param sessionId
     * @param phoneNumber
     */
    public void put(final String sessionId, final String phoneNumber) {
        sessionMap.put(sessionId, phoneNumber);
    }

    /**
     * asdas.
     * @param sessionId
     * @return asd
     */
    public String get(final String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * asda.
     * @param sessionId
     */
    public void remove(final String sessionId) {
        sessionMap.remove(sessionId);
    }
}
