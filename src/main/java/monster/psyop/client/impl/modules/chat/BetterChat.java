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

import java.util.List;

public class BetterChat extends Module {
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


    public BetterChat() {
        super(Categories.CHAT, "better-chat", "Improves and modifies in-game chat in various ways.");
    }

    @EventListener
    public void onPacketReceived(OnPacket.Received event) {
        if (event.packet() instanceof ClientboundDeleteChatPacket) {
            event.cancel();
        }
    }
}
