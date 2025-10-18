package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnMouseClick;

public abstract class HUD extends Module {
    public final BoolSetting move = new BoolSetting.Builder()
            .name("move")
            .description("Middle Click somewhere on the screen to move the HUD element.")
            .defaultTo(false)
            .build();
    public final BoolSetting stopMove = new BoolSetting.Builder()
            .name("stop-move")
            .description("Automatically toggle move off.")
            .defaultTo(true)
            .build();
    public final IntSetting xPos = new IntSetting.Builder()
            .name("x")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(coreGroup);
    public final IntSetting yPos = new IntSetting.Builder()
            .name("y")
            .defaultTo(100)
            .range(0, 4000)
            .addTo(coreGroup);


    public HUD(String name, String description) {
        super(Categories.HUD, name, description);
    }

    public HUD(Category category, String name, String description) {
        super(category, name, description);
    }

    @EventListener
    public void onClick(OnMouseClick event) {
        if (event.key == 2 && event.action == 1) {
            if (move.get()) {
                ImVec2 pos = ImGui.getMousePos();

                xPos.value(new ImInt((int) pos.x));
                yPos.value(new ImInt((int) pos.y));
                if (stopMove.get()) {
                    move.value(new ImBoolean(false));
                }
                event.cancel();
            }
        }
    }
}
