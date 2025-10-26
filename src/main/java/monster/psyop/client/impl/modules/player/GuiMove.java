package monster.psyop.client.impl.modules.player;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

import static monster.psyop.client.Psyop.MC;

public class GuiMove extends Module {

    public GuiMove() {
        super(Categories.PLAYER, "gui-move", "Allows you to move while in a GUI.");
    }

    @Override
    public void update() {
        if (MC.screen != null) {
            if (MC.screen instanceof ChatScreen) {
                return;
            }
            MC.options.keyUp.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS);
            MC.options.keyDown.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS);
            MC.options.keyLeft.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS);
            MC.options.keyRight.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS);
            MC.options.keyJump.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS);
            MC.options.keySprint.setDown(GLFW.glfwGetKey(MC.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS);
        }
    }
}
