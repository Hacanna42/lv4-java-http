package org.apache.coyote.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void add(final Session session) {
        sessions.put(session.getId(), session);
    }

    public Session find(final String id) {
        return sessions.get(id);
    }

    public void remove(final String id) {
        sessions.remove(id);
    }

    public void invalidate(final String id) {
        Session session = sessions.get(id);
        if (session != null) {
            session.invalidate();
            sessions.remove(id);
        }
    }
}
