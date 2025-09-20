package monster.psyop.client.framework.gui;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import monster.psyop.client.Psyop;
import monster.psyop.client.impl.events.On2DRender;

import static monster.psyop.client.Psyop.MC;

public abstract class RenderProxy {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    protected void init() {
        ImGui.createContext();

        imGuiGlfw.init(MC.getWindow().getWindow(), true);
        imGuiGl3.init(null);
    }

    public void renderFrame() {
        //GL32.glClearColor(0, 0, 0, 0);
        //GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
        imGuiGlfw.newFrame();
        ImGui.newFrame();
        process();
        Psyop.EVENT_HANDLER.call(On2DRender.get());
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
        //GLFW.glfwSwapBuffers(MC.getWindow().getWindow());
        //GLFW.glfwPollEvents();
    }

    public void process() {
    }
}
