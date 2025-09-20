package monster.psyop.client.framework.modules.settings.types;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import monster.psyop.client.config.modules.settings.BlockPosSettingConfig;
import monster.psyop.client.framework.modules.settings.Setting;
import monster.psyop.client.framework.modules.settings.wrappers.ImBlockPos;
import net.minecraft.core.BlockPos;

import static monster.psyop.client.Psyop.MC;

public class BlockPosSetting extends Setting<BlockPosSetting, ImBlockPos> {
    public BlockPosSetting(BlockPosSetting.Builder builder) {
        super(builder);
        this.settingConfig = new BlockPosSettingConfig();
    }

    @Override
    public void render() {
        ImGui.beginTable(name + "_bp", 3, ImGuiTableFlags.Borders);

        ImGui.tableNextColumn();

        ImGui.inputInt("X", value().xData);

        ImGui.tableNextColumn();

        ImGui.inputInt("Y", value().yData);

        ImGui.tableNextColumn();

        ImGui.inputInt("Z", value().zData);

        ImGui.tableNextColumn();
        if (ImGui.button("Set Here")) {
            if (MC.player != null) {
                BlockPos blockPos = MC.player.getOnPos();
                value().x(blockPos.getX());
                value().y(blockPos.getY());
                value().z(blockPos.getZ());
            }
        }

        ImGui.tableNextColumn();
        if (ImGui.button("Reset")) {
            value().x(0);
            value().y(64);
            value().z(0);
        }

        ImGui.endTable();
    }

    public static class Builder
            extends SettingBuilder<BlockPosSetting, BlockPosSetting.Builder, ImBlockPos> {
        @Override
        public BlockPosSetting build() {
            check();
            return new BlockPosSetting(this);
        }
    }
}
