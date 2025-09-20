package monster.psyop.client.mixin;

import monster.psyop.client.Psyop;
import monster.psyop.client.impl.modules.chat.BetterChat;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 777)
public class ChatComponentMixin {
    /**
     * @author ViaTi
     * @reason Let's not log all of our chat locally, maybe.
     */
    @Overwrite
    private void logChatMessage(GuiMessage guiMessage) {
    }

    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true)
    public void clearMessages(boolean bl, CallbackInfo ci) {
        if (Psyop.MODULES.isActive(BetterChat.class)) {
            BetterChat module = Psyop.MODULES.get(BetterChat.class);

            if (module.noChatLoss.get()) {
                ci.cancel();
            }
        }
    }
}
