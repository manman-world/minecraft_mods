package cn.skyplanes;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import javax.imageio.ImageIO;

/** Bake the actual Minecraft model and verify geometry and every face's texture mapping. */
public final class RocketModelCheck {
    public static void main(String[] args) throws Exception {
        var textureUrl = RocketModelCheck.class.getResource("/assets/skyplanes/textures/entity/rocket.png");
        require(textureUrl != null, "Entity texture missing from runtime resources");
        var texture = ImageIO.read(textureUrl);
        require(texture.getWidth() == 1024 && texture.getHeight() == 128, "Wrong atlas size");
        var colors = new HashSet<Integer>();
        int[] counts = {0, 0};
        double[] bounds = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                           Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        RocketGeometry.create().visit(new PoseStack(), (pose, path, index, cube) -> {
            counts[0]++;
            bounds[0] = Math.min(bounds[0], cube.minX);
            bounds[1] = Math.min(bounds[1], cube.minY);
            bounds[2] = Math.min(bounds[2], cube.minZ);
            bounds[3] = Math.max(bounds[3], cube.maxX);
            bounds[4] = Math.max(bounds[4], cube.maxY);
            bounds[5] = Math.max(bounds[5], cube.maxZ);
            Integer partColor = null;
            for (var face : cube.polygons) {
                counts[1]++;
                double u = 0, v = 0;
                for (var vertex : face.vertices()) {
                    require(vertex.u() >= 0 && vertex.u() < 1 && vertex.v() >= 0 && vertex.v() < 1, "UV outside texture: " + path);
                    u += vertex.u() / 4;
                    v += vertex.v() / 4;
                }
                int color = texture.getRGB((int)(u * texture.getWidth()), (int)(v * texture.getHeight()));
                require((color >>> 24) == 255, "Transparent face: " + path);
                require(partColor == null || partColor == color, "Material color crosses atlas tiles: " + path);
                partColor = color;
                colors.add(color);
            }
        });
        require(counts[0] == 27 && counts[1] == 162, "Export lost cuboids or faces");
        double[] expected = {-13, -52, -13, 13, 0, 13};
        for (int i = 0; i < expected.length; i++) require(Math.abs(bounds[i] - expected[i]) < 0.001, "Incorrect axis conversion or scale");
        require(colors.size() == 5, "Export lost material colors");
        var icon = ImageIO.read(RocketModelCheck.class.getResource("/assets/skyplanes/textures/item/rocket_kit.png"));
        require(icon.getWidth() == 32 && icon.getHeight() == 32, "Inventory icon invalid");
        System.out.println("PASS: Minecraft model bake, 27 cuboids / 162 faces, bounds, five atlas colors, UVs and item icon");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
