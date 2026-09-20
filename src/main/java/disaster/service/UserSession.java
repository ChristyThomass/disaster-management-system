package disaster.service;

import disaster.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserSession {
    private static User currentUser;
    private static final List<SessionListener> listeners = new ArrayList<>();

    public interface SessionListener {
        void onSessionChanged(User user);
    }

    static {
        // Initialize default session as Responder or Guest
        currentUser = new User("user-001", "john_responder", "john@example.com", "RESPONDER", 9.9312, 76.2673);
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
        notifyListeners();
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized void addListener(SessionListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }

    private static synchronized void notifyListeners() {
        for (SessionListener listener : listeners) {
            try {
                listener.onSessionChanged(currentUser);
            } catch (Exception ignored) {}
        }
    }
}
