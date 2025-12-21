package monster.psyop.client.impl.modules.chat;

import imgui.type.ImString;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.game.OnPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BetterChat extends Module {
    public final BoolSetting noChatLoss = new BoolSetting.Builder()
            .name("no-chat-loss")
            .description("Keeps chat messages in the chat history.")
            .defaultTo(true)
            .addTo(coreGroup);

    public final GroupedSettings filterGroup = addGroup(new GroupedSettings("filter", "Filters for messages"));
    public final BoolSetting selfFilter = new BoolSetting.Builder()
            .name("self-filter")
            .description("Filter yourself.")
            .defaultTo(true)
            .addTo(filterGroup);
    public final StringListSetting selfFilterText = new StringListSetting.Builder()
            .name("self-filter-text")
            .description("Words to prevent you from saying.")
            .defaultTo(new ArrayList<>(List.of(new ImString("nigger"))))
            .addTo(filterGroup);
    public final BoolSetting filter = new BoolSetting.Builder()
            .name("others-filter")
            .description("Filter chat messages from other players.")
            .defaultTo(true)
            .addTo(filterGroup);
    public final StringListSetting filterText = new StringListSetting.Builder()
            .name("filter-text")
            .description("Words to prevent other players from saying.")
            .defaultTo(new ArrayList<>(List.of(new ImString("nigger"))))
            .addTo(filterGroup);

    public final GroupedSettings suffixGroup = addGroup(new GroupedSettings("suffix", "Adds a suffix to your messages"));
    public final BoolSetting suffix = new BoolSetting.Builder()
            .name("suffix")
            .description("Enable a suffix at the end of your messages.")
            .defaultTo(false)
            .addTo(suffixGroup);
    public final StringSetting suffixText = new StringSetting.Builder()
            .name("suffix-text")
            .description("The text to add to the end of your messages.")
            .defaultTo("`psyop")
            .addTo(suffixGroup);

    public final GroupedSettings customizedGroup = addGroup(new GroupedSettings("Customized", "Reconstruct chat messages."));
    public final BoolSetting customPlayer = new BoolSetting.Builder()
            .name("custom-player")
            .description("Modifies player messages.")
            .defaultTo(true)
            .addTo(customizedGroup);
    public BoolSetting timestamps = new BoolSetting.Builder()
            .name("timestamps")
            .description("Adds timestamps to the start of messages.")
            .defaultTo(true)
            .addTo(customizedGroup);
    public ColorSetting timestampsColor = new ColorSetting.Builder()
            .name("timestamps-color")
            .description("The color for timestamps.")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 1.0f})
            .addTo(customizedGroup);
    public ColorSetting textColor = new ColorSetting.Builder()
            .name("text-color")
            .description("The color for chat messages.")
            .defaultTo(new float[]{1.0f, 1.0f, 1.0f, 1.0f})
            .addTo(customizedGroup);
    public ColorSetting defaultNameColor = new ColorSetting.Builder()
            .name("default-name-color")
            .description("The color for default player names.")
            .defaultTo(new float[]{0.906f, 0.639f, 0.906f, 1.0f})
            .addTo(customizedGroup);
    public ColorSetting friendNameColor = new ColorSetting.Builder()
            .name("friend-name-color")
            .description("The color for friend names.")
            .defaultTo(new float[]{0.498f, 0.949f, 0.949f, 1.0f})
            .addTo(customizedGroup);
    public ColorSetting selfNameColor = new ColorSetting.Builder()
            .name("self-name-color")
            .description("The color for your own name.")
            .defaultTo(new float[]{1.0f, 0.231f, 1.0f, 1.0f})
            .addTo(customizedGroup);
    public ProvidedStringSetting nameMode = new ProvidedStringSetting.Builder()
            .suggestions(List.of(new ImString("Vanilla"), new ImString("Psyop"), new ImString("Brackets")))
            .name("name-mode")
            .defaultTo(new ImString("Psyop"))
            .addTo(customizedGroup);
    public BoolSetting greenText = new BoolSetting.Builder()
            .name("green-text")
            .defaultTo(true)
            .addTo(customizedGroup);
    public ColorSetting greenTextColor = new ColorSetting.Builder()
            .name("green-text-color")
            .defaultTo(new float[]{0.0f, 0.88f, 0.1f, 1.0f})
            .addTo(customizedGroup);


    public BetterChat() {
        super(Categories.CHAT, "better-chat", "Improves and modifies in-game chat in various ways.");
    }

    @EventListener(inGame = false)
    public void onPacketReceived(OnPacket.Received event) {
        if (noChatLoss.get() && event.packet() instanceof ClientboundDeleteChatPacket) {
            event.cancel();
        }

        if (event.packet() instanceof ClientboundSystemChatPacket packet) {
            String msg = packet.content().getString();

            if (filter.get()) {
                for (ImString str : filterText.value()) {
                    if (msg.contains(str.get())) {
                        event.cancel();
                        break;
                    }
                }
            }

            if (!event.isCancelled()) {
                if (customPlayer.get() && msg.matches("^<[a-zA-Z_0-9]+> .*")) {
                    String name = msg.split(">")[0];
                    MutableComponent component = Component.empty();

                    if (timestamps.get()) {
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                        component.append(Component.literal("[" + now.format(formatter) + "] ").setStyle(Style.EMPTY.withColor(ImColorW.toInt(timestampsColor.get()))));
                    }

                    String n = name.replace("<", "");

                    String formattedName = switch (nameMode.get()) {
                        case "Vanilla" -> "<" + n + ">";
                        case "Psyop" -> n + " ➣";
                        case "Brackets" -> "[" + n + "]";
                        default -> n;
                    };

                    if (n.equals(MC.getGameProfile().getName())) {
                        component.append(Component.literal(formattedName).setStyle(Style.EMPTY.withBold(true).withColor(ImColorW.toInt(selfNameColor.get()))));
                    } else if (FriendManager.roles.containsKey(n)) {
                        component.append(Component.literal(formattedName).setStyle(Style.EMPTY.withBold(true).withColor(ImColorW.toInt(friendNameColor.get()))));
                    } else {
                        component.append(Component.literal(formattedName).setStyle(Style.EMPTY.withBold(false).withColor(ImColorW.toInt(defaultNameColor.get()))));
                    }

                    component.append(" ");

                    String rawMsg = msg.replaceFirst("^<[a-zA-Z_0-9]+> ", "");
                    boolean activateGT = rawMsg.trim().startsWith(">") && greenText.get();

                    for (String part : rawMsg.split(" ")) {
                        String sanitizedPart = part.replaceAll("([#@\\-:;!.]|'s)", "");

                        if (getPlayerNames().contains(sanitizedPart)) {
                            if (sanitizedPart.equals(MC.getGameProfile().getName())) {
                                component.append(Component.literal(part).setStyle(Style.EMPTY.withBold(true).withColor(ImColorW.toInt(selfNameColor.get()))));
                            } else if (FriendManager.roles.containsKey(sanitizedPart)) {
                                component.append(Component.literal(part).setStyle(Style.EMPTY.withBold(true).withColor(ImColorW.toInt(friendNameColor.get()))));
                            } else {
                                component.append(Component.literal(part).setStyle(Style.EMPTY.withBold(false).withColor(ImColorW.toInt(defaultNameColor.get()))));
                            }

                            component.append(" ");

                            continue;
                        }

                        component.append(Component.literal(part + " ").setStyle(Style.EMPTY.withBold(activateGT).withColor(ImColorW.toInt(activateGT ? greenTextColor.get() : textColor.get()))));
                    }

                    MC.gui.getChat().addMessage(component);

                    event.cancel();
                }
            }
        }


        if (filter.get() && event.packet() instanceof ClientboundPlayerChatPacket packet) {
            String msg = packet.body().content();
            for (ImString str : filterText.value()) {
                if (msg.contains(str.get())) {
                    event.cancel();
                    break;
                }
            }

            if (packet.unsignedContent() != null) {
                msg = packet.unsignedContent().getString();
                for (ImString str : filterText.value()) {
                    if (msg.contains(str.get())) {
                        event.cancel();
                        break;
                    }
                }
            }
        }
    }

    @EventListener(inGame = false)
    public void onPacketSend(OnPacket.Send event) {
        if (event.packet() instanceof ServerboundChatPacket packet) {
            String originalMsg = packet.message();
            if (originalMsg.startsWith("/")) return;

            String modifiedMsg = originalMsg;

            if (selfFilter.get()) {
                for (ImString str : selfFilterText.value()) {
                    if (modifiedMsg.contains(str.get())) {
                        modifiedMsg = modifiedMsg.replace(str.get(), "*".repeat(str.get().length()));
                    }
                }
            }

            if (suffix.get()) {
                modifiedMsg = modifiedMsg + " | " + suffixText.value().get();
            }

            event.packet(new ServerboundChatCommandPacket("say " + modifiedMsg));
        }
    }

    public List<String> getPlayerNames() {
        List<String> names = new ArrayList<>();

        if (MC.getConnection() == null) {
            return names;
        }

        for (var player : MC.getConnection().getOnlinePlayers()) {
            names.add(player.getProfile().getName());
        }

        return names;
    }
}
