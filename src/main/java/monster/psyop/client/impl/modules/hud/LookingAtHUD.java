package monster.psyop.client.impl.modules.hud;

import monster.psyop.client.Psyop;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class LookingAtHUD extends HUD {
    public LookingAtHUD() {
        super("looking-at", "Displays the entity/block you're looking at.");
    }

    @Override
    public void render() {
        if (MC.hitResult == null) {
            return;
        }

        String type = MC.hitResult.getType().name();
        String info = "";

        if (MC.hitResult instanceof BlockHitResult hitResult) {
            info = hitResult.getBlockPos().toString();
        } else if (MC.hitResult instanceof EntityHitResult hitResult) {
            info = hitResult.getEntity().getDisplayName().getString();
        }

        Psyop.GUI.drawString("Looking at: " + type + " - Info: " + info, xPos.get(), yPos.get(), 40, true);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }
}
