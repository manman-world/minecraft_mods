package cn.skyplanes;

// Generated from blender_3D/arrow.blend by tools/export_rocket.py. Do not hand-edit.
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

final class RocketGeometry {
    static ModelPart create() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(384, 0).addBox(-6.000000f, -38.000000f, -6.000000f, 12.000000f, 28.000000f, 12.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Engine_Neck", CubeListBuilder.create().texOffs(128, 0).addBox(-4.000000f, -10.000000f, -4.000000f, 8.000000f, 4.000000f, 8.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_X_Negative_Inner", CubeListBuilder.create().texOffs(256, 0).addBox(-10.000000f, -18.000000f, -1.000000f, 4.000000f, 14.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_X_Negative_Outer", CubeListBuilder.create().texOffs(256, 0).addBox(-12.000000f, -12.000000f, -1.000000f, 2.000000f, 10.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_X_Positive_Inner", CubeListBuilder.create().texOffs(256, 0).addBox(6.000000f, -18.000000f, -1.000000f, 4.000000f, 14.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_X_Positive_Outer", CubeListBuilder.create().texOffs(256, 0).addBox(10.000000f, -12.000000f, -1.000000f, 2.000000f, 10.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_Y_Negative_Inner", CubeListBuilder.create().texOffs(256, 0).addBox(-1.000000f, -18.000000f, -10.000000f, 2.000000f, 14.000000f, 4.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_Y_Negative_Outer", CubeListBuilder.create().texOffs(256, 0).addBox(-1.000000f, -12.000000f, -12.000000f, 2.000000f, 10.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_Y_Positive_Inner", CubeListBuilder.create().texOffs(256, 0).addBox(-1.000000f, -18.000000f, 6.000000f, 2.000000f, 14.000000f, 4.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Fin_Y_Positive_Outer", CubeListBuilder.create().texOffs(256, 0).addBox(-1.000000f, -12.000000f, 10.000000f, 2.000000f, 10.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Foot_X_Negative", CubeListBuilder.create().texOffs(0, 0).addBox(-13.000000f, -2.000000f, -2.000000f, 6.000000f, 2.000000f, 4.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Foot_X_Positive", CubeListBuilder.create().texOffs(0, 0).addBox(7.000000f, -2.000000f, -2.000000f, 6.000000f, 2.000000f, 4.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Foot_Y_Negative", CubeListBuilder.create().texOffs(0, 0).addBox(-2.000000f, -2.000000f, -13.000000f, 4.000000f, 2.000000f, 6.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Foot_Y_Positive", CubeListBuilder.create().texOffs(0, 0).addBox(-2.000000f, -2.000000f, 7.000000f, 4.000000f, 2.000000f, 6.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Lower_Band", CubeListBuilder.create().texOffs(256, 0).addBox(-6.500000f, -14.000000f, -6.500000f, 13.000000f, 4.000000f, 13.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nose_Base", CubeListBuilder.create().texOffs(256, 0).addBox(-6.000000f, -42.000000f, -6.000000f, 12.000000f, 4.000000f, 12.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nose_Middle", CubeListBuilder.create().texOffs(256, 0).addBox(-4.000000f, -46.000000f, -4.000000f, 8.000000f, 4.000000f, 8.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nose_Tip", CubeListBuilder.create().texOffs(128, 0).addBox(-1.000000f, -52.000000f, -1.000000f, 2.000000f, 2.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nose_Top", CubeListBuilder.create().texOffs(256, 0).addBox(-2.000000f, -50.000000f, -2.000000f, 4.000000f, 4.000000f, 4.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nozzle_Back", CubeListBuilder.create().texOffs(0, 0).addBox(-3.000000f, -6.000000f, 3.000000f, 6.000000f, 4.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nozzle_Front", CubeListBuilder.create().texOffs(0, 0).addBox(-3.000000f, -6.000000f, -5.000000f, 6.000000f, 4.000000f, 2.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nozzle_Left", CubeListBuilder.create().texOffs(0, 0).addBox(-5.000000f, -6.000000f, -5.000000f, 2.000000f, 4.000000f, 10.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Nozzle_Right", CubeListBuilder.create().texOffs(0, 0).addBox(3.000000f, -6.000000f, -5.000000f, 2.000000f, 4.000000f, 10.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Upper_Band", CubeListBuilder.create().texOffs(256, 0).addBox(-6.500000f, -38.000000f, -6.500000f, 13.000000f, 4.000000f, 13.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Window_Frame", CubeListBuilder.create().texOffs(0, 0).addBox(-4.000000f, -32.000000f, -7.000000f, 8.000000f, 8.000000f, 1.000000f), PartPose.ZERO);
        root.addOrReplaceChild("Window_Glass", CubeListBuilder.create().texOffs(512, 0).addBox(-3.000000f, -31.000000f, -7.300000f, 6.000000f, 6.000000f, 0.400000f), PartPose.ZERO);
        root.addOrReplaceChild("Window_Glint", CubeListBuilder.create().texOffs(384, 0).addBox(-2.000000f, -30.500000f, -7.400000f, 1.000000f, 2.000000f, 0.100000f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 1024, 128).bakeRoot();
    }
}
