package monster.psyop.client.impl.modules.render;

import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.types.BlockListSetting;
import monster.psyop.client.framework.modules.settings.types.IntSetting;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class BlockLights extends Module {
    public BlockListSetting blockList = new BlockListSetting.Builder()
            .name("blocks")
            .defaultTo(List.of(Blocks.OBSIDIAN, Blocks.BEDROCK))
            .addTo(coreGroup);
    public IntSetting light = new IntSetting.Builder()
            .name("light")
            .defaultTo(12)
            .range(0, 15)
            .addTo(coreGroup);

    public BlockLights() {
        super(Categories.RENDER, "block-lights", "Modifies blocks brightness.");
    }
}
