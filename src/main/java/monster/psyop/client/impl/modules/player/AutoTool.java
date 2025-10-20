package monster.psyop.client.impl.modules.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import monster.psyop.client.impl.events.game.OnPacket;
import monster.psyop.client.utility.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;

public class AutoTool extends Module {

    public final IntSetting preference = new IntSetting.Builder()
            .name("preference")
            .description("Which enchantment to prefer. 0: None, 1: Fortune, 2: Silk Touch.")
            .defaultTo(0)
            .range(0, 2)
            .addTo(coreGroup);

    public final BoolSetting switchBack = new BoolSetting.Builder()
            .name("switch-back")
            .description("Switches back to your original slot after breaking the block.")
            .defaultTo(true)
            .addTo(coreGroup);

    public final IntSetting switchDelay = new IntSetting.Builder()
            .name("switch-delay")
            .description("The delay in ticks before switching back (20 ticks = 1 second).")
            .defaultTo(10)
            .range(0, 100)
            .addTo(coreGroup);

    private int originalSlot = -1;
    private int switchBackDelay = -1;

    public AutoTool() {
        super(Categories.PLAYER, "auto-tool", "Automatically switches to the best tool for the job.");
    }

    @Override
    public void update() {
        if (MC.player == null) return;

        if (switchBackDelay > 0) {
            switchBackDelay--;
        } else if (switchBackDelay == 0 && originalSlot != -1) {
            InventoryUtils.swapSlot(originalSlot);
            originalSlot = -1;
            switchBackDelay = -1;
        }
    }

    @EventListener
    public void onPacketSend(OnPacket.Send event) {
        if (MC.player == null || MC.level == null) return;

        if (event.packet() instanceof ServerboundPlayerActionPacket packet) {
            if (packet.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                switchBackDelay = -1; // Cancel any pending switch-back
                findAndSwitch(packet.getPos());
            } else if (packet.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                if (switchBack.get() && originalSlot != -1) {
                    switchBackDelay = switchDelay.get();
                }
            }
        }
    }

    public void findAndSwitch(BlockPos pos) {
        if (MC.player == null || MC.level == null) return;

        BlockState blockState = MC.level.getBlockState(pos);
        float bestSpeed = 1.0f;
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            float speed = stack.getDestroySpeed(blockState);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            } else if (speed == bestSpeed && bestSlot != -1) {
                int pref = preference.get();
                if (pref != 0) {
                    ItemStack currentBestStack = MC.player.getInventory().getItem(bestSlot);
                    int currentBestEnchant = getEnchantmentLevel(currentBestStack, pref);
                    int newEnchant = getEnchantmentLevel(stack, pref);

                    if (newEnchant > currentBestEnchant) {
                        bestSlot = i;
                    }
                }
            }
        }

        if (bestSlot != -1 && bestSlot != MC.player.getInventory().getSelectedSlot()) {
            originalSlot = MC.player.getInventory().getSelectedSlot();
            InventoryUtils.swapSlot(bestSlot);
        }
    }

    private int getEnchantmentLevel(ItemStack stack, int preference) {
        if (stack.isEmpty()) return 0;

        ResourceKey<Enchantment> key;
        if (preference == 1) { // Fortune
            key = Enchantments.FORTUNE;
        } else if (preference == 2) { // Silk Touch
            key = Enchantments.SILK_TOUCH;
        } else {
            return 0;
        }

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) {
            return 0;
        }

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (entry.getKey().is(key)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }
}
