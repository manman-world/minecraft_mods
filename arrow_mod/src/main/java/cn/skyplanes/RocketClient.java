package cn.skyplanes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = SkyPlanes.ID, dist = Dist.CLIENT)
public final class RocketClient {
    public RocketClient(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
            event.registerEntityRenderer(SkyPlanes.ROCKET.get(), Renderer::new));
    }
    public static final class State extends EntityRenderState { public float yaw; }
    private static final class Model extends EntityModel<State> {
        Model() { super(RocketGeometry.create()); }
    }
    public static final class Renderer extends EntityRenderer<Rocket, State> {
        private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(SkyPlanes.ID, "textures/entity/rocket.png");
        private final Model model = new Model();
        Renderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0.8f; }
        @Override public State createRenderState() { return new State(); }
        @Override public void extractRenderState(Rocket rocket, State state, float partialTick) {
            super.extractRenderState(rocket, state, partialTick);
            state.yaw = rocket.getYRot();
        }
        @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180 - state.yaw));
            pose.scale(-1, -1, 1);
            collector.submitModel(model, state, pose, model.renderType(TEXTURE), state.lightCoords,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
            pose.popPose();
            super.submit(state, pose, collector, camera);
        }
    }
}
