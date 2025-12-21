package monster.psyop.client.impl.modules.vhud;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.ColorSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.impl.events.game.OnRenderSlot;
import monster.psyop.client.impl.events.game.OnScreenRender;
import monster.psyop.client.impl.modules.hud.HUD;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.HashMap;
import java.util.Map;

public class BetterShulkers extends HUD {
    public GroupedSettings displayGroup = addGroup(new GroupedSettings("Display", "Displays the shulker content in HUD's"));
    public GroupedSettings majorityItemsGroup = addGroup(new GroupedSettings("Majority Items", "Displays majority items on shulkers in containers."));

    public BoolSetting displayToggle = new BoolSetting.Builder()
            .name("display")
            .defaultTo(true)
            .addTo(displayGroup);
    public IntSetting offset = new IntSetting.Builder()
            .name("offset-debug")
            .defaultTo(16)
            .range(0, 24)
            .addTo(displayGroup);
    public IntSetting yOffset = new IntSetting.Builder()
            .name("y-offset-debug")
            .defaultTo(16)
            .range(0, 72)
            .addTo(displayGroup);
    public BoolSetting followCursor = new BoolSetting.Builder()
            .name("follow-cursor")
            .defaultTo(true)
            .addTo(displayGroup);
    public IntSetting cursorXOffset = new IntSetting.Builder()
            .name("cursor-x-offset")
            .defaultTo(16)
            .range(-128, 128)
            .addTo(displayGroup);
    public IntSetting cursorYOffset = new IntSetting.Builder()
            .name("cursor-y-offset")
            .defaultTo(72)
            .range(-128, 128)
            .addTo(displayGroup);
    public BoolSetting background = new BoolSetting.Builder()
            .name("background")
            .defaultTo(true)
            .addTo(displayGroup);
    public IntSetting xOffsetSBG = new IntSetting.Builder()
            .name("x-offset-s-bg")
            .defaultTo(0)
            .range(-16, 16)
            .addTo(displayGroup);
    public IntSetting yOffsetSBG = new IntSetting.Builder()
            .name("y-offset-s-bg")
            .defaultTo(0)
            .range(-16, 16)
            .addTo(displayGroup);
    public IntSetting xOffsetEBG = new IntSetting.Builder()
            .name("x-offset-e-bg")
            .defaultTo(-14)
            .range(-16, 16)
            .addTo(displayGroup);
    public IntSetting yOffsetEBG = new IntSetting.Builder()
            .name("y-offset-e-bg")
            .defaultTo(2)
            .range(-16, 16)
            .addTo(displayGroup);
    public ColorSetting backgroundColor = new ColorSetting.Builder()
            .name("background-color")
            .defaultTo(new float[]{0.94f, 0.94f, 0.94f, 0.6f})
            .addTo(displayGroup);
    public FloatSetting scale = new FloatSetting.Builder()
            .name("scale")
            .defaultTo(1.0f)
            .range(0.3f, 2.0f)
            .addTo(displayGroup);

    public BoolSetting majorityItems = new BoolSetting.Builder()
            .name("majority-items")
            .description("Displays the majority item over shulkers in GUI's.")
            .defaultTo(true)
            .addTo(majorityItemsGroup);
    public IntSetting xOffsetMI = new IntSetting.Builder()
            .name("x-offset-m-i")
            .defaultTo(5)
            .range(0, 16)
            .addTo(majorityItemsGroup);
    public IntSetting yOffsetMI = new IntSetting.Builder()
            .name("y-offset-m-i")
            .defaultTo(4)
            .range(0, 16)
            .addTo(majorityItemsGroup);
    public FloatSetting miScale = new FloatSetting.Builder()
            .name("m-i-scale")
            .defaultTo(0.75f)
            .range(0.2f, 0.9f)
            .addTo(majorityItemsGroup);
    public BoolSetting smartMI = new BoolSetting.Builder()
            .name("smart-m-i")
            .description("Returns easier to understand results based on specific items being included.")
            .defaultTo(false)
            .addTo(majorityItemsGroup);
    public BoolSetting prioritizeCrystals = new BoolSetting.Builder()
            .name("prio-crystals")
            .description("Prioritizes showing if a kit has crystals over armor.")
            .defaultTo(false)
            .addTo(majorityItemsGroup);

