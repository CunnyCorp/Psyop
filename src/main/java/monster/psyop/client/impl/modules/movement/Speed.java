package monster.psyop.client.impl.modules.movement;

import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.Module;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.BoolSetting;
import monster.psyop.client.framework.modules.settings.types.FloatSetting;
import monster.psyop.client.impl.events.game.OnMove;
import net.minecraft.world.phys.Vec3;

public class Speed extends Module {
    public GroupedSettings attributeGroup = addGroup(new GroupedSettings("Attribute", "Modifies speed the classical way."));
    public BoolSetting attribute = new BoolSetting.Builder()
            .name("attribute")
            .defaultTo(true)
            .addTo(attributeGroup);
    public FloatSetting speedBase = new FloatSetting.Builder()
            .name("speed-base")
            .description("A base increase to speed.")
            .defaultTo(4.0f)
            .range(0.0f, 10.0f)
            .addTo(attributeGroup);
    public FloatSetting speedMulti = new FloatSetting.Builder()
            .name("speed-multi")
            .defaultTo(1.0f)
            .range(1.0f, 10.0f)
            .addTo(attributeGroup);
    public GroupedSettings accelGroup = addGroup(new GroupedSettings("Acceleration", "Modifies delta movement by accelerating it."));
    public BoolSetting accel = new BoolSetting.Builder()
            .name("accel")
            .defaultTo(true)
            .addTo(accelGroup);
    public BoolSetting onlyInAir = new BoolSetting.Builder()
            .name("only-in-air")
            .defaultTo(true)
            .addTo(accelGroup);
    public FloatSetting accelMulti = new FloatSetting.Builder()
            .name("accel-multi")
            .defaultTo(0.12f)
            .range(0.0f, 1.0f)
            .addTo(accelGroup);
    public FloatSetting maxAccel = new FloatSetting.Builder()
            .name("max-accel")
            .defaultTo(0.5f)
            .range(0.0f, 5.0f)
            .addTo(accelGroup);
    public BoolSetting fastFall = new BoolSetting.Builder()
            .name("fast-fall")
            .defaultTo(true)
            .addTo(accelGroup);
    public BoolSetting autoJump = new BoolSetting.Builder()
            .name("auto-jump")
            .defaultTo(true)
            .addTo(accelGroup);

    public Speed() {
        super(Categories.MOVEMENT, "speed", "Allows you to continuously accelerate in the air.");
    }

    @Override
    public void update() {
        if (autoJump.get()) {
            MC.options.keyJump.setDown(MC.options.keyUp.isDown() || MC.options.keyLeft.isDown() || MC.options.keyDown.isDown() || MC.options.keyRight.isDown());
        }
    }

    @EventListener
    public void onPlayerMove(OnMove.Player event) {
        if (accel.get()) {
            if (onlyInAir.get() && MC.player.onGround()) {
                return;
            }

            Vec3 acc = event.vec3.multiply(accelMulti.get(), 1.0f, accelMulti.get());

            if (Math.abs(event.vec3.x) < maxAccel.get() && Math.abs(event.vec3.z) < maxAccel.get()) {
                event.vec3.x += acc.x;
                event.vec3.z += acc.z;
            }

            if (fastFall.get()) {
                if (event.vec3.y < 0) {
                    event.vec3.y += (event.vec3.y * accelMulti.get());
                    if (event.vec3.y < -maxAccel.get()) {
                        event.vec3.y = -maxAccel.get();
                    }
                }
            }
        }
    }

    @Override
    public AntiCheat getAntiCheat() {
        return AntiCheat.Veck_Grim;
    }
}
