package monster.psyop.client.framework.gui.hud.features;

import imgui.ImGui;
import monster.psyop.client.Liberty;
import monster.psyop.client.config.Config;
import monster.psyop.client.framework.gui.Gui;
import monster.psyop.client.framework.gui.hud.HudElement;
import monster.psyop.client.framework.modules.settings.wrappers.ImBlockPos;
import monster.psyop.client.utility.StringUtils;
import monster.psyop.client.utility.WorldUtils;

import java.text.DecimalFormat;

public class PositionHud extends HudElement {
    private final DecimalFormat numberFormat = new DecimalFormat("#.0");
    private final ImBlockPos lastDetectedGateway = new ImBlockPos();

    @Override
    public String name() {
        return "position";
    }

    @Override
    public int defaultWidth() {
        return 274;
    }

    @Override
    public int defaultHeight() {
        return 51;
    }

    @Override
    public int defaultX() {
        return 1641;
    }

    @Override
    public int defaultY() {
        return 364;
    }

    @Override
    public void show() {
        if (!shouldShow()) {
            return;
        }

        if (ImGui.begin(name() + "__hud", state(), defaultWindowFlags())) {
            fillPosAndSize();

            renderCurPosition();

            if (shouldHaveOpposite()) {
                renderOppPosition();
            } else {
                renderLastGateway();
            }

            renderExtraData();
        }

        if (Gui.IS_LOADED.get()) {
            copyPosAndSize();
        }

        ImGui.end();
    }

    private void renderCurPosition() {
        float[] soft = Gui.THEME.getSoftColor();
        float[] harsh = Gui.THEME.getHarshColor();

        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(Liberty.MC.player != null ? Liberty.MC.player.getX() : 749.47));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ",");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(Liberty.MC.player != null ? Liberty.MC.player.getY() : 7.49));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ",");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(Liberty.MC.player != null ? Liberty.MC.player.getZ() : 749.47));
    }

    private void renderOppPosition() {
        float[] soft = Gui.THEME.getSoftColor();
        float[] harsh = Gui.THEME.getHarshColor();

        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], "(");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(getOpposite(Liberty.MC.player != null ? Liberty.MC.player.getX() : 749.47)));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ",");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(getOpposite(Liberty.MC.player != null ? Liberty.MC.player.getZ() : 749.47)));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ")");
    }

    private double getOpposite(double v) {
        if (WorldUtils.isInNether()) {
            return v * 8;
        }

        return v / 8;
    }

    private void renderLastGateway() {
        float[] soft = Gui.THEME.getSoftColor();
        float[] harsh = Gui.THEME.getHarshColor();

        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], "GW");
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ":");
        ImGui.sameLine(2);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], "(");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(lastDetectedGateway.xData));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ",");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], numberFormat.format(lastDetectedGateway.zData));
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], ")");
    }

    private void renderExtraData() {
        float[] soft = Gui.THEME.getSoftColor();
        float[] harsh = Gui.THEME.getHarshColor();

        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], Liberty.MC.player != null ? parseNamespace(Liberty.MC.player.level().dimensionTypeRegistration().getRegisteredName()) : "Purgatory");
        ImGui.sameLine(0);
        ImGui.textColored(harsh[0], harsh[1], harsh[2], harsh[3], " | ");
        ImGui.sameLine(0);
        ImGui.textColored(soft[0], soft[1], soft[2], soft[3], Liberty.MC.player != null ? parseNamespace(Liberty.MC.player.level().getBiome(Liberty.MC.player.getOnPos()).getRegisteredName()) : "Uoh");
    }

    private String parseNamespace(String namespace) {
        if (namespace.startsWith("minecraft:")) {
            return StringUtils.readable(namespace.replace("minecraft:", ""), Config.get().coreSettings);
        }

        return namespace;
    }

    private boolean shouldHaveOpposite() {
        return WorldUtils.isInOW() || WorldUtils.isInNether();
    }
}
