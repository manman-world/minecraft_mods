package cn.skyplanes;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class Plane extends Entity {
    private double speed;
    private final net.minecraft.world.entity.InterpolationHandler interpolation = new net.minecraft.world.entity.InterpolationHandler(this, 3);
    @Override public net.minecraft.world.entity.InterpolationHandler getInterpolation() { return interpolation; }
    public Plane(EntityType<? extends Plane> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput input) { speed = 0; }
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
    @Override public boolean isPickable() { return true; }
    @Override public boolean canBeCollidedWith(Entity other) { return true; }
    @Override protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!level().isClientSide()) {
            player.startRiding(this);
            if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendSystemMessage(Component.literal("飞机：W加速 / S减速；鼠标转向、抬头起飞、低头降落；Shift下机"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide()) { interpolation.interpolate(); return; }
        double vertical = getDeltaMovement().y;
        if (getFirstPassenger() instanceof ServerPlayer pilot) {
            var input = pilot.getLastClientInput();
            speed = FlightPhysics.speed(speed, input.forward(), input.backward());
            setYRot(Mth.approachDegrees(getYRot(), pilot.getYRot(), 3.0f));
            float pitch = Mth.clamp(pilot.getXRot(), -30, 35);
            setXRot(pitch);
            vertical = FlightPhysics.vertical(speed, pitch, vertical);
            if (tickCount % 20 == 0) pilot.sendOverlayMessage(Component.literal(
                String.format("速度 %.0f 格/秒 | %s", speed * 20, onGround() ? "滑行：加速后抬头起飞" : "飞行：低头缓降，S减速")));
        } else {
            speed *= 0.96;
            vertical = Math.max(vertical - 0.04, -0.6);
            setXRot(0);
        }
        double yaw = Math.toRadians(getYRot());
        setDeltaMovement(new Vec3(-Math.sin(yaw) * speed, vertical, Math.cos(yaw) * speed));
        move(MoverType.SELF, getDeltaMovement());
        if (horizontalCollision) speed = 0;
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1, 0, 1));
        fallDistance = 0;

    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player && !isVehicle()) {
            spawnAtLocation(level, SkyPlanes.PLANE_KIT.get());
            discard();
            return true;
        }
        return false;
    }
}





