package monster.psyop.client.utility;

import monster.psyop.client.framework.modules.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.awt.*;

public class TextUtils {
    public static Style MODULE_NAME_STYLE = Style.EMPTY.withColor(new Color(200, 141, 232).getRGB()).withBold(true);
    public static Style MODULE_INFO_STYLE = Style.EMPTY.withColor(new Color(179, 17, 228).getRGB());
    public static Style MODULE_INFO_SUB_STYLE = Style.EMPTY.withColor(new Color(171, 64, 232).getRGB()).withItalic(true);

    public static MutableComponent BRACKET_OPEN_COMPONENT = Component.literal("[").setStyle(Style.EMPTY.withColor(new Color(91, 9, 168).getRGB()).withBold(true));
    public static MutableComponent BRACKET_CLOSE_COMPONENT = Component.literal("]").setStyle(Style.EMPTY.withColor(new Color(91, 9, 168).getRGB()).withBold(true));
    public static MutableComponent CLIENT_TITLE = Component.literal("Liberty - Psyop").withStyle(TextUtils.MODULE_NAME_STYLE);

    public static MutableComponent getModuleNameFormat(Module module) {
        MutableComponent component = Component.empty();

        component.append(BRACKET_OPEN_COMPONENT);

        component.append(Component.literal(module.getLabel()).withStyle(MODULE_NAME_STYLE));

        component.append(BRACKET_CLOSE_COMPONENT);

        return component;
    }
}
