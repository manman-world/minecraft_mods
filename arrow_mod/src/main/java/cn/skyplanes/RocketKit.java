package cn.skyplanes;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Places an unlit rocket on a solid floor, with its full bounds unobstructed. */
public final class RocketKit extends Item {
    public RocketKit(Properties properties) { super(properties.stacksTo(1)); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var floor = context.getClickedPos();
        var pos = floor.above();
        var player = context.getPlayer();
        if (context.getClickedFace() != Direction.UP
                || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                || player == null || !player.mayUseItemAt(pos, Direction.UP, context.getItemInHand()))
            return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Rocket rocket = new Rocket(SkyPlanes.ROCKET.get(), level);
        rocket.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rocket.setYRot(context.getRotation());
        var bounds = rocket.getBoundingBox();
        if (bounds.minY < level.getMinY() || bounds.maxY > level.getMaxY() + 1.0
                || !level.getWorldBorder().isWithinBounds(bounds)
                || !level.noCollision(rocket)
                || !level.getEntities(rocket, bounds, entity -> !entity.isSpectator()).isEmpty())
            return InteractionResult.FAIL;
        if (!level.addFreshEntity(rocket)) return InteractionResult.FAIL;
        if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
