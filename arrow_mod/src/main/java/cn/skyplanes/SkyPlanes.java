package cn.skyplanes;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

@Mod(SkyPlanes.ID)
public final class SkyPlanes {
    public static final String ID = "skyplanes";
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final Supplier<EntityType<Plane>> PLANE = ENTITIES.registerEntityType(
        "plane", Plane::new, MobCategory.MISC,
        b -> b.sized(2.4f, 1.2f).clientTrackingRange(10).updateInterval(1));
    public static final Supplier<Item> PLANE_KIT = ITEMS.registerItem("plane_kit", PlaneKit::new);
    public static final Supplier<EntityType<Rocket>> ROCKET = ENTITIES.registerEntityType(
        "rocket", Rocket::new, MobCategory.MISC,
        b -> b.sized(1.625f, 3.25f).clientTrackingRange(10).updateInterval(3));
    public static final Supplier<Item> ROCKET_KIT = ITEMS.registerItem("rocket_kit", RocketKit::new);

    public SkyPlanes(IEventBus bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(PLANE_KIT.get());
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(ROCKET_KIT.get());
        });
    }

    public static final class PlaneKit extends Item {
        public PlaneKit(Properties properties) { super(properties.stacksTo(1)); }
        @Override public InteractionResult useOn(UseOnContext context) {
            var level = context.getLevel();
            var pos = context.getClickedPos().relative(context.getClickedFace());
            if (!level.isClientSide()) {
                Plane plane = new Plane(PLANE.get(), level);
                plane.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                plane.setYRot(context.getRotation());
                if (!level.noCollision(plane)) return InteractionResult.FAIL;
                if (level.addFreshEntity(plane) && context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild)
                    context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
