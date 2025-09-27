package monster.psyop.client.framework.modules.settings.wrappers;

import imgui.type.ImInt;
import net.minecraft.core.BlockPos;

public class ImBlockPos {
    public int[] data = new int[]{0,0,0};
    protected transient BlockPos.MutableBlockPos original = new BlockPos.MutableBlockPos(0, 0, 0);

    public ImBlockPos() {
    }

    public ImBlockPos(int x, int y, int z) {
        this.x(x);
        this.y(x);
        this.z(x);
    }

    public int x(int i) {
        data[0] = i;
        original.setX(i);
        return i;
    }

    public int y(int i) {
        data[1] = i;
        original.setY(i);
        return i;
    }

    public int z(int i) {
        data[2] = i;
        original.setZ(i);
        return i;
    }

    public int x() {
        return data[0];
    }

    public int y() {
        return data[1];
    }

    public int z() {
        return data[2];
    }

    public BlockPos asImmutable() {
        return new BlockPos(x(), y(), z());
    }

    public BlockPos.MutableBlockPos get() {
        return original;
    }
}