    private final Item[] diamondArmor = new Item[]{Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
    private final Item[] netheriteArmor = new Item[]{Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS};


    public BetterShulkers() {
        super("better-shulkers", "Displays more information about shulkers in the HUD");
    }

    @EventListener
    public void onRender(OnScreenRender event) {
        GuiGraphics guiGraphics = event.getGuiGraphics();

        if (displayToggle.get()) {
            if (MC.screen instanceof AbstractContainerScreen<?> containerScreen) {
                Slot s = containerScreen.hoveredSlot;
                if (s != null) {
                    ItemStack st = s.getItem();

                    if (!st.has(DataComponents.CONTAINER)) {
                        return;
                    }

                    ItemContainerContents contents = st.get(DataComponents.CONTAINER);

                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(scale.get());

                    int vX = getVanillaX();
                    int vY = getVanillaY();
                    if (followCursor.get()) {
                        vX = (int) MC.mouseHandler.getScaledXPos(MC.getWindow()) + cursorXOffset.get();
                        vY = (int) MC.mouseHandler.getScaledYPos(MC.getWindow()) + cursorYOffset.get();
                    }

                    boolean hasItem = false;

                    for (ItemStack stack : contents.nonEmptyItems()) {
                        if (!stack.isEmpty()) {
                            hasItem = true;
                            break;
                        }
                    }

                    int slot = 0;
                    int row = 0;

                    if (hasItem) {
                        if (background.get()) {
                            guiGraphics.fill(vX - xOffsetSBG.get(), vY - yOffsetSBG.get(), (int) (vX + xOffsetEBG.get() + ((offset.get() * 10) * scale.get())), (int) (vY + yOffsetEBG.get() + ((offset.get() * 3) * scale.get())), ImColorW.toInt(backgroundColor.get()));
                        }

                        for (ItemStack stack : contents.items) {
                            if (slot >= 9) {
                                slot = 0;
                                row++;
                            }

                            if (!stack.isEmpty()) {
                                guiGraphics.renderItem(stack, vX + (offset.get() * slot), vY + (offset.get() * row));
                                guiGraphics.renderItemDecorations(MC.font, stack, vX + (offset.get() * slot), vY + (offset.get() * row));
                            }

                            slot++;
                        }
                    }

                    guiGraphics.pose().popMatrix();
                }
            }
        }
    }

    @EventListener
    public void onRenderSlot(OnRenderSlot.Post event) {
        if (majorityItems.get()) {
            if (event.stack.has(DataComponents.CONTAINER)) {
                ItemContainerContents contents = event.stack.get(DataComponents.CONTAINER);

                if (contents != null) {
                    Item majorityItem = getMajorityItem(contents);

                    if (majorityItem != Items.AIR) {
                        event.guiGraphics.pose().pushMatrix();
                        event.guiGraphics.pose().scale(miScale.get());

                        float inverseScale = 1.0f / miScale.get();
                        float scaledX = (event.x + xOffsetMI.get()) * inverseScale;
                        float scaledY = (event.y + yOffsetMI.get()) * inverseScale;

                        event.guiGraphics.renderItem(majorityItem.getDefaultInstance(), (int) scaledX, (int) scaledY);
                        event.guiGraphics.pose().popMatrix();
                    }
                }
            }
        }
    }

    public Item getMajorityItem(ItemContainerContents contents) {
        Map<Item, Integer> itemCounts = new HashMap<>();

        for (ItemStack stack : contents.nonEmptyItems()) {
            if (!stack.isEmpty()) {
                itemCounts.put(stack.getItem(), itemCounts.getOrDefault(stack.getItem(), 0) + 1);
            }
        }

        if (smartMI.get()) {
            if (prioritizeCrystals.get()) {
                if (itemCounts.containsKey(Items.END_CRYSTAL)) {
                    return Items.END_CRYSTAL;
                }
            }

            int armorCount = 0;

            for (Item item : diamondArmor) {
                if (itemCounts.containsKey(item)) {
                    armorCount++;
                }
            }

            if (armorCount >= 4) {
                return Items.DIAMOND_HELMET;
            }

            armorCount = 0;

            for (Item item : netheriteArmor) {
                if (itemCounts.containsKey(item)) {
                    armorCount++;
                }
            }

            if (armorCount >= 4) {
                return Items.NETHERITE_HELMET;
            }

            if (itemCounts.containsKey(Items.TADPOLE_BUCKET)) {
                return Items.REPEATING_COMMAND_BLOCK;
            }

            if (itemCounts.containsKey(Items.COOKIE)) {
                return Items.EMERALD_BLOCK;
            }

            if (itemCounts.containsKey(Items.END_CRYSTAL)) {
                return Items.END_CRYSTAL;
            }

            if (itemCounts.containsKey(Items.MACE)) {
                return Items.MACE;
            }

        }

        Item maxedItem = Items.AIR;
        int maxCount = 0;

        for (Map.Entry<Item, Integer> counts : itemCounts.entrySet()) {
            if (counts.getValue() > maxCount) {
                maxedItem = counts.getKey();
                maxCount = counts.getValue();
            }
        }

        return maxedItem;
    }

    @Override
    public int getWidth() {
        return (int) (((offset.get() * 9) * MC.options.guiScale().get()) * scale.get());
    }

    @Override
    public int getHeight() {
        return (int) (((yOffset.get() * 3) * MC.options.guiScale().get()) * scale.get());
    }
}
