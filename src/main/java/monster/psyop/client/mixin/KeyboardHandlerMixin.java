package monster.psyop.client.mixin;

import imgui.ImGui;
import monster.psyop.client.Liberty;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.utility.KeyUtils;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.impl.events.game.OnKeyInput;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static monster.psyop.client.Liberty.MC;

@Mixin(value = KeyboardHandler.class, priority = 1)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void onKey(
            long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if ((Gui.IS_LOADED.get() && window == MC.getWindow().getWindow())) {
            ci.cancel();
        }

        if (ImGui.isAnyItemActive()) {
            ci.cancel();
            return;
        }

        if (MC.screen != null) {
            if (MC.screen instanceof ChatScreen || MC.screen.getFocused() instanceof EditBox) {
                return;
            }
        }

        int remapped = KeyUtils.getKeyMapFromGlfwCode(key);

        Liberty.debug("Pressed Key: {} - {} - {}", key, remapped, KeyUtils.getTranslation(remapped));

        if (remapped == -1) {
            return;
        }

        ImGui.getIO().setKeysDown(remapped, action == 1 || action == 2);

        for (Category category : Categories.INDEX) {
            for (Module module : Liberty.MODULES.getCategory(category)) {
                module.keyPressed(remapped, action);
            }
        }

        OnKeyInput event = OnKeyInput.get(remapped, action);

        Liberty.EVENT_HANDLER.call(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    public void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
        if ((Gui.IS_LOADED.get() && window == MC.getWindow().getWindow()) || ImGui.isAnyItemFocused()) {
            ci.cancel();
        }
    }
}
