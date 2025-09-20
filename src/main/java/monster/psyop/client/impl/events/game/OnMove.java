package monster.psyop.client.impl.events.game;

import monster.psyop.client.Psyop;
import monster.psyop.client.framework.events.Event;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public abstract class OnMove extends Event {
    public Vec3 vec3 = new Vec3(0, 0, 0);
    public MoverType moverType = MoverType.SELF;
    public net.minecraft.world.entity.Entity entity;

    public OnMove() {
        super(false);
    }


    public static class Player extends OnMove {
        public static Player INSTANCE = new Player();

        public Player() {
            INSTANCE = this;
        }

        public static Player get(Vec3 vec3, MoverType moverType) {
            INSTANCE.refresh();
            INSTANCE.vec3 = vec3;
            INSTANCE.moverType = moverType;
            INSTANCE.entity = Psyop.MC.player;
            return INSTANCE;
        }
    }

    public static class Entity extends OnMove {
        public static Entity INSTANCE = new Entity();

        public Entity() {
            INSTANCE = this;
        }

        public static Entity get(Vec3 vec3, MoverType moverType, net.minecraft.world.entity.Entity entity) {
            INSTANCE.refresh();
            INSTANCE.vec3 = vec3;
            INSTANCE.moverType = moverType;
            INSTANCE.entity = entity;
            return INSTANCE;
        }
    }
}
