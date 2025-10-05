package monster.psyop.client.impl.modules.chat;

import imgui.type.ImString;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.StringListSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

import java.util.List;

public class BetterChat extends Module {
    public final BoolSetting noChatLoss =
            new BoolSetting.Builder()
                    .name("no-chat-loss")
                    .description("Keeps chat messages in the chat history.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final GroupedSettings filterGroup = addGroup(new GroupedSettings("filter", "Filters for messages"));
    public final BoolSetting selfFilter =
            new BoolSetting.Builder()
                    .name("self-filter")
                    .description("Filter yourself.")
                    .defaultTo(true)
                    .addTo(filterGroup);
    public final StringListSetting selfFilterText =
            new StringListSetting.Builder()
                    .name("self-filter-text")
                    .description("Words to prevent you from saying.")
                    .defaultTo(List.of(new ImString("nigger")))
                    .addTo(filterGroup);
    public final BoolSetting filter =
            new BoolSetting.Builder()
                    .name("others-filter")
                    .description("Filter chat messages from other players.")
                    .defaultTo(true)
                    .addTo(filterGroup);
    public final StringListSetting filterText =
            new StringListSetting.Builder()
                    .name("filter-text")
                    .description("Words to prevent other players from saying.")
                    .defaultTo(List.of(new ImString("nigger")))
                    .addTo(filterGroup);

    public BetterChat() {
        super(Categories.CHAT, "better-chat", "Improves and modifies in-game chat in various ways.");
    }

    @EventListener
    public void onPacketReceived(OnPacket.Received event) {
        if (noChatLoss.get() && event.packet() instanceof ClientboundDeleteChatPacket) {
            event.cancel();
        }

        if (filter.get() && event.packet() instanceof ClientboundSystemChatPacket packet) {
            String msg = packet.content().getString();
            for (ImString str : filterText.value()) {
                if (msg.contains(str.get())) {
                    event.cancel();
                    break;
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

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (selfFilter.get() && event.packet() instanceof ServerboundChatPacket packet) {
            String msg = packet.message();

            for (ImString str : selfFilterText.value()) {
                if (msg.contains(str.get())) {
                    msg = msg.replaceAll(str.get(), "*".repeat(str.get().length()));
                }
            }

            event.packet(new ServerboundChatPacket(msg, packet.timeStamp(), packet.salt(), packet.signature(), packet.lastSeenMessages()));
        }
    }
}
