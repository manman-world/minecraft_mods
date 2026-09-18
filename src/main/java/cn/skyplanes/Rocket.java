package cn.skyplanes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative launch, damage and effects; clients interpolate tracked positions. */
public final class Rocket extends Entity {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.INT);
    private final InterpolationHandler interpolation = new InterpolationHandler(this, 3);
    private RocketFlight flight = new RocketFlight();
    public Rocket(EntityType<? extends Rocket> type, Level level) { super(type, level); }
    @Override public InterpolationHandler getInterpolation() { return interpolation; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(PHASE, 0); }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        // Missing tags belong to the earlier static rockets and retain their idle state.
        flight = RocketFlight.restore(new RocketFlight.Saved(input.getIntOr("RocketPhase", 0),
            input.getIntOr("RocketCountdown", 0), input.getIntOr("RocketFlightTicks", 0),
            input.getDoubleOr("RocketLaunchY", getY())));
        syncPhase();
        setDeltaMovement(Vec3.ZERO);
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        var saved = flight.save();
        output.putInt("RocketPhase", saved.phase());
        output.putInt("RocketCountdown", saved.countdown());
        output.putInt("RocketFlightTicks", saved.flightTicks());
        output.putDouble("RocketLaunchY", saved.launchY());
    }
    private void syncPhase() { entityData.set(PHASE, flight.phase().ordinal()); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean canBeCollidedWith(Entity other) { return true; }
    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(hand).isEmpty()
                || player.isSecondaryUseActive() || player.isSpectator() || !player.mayBuild())
            return InteractionResult.PASS;
        if (isRemoved()) return InteractionResult.FAIL;
        if (level() instanceof ServerLevel server) {
            boolean ignited = flight.ignite(getY());
            if (ignited) {
                syncPhase();
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1, 1);
            }
            if (player instanceof ServerPlayer serverPlayer)
                serverPlayer.sendOverlayMessage(Component.translatable(ignited ? "message.skyplanes.rocket_ignited" : "message.skyplanes.rocket_active"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override public void tick() {
        super.tick();
        if (isRemoved()) return;
        if (level().isClientSide()) { interpolation.interpolate(); return; }
        ServerLevel server = (ServerLevel) level();
        setDeltaMovement(Vec3.ZERO);
        if (flight.phase() == RocketFlight.Phase.FINISHED) { discard(); return; }
        if (flight.phase() == RocketFlight.Phase.IDLE) return;

        double speed = flight.nextSpeed();
        var swept = getBoundingBox().expandTowards(0, speed, 0);
        boolean blocked = !server.noCollision(this, swept) || !server.getWorldBorder().isWithinBounds(swept);
        boolean ceiling = swept.maxY > server.getMaxY() + 1.0;
        if (flight.phase() == RocketFlight.Phase.COUNTDOWN && flight.countdown() % 20 == 0)
            server.playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.BLOCKS, 1, 1.4f);

        var action = flight.tick(getY(), blocked, ceiling);
        syncPhase();
        switch (action) {
            case LAUNCH -> server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 2, 0.7f);
            case ASCEND -> {
                setDeltaMovement(0, flight.speed(), 0);
                move(MoverType.SELF, getDeltaMovement());
                fallDistance = 0;
                if (flight.flightTicks() % 10 == 0)
                    server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.7f, 0.6f);
            }
            case EXPLODE -> {
                // NONE maps to KEEP in ServerLevel: entity damage/knockback, no terrain destruction or fire.
                server.explode(this, getX(), getY() + 1.625, getZ(), RocketFlight.EXPLOSION_POWER, false, Level.ExplosionInteraction.NONE);
                discard();
                return;
            }
            case FIZZLE -> {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.3, getZ(), 16, 0.3, 0.2, 0.3, 0.04);
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 0.8f);
                discard();
                return;
            }
            case NONE -> { }
        }
        if (flight.phase() == RocketFlight.Phase.FLYING) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.15, getZ(), 5, 0.16, 0.08, 0.16, 0.02);
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 3, 0.2, 0.1, 0.2, 0.02);
        } else if (flight.countdown() % 4 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 2, 0.15, 0.1, 0.15, 0.01);
        }
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isRemoved() || !flight.canRecover() || !(source.getEntity() instanceof Player player)
                || player.isSpectator() || !player.mayBuild()) return false;
        if (!player.getAbilities().instabuild) spawnAtLocation(level, SkyPlanes.ROCKET_KIT.get());
        discard();
        return true;
    }
}
