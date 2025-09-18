package monster.psyop.client.framework.modules.settings.wrappers;

import imgui.type.ImInt;
import net.minecraft.core.BlockPos;

public class ImBlockPos {
    public ImInt xData = new ImInt(0);
    public ImInt yData = new ImInt(0);
    public ImInt zData = new ImInt(0);
    protected transient BlockPos.MutableBlockPos original = new BlockPos.MutableBlockPos(0, 0, 0);

    public ImBlockPos() {
    }

    public ImBlockPos(int x, int y, int z) {
        this.x(x);
        this.y(x);
        this.z(x);
    }

    public int x(int i) {
        xData.set(i);
        original.setX(i);
        return i;
    }

    public int y(int i) {
        yData.set(i);
        original.setY(i);
        return i;
    }

    public int z(int i) {
        zData.set(i);
        original.setZ(i);
        return i;
    }

    public int x() {
        return xData.get();
    }

    public int y() {
        return yData.get();
    }

    public int z() {
        return zData.get();
    }

    public BlockPos asImmutable() {
        return new BlockPos(x(), y(), z());
    }

    public BlockPos.MutableBlockPos get() {
        return original;
    }
}
