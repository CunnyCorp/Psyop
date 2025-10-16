package monster.psyop.client.impl.modules.misc;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Category;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.types.ItemListSetting;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.modules.hud.HUD;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.PacketUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

import java.util.ArrayList;

public class InventoryPanel extends HUD {
    public ItemListSetting stealList = new ItemListSetting.Builder()
            .name("steal-list")
            .defaultTo(new ArrayList<>())
            .addTo(coreGroup);
    public ItemListSetting dumpList = new ItemListSetting.Builder()
            .name("dump-list")
            .defaultTo(new ArrayList<>())
            .addTo(coreGroup);
    public IntSetting movesPerTick = new IntSetting.Builder()
            .name("moves-per-tick")
            .defaultTo(1)
            .range(1, 5)
            .addTo(coreGroup);

    public boolean steal = false;
    public boolean dump = false;

    public InventoryPanel() {
        super(Categories.MISC, "inventory-panel", "Shows a panel in inventories for management.");
    }

    @EventListener
    public void onRender2D(On2DRender event) {
        if (MC.screen instanceof AbstractContainerScreen) {
            ImGui.begin("Inventory Panel", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize);

            if (ImGui.button("Steal")) {
                dump = false;
                steal = true;
            }

            ImGui.sameLine();

            if (ImGui.button("Dump")) {
                dump = true;
                steal = false;
            }

            if (ImGui.button("Silent Close")) {
                MC.player.clientSideCloseContainer();
            }

            ImGui.sameLine();

            if (ImGui.button("Server Close")) {
                PacketUtils.send(new ServerboundContainerClosePacket(MC.player.containerMenu.containerId));
            }

            ImGui.sameLine();

            if (ImGui.button("Close")) {
                MC.player.closeContainer();
            }

            ImGui.end();
        }
    }

    @Override
    public void update() {
        if (steal) {
            for (int i = 0; i < movesPerTick.get(); i++) {
                int slot = InventoryUtils.findMatchingSlot((stack, s) -> stealList.value().contains(stack.getItem()) && s < MC.player.containerMenu.slots.size());

                if (slot != -1) {
                    InventoryUtils.quickMove(slot);
                } else {
                    steal = false;
                    break;
                }
            }
        }

        if (dump) {
            for (int i = 0; i < movesPerTick.get(); i++) {
                int slot = InventoryUtils.findMatchingSlot((stack, s) -> dumpList.value().contains(stack.getItem()) && s > InventoryUtils.getInventoryOffset());

                if (slot != -1) {
                    InventoryUtils.quickMove(slot);
                } else {
                    dump = false;
                    break;
                }
            }
        }
    }
}
