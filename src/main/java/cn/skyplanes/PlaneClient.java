package cn.skyplanes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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
public final class PlaneClient {
    public PlaneClient(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterRenderers e) -> e.registerEntityRenderer(SkyPlanes.PLANE.get(), Renderer::new));
    }
    public static final class State extends EntityRenderState { public float yaw, pitch; }
    public static final class Model extends EntityModel<State> {
        Model(ModelPart root) { super(root); }
        static ModelPart create() {
            MeshDefinition mesh = new MeshDefinition();
            var root = mesh.getRoot();
            root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5, -12, -24, 10, 8, 44), PartPose.ZERO);
            root.addOrReplaceChild("wings", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-36, -7, -8, 72, 2, 12), PartPose.ZERO);
            root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-15, -10, 15, 30, 2, 7).addBox(-1, -21, 13, 2, 13, 8), PartPose.ZERO);
            root.addOrReplaceChild("cockpit", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -17, -6, 8, 5, 10), PartPose.ZERO);
            root.addOrReplaceChild("propeller", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1, -23, -26, 2, 29, 2), PartPose.ZERO);
            root.addOrReplaceChild("wheels", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-10, -4, -8, 3, 4, 5).addBox(7, -4, -8, 3, 4, 5)
                .addBox(-1, -4, 15, 2, 4, 3), PartPose.ZERO);
            return LayerDefinition.create(mesh, 256, 256).bakeRoot();
        }
    }
    public static final class Renderer extends EntityRenderer<Plane, State> {
        private final Model model = new Model(Model.create());
        private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/block/red_concrete.png");
        Renderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 1.2f; }
        @Override public State createRenderState() { return new State(); }
        @Override public void extractRenderState(Plane plane, State state, float partial) {
            super.extractRenderState(plane, state, partial);
            state.yaw = plane.getYRot(partial); state.pitch = plane.getXRot(partial);
        }
        @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180 - state.yaw));
            pose.mulPose(Axis.XP.rotationDegrees(-state.pitch));
            pose.scale(-1, -1, 1);
            collector.submitModel(model, state, pose, model.renderType(TEXTURE), state.lightCoords,
                OverlayTexture.NO_OVERLAY, -1, null, 0, null);
            pose.popPose();
            super.submit(state, pose, collector, camera);
        }
    }
}

