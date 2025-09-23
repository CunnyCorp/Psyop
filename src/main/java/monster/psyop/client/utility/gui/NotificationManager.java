package monster.psyop.client.utility.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private static final NotificationManager INSTANCE = new NotificationManager();
    private final List<NotificationEvent> notifications = new CopyOnWriteArrayList<>();
    private final List<NotificationEvent> pendingRemoval = new ArrayList<>();
    
    private NotificationManager() {}
    
    public static NotificationManager get() {
        return INSTANCE;
    }
    
    public void addNotification(NotificationEvent notification) {
        notifications.add(notification);
        
        while (notifications.size() > 5) {
            notifications.remove(0);
        }
    }
    
    public void addNotification(String title, String message, NotificationEvent.Type type, long duration) {
        addNotification(new NotificationEvent(title, message, type, duration));
    }
    
    public void addNotification(String message, NotificationEvent.Type type) {
        addNotification("", message, type, 5000); // Default 3 seconds
    }
    
    public List<NotificationEvent> getNotifications() {
        return new ArrayList<>(notifications);
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        pendingRemoval.clear();
        
        for (NotificationEvent notification : notifications) {
            if (notification.isExpired(currentTime)) {
                pendingRemoval.add(notification);
            }
        }
        
        notifications.removeAll(pendingRemoval);
    }
    
    public void clear() {
        notifications.clear();
    }
}