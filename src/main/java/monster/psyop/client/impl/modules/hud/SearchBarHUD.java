package monster.psyop.client.impl.modules.hud;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.game.OnRenderSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class SearchBarHUD extends HUD {
    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public final ColorSetting highlightColor =
            new ColorSetting.Builder()
                    .name("highlight-color")
                    .defaultTo(new float[]{0.6f, 1.0f, 0.0f, 0.3f})
                    .addTo(coreGroup);
    public final ColorSetting dampeningColor =
            new ColorSetting.Builder()
                    .name("dampening-color")
                    .defaultTo(new float[]{0.0f, 0.0f, 0.0f, 0.7f})
                    .addTo(coreGroup);
    public final BoolSetting customNames =
            new BoolSetting.Builder()
                    .name("custom-names")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final BoolSetting checkEnchants =
            new BoolSetting.Builder()
                    .name("check-enchants")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public final BoolSetting books =
            new BoolSetting.Builder()
                    .name("books")
                    .defaultTo(true)
                    .addTo(coreGroup);


    public ImString searchText = new ImString();

    public SearchBarHUD() {
        super("search-bar", "Highlights desired items.");
    }

    @Override
    public void render() {
        if (MC.screen instanceof AbstractContainerScreen<?>) {
            ImGui.begin("Search Bar", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoDecoration);

            ImGui.inputTextWithHint("##search_bar_input", "Tadpole Bucket", searchText);

            ImGui.end();
        }
    }

    @EventListener
    public void onRenderSlot(OnRenderSlot.Post event) {
        if (searchText.isEmpty()) {
            return;
        }

        if (matchSearch(event.stack)) {
            event.guiGraphics.fill(event.x, event.y, event.x + 16, event.y + 16, ImColorW.toInt(highlightColor.get()));
        } else {
            event.guiGraphics.fill(event.x, event.y, event.x + 16, event.y + 16, ImColorW.toInt(dampeningColor.get()));
        }
    }

    public boolean matchSearch(ItemStack stack) {
        String search = sanitizeString(searchText.toString());

        if (sanitizeString(stack.getItem().getDescriptionId()).contains(search)) {
            return true;
        }

        if (customNames.get() && stack.has(DataComponents.CUSTOM_NAME) && sanitizeString(stack.getCustomName().getString()).contains(search)) {
            return true;
        }

        if (checkEnchants.get() && stack.has(DataComponents.ENCHANTMENTS)) {
            ItemEnchantments enchantments = stack.getEnchantments();

            for (Holder<Enchantment> holder : enchantments.keySet()) {
                if (enchantments.getLevel(holder) > 0) {
                    String sanitized = sanitizeString(Enchantment.getFullname(holder, enchantments.getLevel(holder)).getString());

                    if (sanitized.contains(search)) {
                        return true;
                    }
                }
            }
        }

        if (books.get() && stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);

            if (sanitizeString(content.author()).contains(search)) {
                return true;
            }

            if (sanitizeString(content.title().get(false)).contains(search)) {
                return true;
            }
        }

        if (stack.has(DataComponents.MAP_ID)) {
            if (Integer.toString(stack.get(DataComponents.MAP_ID).id()).startsWith(search)) {
                return true;
            }
        }

        if (stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

            for (ItemStack st : contents.nonEmptyItems()) {
                if (matchSearch(st)) {
                    return true;
                }
            }
        }

        if (stack.has(DataComponents.BUNDLE_CONTENTS)) {
            BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);

            for (ItemStack st : contents.items()) {
                if (matchSearch(st)) {
                    return true;
                }
            }
        }

        return false;
    }

    public String sanitizeString(String str) {
        String sanitized = str.toLowerCase();

        sanitized = sanitized.replaceAll("([ _.])", "");

        return sanitized.trim();
    }
}