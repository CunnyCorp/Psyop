package monster.psyop.client.impl.modules.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import imgui.ImGui;
import imgui.ImVec2;
import monster.psyop.client.framework.events.EventListener;
import monster.psyop.client.framework.friends.FriendManager;
import monster.psyop.client.framework.modules.Categories;
import monster.psyop.client.framework.modules.settings.GroupedSettings;
import monster.psyop.client.framework.modules.settings.types.*;
import monster.psyop.client.framework.modules.settings.wrappers.ImColorW;
import monster.psyop.client.framework.rendering.Render3DUtil;
import monster.psyop.client.impl.events.On2DRender;
import monster.psyop.client.impl.events.game.OnRender;
import monster.psyop.client.impl.modules.hud.HUD;
import monster.psyop.client.utility.InventoryUtils;
import monster.psyop.client.utility.PacketUtils;
import monster.psyop.client.utility.gui.GradientUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static monster.psyop.client.Psyop.GUI;

public class KillAura extends HUD {
    public final BoolSetting displayHUD =
            new BoolSetting.Builder()
                    .name("display-hud")
                    .description("Displays a HUD element with entity info!")
                    .defaultTo(false)
                    .addTo(coreGroup);
    public ColorSetting textColor =
            new ColorSetting.Builder()
                    .name("text-color")
                    .defaultTo(new float[]{0.90f, 0.90f, 0.95f, 0.95f})
                    .addTo(coreGroup);
    public ColorSetting upperColor =
            new ColorSetting.Builder()
                    .name("upper-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting middleColor =
            new ColorSetting.Builder()
                    .name("middle-color")
                    .defaultTo(new float[]{0.00f, 0.60f, 0.60f, 1.0f})
                    .addTo(coreGroup);
    public ColorSetting lowerColor =
            new ColorSetting.Builder()
                    .name("lower-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public final IntSetting alpha =
            new IntSetting.Builder()
                    .name("alpha")
                    .range(40, 200)
                    .defaultTo(184)
                    .addTo(coreGroup);
    public final IntSetting waveSpeed =
            new IntSetting.Builder()
                    .name("wave-speed")
                    .range(1, 10)
                    .defaultTo(3)
                    .addTo(coreGroup);
    public final IntSetting waveDensity =
            new IntSetting.Builder()
                    .name("wave-density")
                    .range(1, 10)
                    .defaultTo(5)
                    .addTo(coreGroup);
    public final BoolSetting shouldGlow =
            new BoolSetting.Builder()
                    .name("should-glow")
                    .description("Makes the target entity glow.")
                    .defaultTo(true)
                    .addTo(coreGroup);
    public ColorSetting glowColor =
            new ColorSetting.Builder()
                    .name("glow-color")
                    .defaultTo(new float[]{0.00f, 0.75f, 0.75f, 1.0f})
                    .addTo(coreGroup);
    public FloatSetting circleRadius = new FloatSetting.Builder()
            .name("circle-radius")
            .defaultTo(0.5f).range(0.1f, 1.5f)
            .addTo(coreGroup);
    public FloatSetting gyroSpeed = new FloatSetting.Builder()
            .name("gyro-speed")
            .defaultTo(0.015f).range(0.0f, 2.0f)
            .addTo(coreGroup);
    public BoolSetting pretty = new BoolSetting.Builder()
            .name("pretty")
            .defaultTo(false)
            .addTo(coreGroup);
    public final GroupedSettings switchGroup = addGroup(new GroupedSettings("auto-switch", "Automatically switch to weapons."));
    public final BoolSetting autoSwitch =
            new BoolSetting.Builder()
                    .name("auto-switch")
                    .description("Automatically switch to weapons.")
                    .defaultTo(true)
                    .addTo(switchGroup);
    public final IntSetting dedicatedSlot =
            new IntSetting.Builder()
                    .name("dedicated-slot")
                    .description("Slot for weapons!")
                    .defaultTo(0)
                    .range(0, 8)
                    .addTo(switchGroup);
    public ItemListSetting weapons =
            new ItemListSetting.Builder()
                    .name("weapons")
                    .description("Weapons to switch to.")
                    .defaultTo(List.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD))
                    .filter((v) -> v.getDefaultInstance().has(DataComponents.WEAPON))
                    .addTo(switchGroup);
    public final IntSetting timeout =
            new IntSetting.Builder()
                    .name("timeout")
                    .description("Timeout for KillAura after switching.")
                    .defaultTo(2)
                    .range(0, 10)
                    .addTo(switchGroup);
    public final GroupedSettings checksGroup = addGroup(new GroupedSettings("checks", "Checks to run on entities before attacking."));
    public final EntityListSetting entityTypes =
            new EntityListSetting.Builder()
                    .name("entity-types")
                    .description("The types of entities to attack.")
                    .defaultTo(List.of(EntityType.PLAYER))
                    .addTo(checksGroup);
    public final BoolSetting noCustomNames =
            new BoolSetting.Builder()
                    .name("no-named")
                    .description("Don't attack entities with custom names.")
                    .defaultTo(false)
                    .addTo(checksGroup);
    public final BoolSetting visibleCheck =
            new BoolSetting.Builder()
                    .name("is-visible")
                    .description("Makes sure a entity is visible before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting attackCheck =
            new BoolSetting.Builder()
                    .name("can-attack")
                    .description("Makes sure a entity can be attacked before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);
    public final BoolSetting healthCheck =
            new BoolSetting.Builder()
                    .name("healthy")
                    .description("Makes sure a entity is healthy before attacking.")
                    .defaultTo(true)
                    .addTo(checksGroup);

    private final GradientUtils gradientUtils = new GradientUtils(0.5f);
    private int delay = 0;
    public Entity target = null;
    public float r = 0;
    public boolean goUp = true;
    public float circleAnimationPos = 0f;
    public boolean goingDown = false;

    public KillAura() {
        super(
                Categories.COMBAT,
                "kill-aura",
                "Automatically hits entities around the player.");
    }

    @EventListener(inGame = false)
    public void onRender2D(On2DRender event) {
        if (!displayHUD.get()) {
            return;
        }

        String targetingDistance = "0 Blocks Away";
        String targetingHealth = "0/0";
        String targetingName = "No Target";

        assert MC.player != null;
        if (target != null) {
            targetingDistance = Math.round(MC.player.distanceTo(target)) + " Blocks Away";
            if (target instanceof LivingEntity le) {
                targetingHealth = Math.round(le.getHealth() + le.getAbsorptionAmount()) + "/" + Math.round(le.getMaxHealth());
            }

            if (target.hasCustomName()) {
                targetingName = target.getDisplayName().getString();
            } else {
                targetingName = EntityType.getKey(target.getType()).getPath();
            }
        }


        ImVec2 textSize = new ImVec2();
        ImGui.calcTextSize(textSize, targetingDistance.length() > targetingName.length() ? targetingDistance : targetingName);

        float padding = 8f;
        float bgWidth = textSize.x + (padding * 2);
        float bgHeight = (textSize.y * 3) + 6 + (padding * 2);

        float bgX = xPos.get() - padding;
        float bgY = yPos.get() - padding;

        Color[] waveColors = {
                GradientUtils.getColorFromSetting(upperColor),
                GradientUtils.getColorFromSetting(middleColor),
                GradientUtils.getColorFromSetting(lowerColor),
                GradientUtils.getColorFromSetting(middleColor)
        };

        gradientUtils.drawHorizontalWaveGradientTile(
                bgX, bgY, bgWidth, bgHeight,
                waveColors, alpha.get(),
                waveSpeed.get() / 2f,
                waveDensity.get() / 2f
        );

        float textX = bgX + padding;
        float textY = bgY + padding;

        ImColorW colorW = new ImColorW(textColor.get());

        GUI.drawString(targetingName, textX, textY, colorW);

        textY += textSize.y + 2;
        GUI.drawString(targetingHealth, textX, textY, colorW);

        textY += textSize.y + 2;
        GUI.drawString(targetingDistance, textX, textY, colorW);
    }

    @Override
    public void update() {
        assert MC.player != null;

        delay--;

        if (goingDown) {
            circleAnimationPos -= gyroSpeed.get();
            if (circleAnimationPos <= 0) {
                goingDown = false;
            }
        } else {
            circleAnimationPos += gyroSpeed.get();

            if (circleAnimationPos >= (target != null ? target.getBbHeight() : 1)) {
                goingDown = true;
            }
        }

        if (pretty.get()) {
            if (goUp) r += 0.01f;
            else r -= 0.01f;

            if (r >= 1.0f) {
                goUp = false;
            } else if (r <= 0.0f) {
                goUp = true;
            }
        }

        if (target != null) {
            if (!target.isAlive()) {
                target = null;
            }

            if (target.distanceTo(MC.player) > MC.player.entityInteractionRange()) {
                target = null;
            }
        }

        if (MC.player.getAttackStrengthScale(0.5f) < 1.0f || delay > 0) {
            return;
        }

        if (target != null && autoSwitch.get()) {
            if (!weapons.value().contains(MC.player.getMainHandItem().getItem())) {
                delay = timeout.get();
                InventoryUtils.swapToHotbar(InventoryUtils.findAnySlot(weapons.value()), dedicatedSlot.get());
                InventoryUtils.swapSlot(dedicatedSlot.get());
                return;
            }
        }

        List<Entity> entities = filterEntities();

        if (entities.isEmpty()) {
            return;
        }

        entities.sort(Comparator.comparingDouble((e) -> e.distanceToSqr(MC.player)));

        target = entities.get(0);

        if (!autoSwitch.get() || weapons.value().contains(MC.player.getMainHandItem().getItem())) {
            attackEntity(target);
        }
    }

    protected void attackEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        assert MC.player != null;
        MC.player.resetAttackStrengthTicker();

        // 9b doesn't need swinging!
        PacketUtils.send(
                ServerboundInteractPacket.createAttackPacket(
                        entity, MC.player.isShiftKeyDown()));
    }

    protected List<Entity> filterEntities() {
        ArrayList<Entity> filteredEntities = new ArrayList<>();

        if (MC.player == null) {
            return filteredEntities;
        }

        for (Entity entity : getNearbyEntitiesRaw()) {
            if (!entityTypes.value().contains(entity.getType())) {
                continue;
            }

            if (MC.player.getEyePosition().distanceTo(entity.position()) > MC.player.entityInteractionRange()) {
                continue;
            }

            if (attackCheck.get() && !entity.isAttackable()) {
                continue;
            }

            if (visibleCheck.get() && (entity.isInvisible() || entity.isInvisibleTo(MC.player))) {
                continue;
            }

            boolean isPlayer = entity instanceof Player;

            if (entity instanceof LivingEntity living) {
                if (visibleCheck.get() && !living.canBeSeenByAnyone()) {
                    continue;
                }

                if (!isPlayer && noCustomNames.get() && living.hasCustomName()) {
                    continue;
                }

                if (healthCheck.get() && (living.getHealth() <= 0 || living.getMaxHealth() <= 0)) {
                    continue;
                }
            }

            if (entity instanceof Player player) {
                if (!FriendManager.canAttack(player)) {
                    continue;
                }
            }

            filteredEntities.add(entity);
        }

        return filteredEntities;
    }

    protected List<Entity> getNearbyEntitiesRaw() {
        if (MC.player == null) {
            return new ArrayList<>();
        }

        return MC.player.level().getEntities(MC.player, new AABB(MC.player.getX() + 9, MC.player.getY() + 9, MC.player.getZ() + 9, MC.player.getX() - 9, MC.player.getY() - 9, MC.player.getZ() - 9));
    }

    @EventListener
    public void onRender3D(OnRender event) {
        if (MC == null || MC.level == null || MC.player == null) return;
        if (target == null || !target.isAlive()) return;

        PoseStack.Pose pose = event.poseStack.last();


        Vec3 cam = MC.gameRenderer.getMainCamera().getPosition();
        double camX = cam.x();
        double camY = cam.y();
        double camZ = cam.z();

        float cx = (float) (target.position().x - camX);
        float cy = (float) ((target.position().y + circleAnimationPos) - camY);
        float cz = (float) (target.position().z - camZ);

        float[] c;

        if (pretty.get()) {
            c = new float[]{r, 1.0f, 1.0f, 1.0f};
        } else {
            c = glowColor.get();
        }

        Render3DUtil.drawCircleEdgesXZ(event.quads, pose, cx, cy, cz, circleRadius.get(), 24, c[0], c[1], c[2], c[3]);
        Render3DUtil.drawCircleEdgesXZ(event.quads, pose, cx, cy - 0.1f, cz, circleRadius.get(), 24, c[0], c[1], c[2], c[3]);

    }
}
