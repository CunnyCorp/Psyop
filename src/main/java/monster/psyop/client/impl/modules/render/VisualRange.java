package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.StringSetting;
import monster.psyop.client.impl.events.game.OnTick;
import monster.psyop.client.utility.TextUtils;
import monster.psyop.client.utility.gui.NotificationEvent;
import monster.psyop.client.utility.gui.NotificationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VisualRange extends Module {

    private final BoolSetting chatNotify = new BoolSetting.Builder()
        .name("chat-notify")
        .description("Sends a message in chat when a player enters/leaves.")
        .defaultTo(true)
        .addTo(coreGroup);

    private final BoolSetting toastNotify = new BoolSetting.Builder()
        .name("toast-notify")
        .description("Shows a notification when a player enters/leaves.")
        .defaultTo(true)
        .addTo(coreGroup);

    private final StringSetting enterMessage = new StringSetting.Builder()
        .name("enter-message")
        .description("The message to send when a player enters. Use {player} as a placeholder.")
        .defaultTo("{player} has entered your visual range.")
        .addTo(coreGroup);

    private final StringSetting leaveMessage = new StringSetting.Builder()
        .name("leave-message")
        .description("The message to send when a player leaves. Use {player} as a placeholder.")
        .defaultTo("{player} has left your visual range.")
        .addTo(coreGroup);

    private Set<String> playersInRange = new HashSet<>();

    public VisualRange() {
        super(Categories.RENDER, "visual-range", "Notifies you when players enter or leave your visual range.");
    }

    @EventListener
    public void onTick(OnTick.Pre e) {
        if (MC.player == null || MC.level == null) {
            playersInRange.clear();
            return;
        }

        Set<String> currentPlayers = MC.level.players().stream()
            .map(player -> player.getGameProfile().getName())
            .filter(name -> !name.equals(MC.player.getGameProfile().getName()))
            .collect(Collectors.toSet());

        for (String playerName : currentPlayers) {
            if (playersInRange.add(playerName)) {
                onPlayerEnter(playerName);
            }
        }

        Set<String> playersLeft = new HashSet<>(playersInRange);
        playersLeft.removeAll(currentPlayers);

        for (String playerName : playersLeft) {
            onPlayerLeave(playerName);
            playersInRange.remove(playerName);
        }
    }

    private void onPlayerEnter(String playerName) {
        String message = enterMessage.value().get().replace("{player}", playerName);
        if (chatNotify.get()) {
            if (MC.gui != null && MC.gui.getChat() != null) {
                MutableComponent component = Component.empty();
                String[] parts = enterMessage.value().get().split("\\{player\\}");
                
                if (parts.length > 0) {
                    component.append(Component.literal(parts[0]).withStyle(TextUtils.MODULE_INFO_STYLE));
                }

                for (int i = 1; i < parts.length; i++) {
                    component.append(Component.literal(playerName).withStyle(TextUtils.MODULE_NAME_STYLE).withColor(new Color(154, 243, 232, 255).getRGB()));
                    component.append(Component.literal(parts[i]).withStyle(TextUtils.MODULE_INFO_STYLE));
                }
                
                if (!enterMessage.value().get().contains("{player}")) {
                     component.append(Component.literal(enterMessage.value().get()).withStyle(TextUtils.MODULE_INFO_STYLE));
                } else if (enterMessage.value().get().endsWith("{player}")) {
                    component.append(Component.literal(playerName).withStyle(TextUtils.MODULE_NAME_STYLE).withColor(new Color(154, 243, 232, 255).getRGB()));
                }


                MC.gui.getChat().addMessage(component);
            }
        }
        if (toastNotify.get()) {
            NotificationManager.get().addNotification("Visual Range", message, NotificationEvent.Type.INFO, 2000L);
        }
    }

    private void onPlayerLeave(String playerName) {
        String message = leaveMessage.value().get().replace("{player}", playerName);
        if (chatNotify.get()) {
            if (MC.gui != null && MC.gui.getChat() != null) {
                MutableComponent component = Component.empty();
                String[] parts = leaveMessage.value().get().split("\\{player\\}");

                if (parts.length > 0) {
                    component.append(Component.literal(parts[0]).withStyle(TextUtils.MODULE_INFO_STYLE));
                }

                for (int i = 1; i < parts.length; i++) {
                    component.append(Component.literal(playerName).withStyle(TextUtils.MODULE_NAME_STYLE).withColor(new Color(199, 109, 244, 255).getRGB()));
                    component.append(Component.literal(parts[i]).withStyle(TextUtils.MODULE_INFO_STYLE));
                }

                if (!leaveMessage.value().get().contains("{player}")) {
                     component.append(Component.literal(leaveMessage.value().get()).withStyle(TextUtils.MODULE_INFO_STYLE));
                } else if (leaveMessage.value().get().endsWith("{player}")) {
                    component.append(Component.literal(playerName).withStyle(TextUtils.MODULE_NAME_STYLE).withColor(new Color(199, 109, 244, 255).getRGB()));
                }

                MC.gui.getChat().addMessage(component);
            }
        }
        if (toastNotify.get()) {
            NotificationManager.get().addNotification("Visual Range", message, NotificationEvent.Type.INFO, 2000L);
        }
    }

    @Override
    protected void disabled() {
        super.disabled();
        playersInRange.clear();
    }

    @Override
    protected void enabled() {
        super.enabled();
        if (MC.player != null && MC.level != null) {
            playersInRange = MC.level.players().stream()
                .map(player -> player.getGameProfile().getName())
                .filter(name -> !name.equals(MC.player.getGameProfile().getName()))
                .collect(Collectors.toSet());
        }
    }
}
