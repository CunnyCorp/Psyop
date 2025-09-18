package monster.psyop.client.mixin;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ChatComponent.class, priority = 777)
public class ChatComponentMixin {
    /**
     * @author ViaTi
     * @reason Let's not log all of our chat locally, maybe.
     */
    @Overwrite
    private void logChatMessage(GuiMessage guiMessage) {
    }
}
