package monster.psyop.client.impl.eggs;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.CollectionUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

public class Nice {
    public static final String[] FLAGS = new String[]{"69", "420", "weed", "geeked", "uwu", "nyan", "owo", "^-^"};
    public static final String[] RESPONSES = new String[]{
            "Fuck you lmao",
            "You are smelly",
            "Get touched",
            "I will touch you",
            "Hallo everynyan",
            "Chyat so I was-",
            "hey chyat",
            "and then I went-",
            "meow meow meow",
            "OwO whats this?"
    };

    public Nice() {
        Psyop.EVENT_HANDLER.add(this);
    }

    @EventListener
    public void onPacketSent(OnPacket.Sent event) {
        if (event.packet() instanceof ServerboundChatPacket packet) {
            String msg = packet.message();
            if (msg.isBlank()) return;

            boolean isNice = false;
            for (String flag : FLAGS) {
                if (msg.toLowerCase().contains(flag.toLowerCase())) {
                    isNice = true;
                    break;
                }
            }

            if (!isNice) return;

            NotificationManager.get().addNotification("Vali", CollectionUtils.random(RESPONSES), NotificationEvent.Type.ERROR, 15000L);
        }
    }
}
