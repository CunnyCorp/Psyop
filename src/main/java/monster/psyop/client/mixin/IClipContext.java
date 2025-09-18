package monster.psyop.client.mixin;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ClipContext.class)
public interface IClipContext {
    @Accessor("from")
    void setFrom(Vec3 from);

    @Accessor("to")
    void setTo(Vec3 to);

    @Accessor("block")
    void setBlock(ClipContext.Block block);

    @Accessor("fluid")
    void setFluid(ClipContext.Fluid fluid);

    @Accessor("collisionContext")
    void setCollisionContext(CollisionContext collisionContext);
}
