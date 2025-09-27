package monster.psyop.client.framework.modules.settings.wrappers;

import imgui.type.ImInt;
import net.minecraft.core.BlockPos;

public class ImBlockPos {
    public ImInt[] data = new ImInt[]{new ImInt(0), new ImInt(0), new ImInt(0)};
    protected transient BlockPos.MutableBlockPos original = new BlockPos.MutableBlockPos(0, 0, 0);

    public ImBlockPos() {
    }

    public ImBlockPos(int x, int y, int z) {
        this.x(x);
        this.y(x);
        this.z(x);
    }

    public int x(int i) {
        data[0].set(i);
        original.setX(i);
        return i;
    }

    public int y(int i) {
        data[1].set(i);
        original.setY(i);
        return i;
    }

    public int z(int i) {
        data[2].set(i);
        original.setZ(i);
        return i;
    }

    public int x() {
        return data[0].get();
    }

    public int y() {
        return data[1].get();
    }

    public int z() {
        return data[2].get();
    }

    public BlockPos asImmutable() {
        return new BlockPos(x(), y(), z());
    }

    public BlockPos.MutableBlockPos get() {
        return original;
    }
}
