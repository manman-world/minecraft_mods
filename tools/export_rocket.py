"""Run with Blender --background --factory-startup --disable-autoexec arrow.blend --python this_file.

Read the source without saving it. Export its axis-aligned cuboids and flat
material colors into the existing Minecraft entity renderer format.
"""
import bpy
import hashlib
import json
from pathlib import Path
import struct
import zlib
from mathutils import Vector

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/skyplanes"
JAVA = ROOT / "src/main/java/cn/skyplanes/RocketGeometry.java"
meshes = sorted((o for o in bpy.context.scene.objects if o.type == "MESH"), key=lambda o: o.name)
assert len(meshes) == 27, "Unexpected source: expected 27 rocket cuboids"
materials = sorted({o.active_material.name for o in meshes})
assert len(materials) == 5
TILE = 128
WIDTH, HEIGHT = 1024, 128


def png(path, width, height, pixels):
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))
    raw = b"".join(b"\0" + bytes(pixels[y * width * 4:(y + 1) * width * 4]) for y in range(height))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
                     + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


def srgb(value):
    return round(255 * (12.92 * value if value <= 0.0031308 else 1.055 * value ** (1 / 2.4) - 0.055))


colors = {}
for name in materials:
    material = bpy.data.materials[name]
    shader = next(n for n in material.node_tree.nodes if n.type == "BSDF_PRINCIPLED")
    rgba = shader.inputs["Base Color"].default_value
    colors[name] = tuple(srgb(c) for c in rgba[:3]) + (255,)
pixels = bytearray()
for y in range(HEIGHT):
    for x in range(WIDTH):
        pixels.extend(colors[materials[min(x // TILE, len(materials) - 1)]])
png(ASSETS / "textures/entity/rocket.png", WIDTH, HEIGHT, pixels)

parts, lines = [], []
for obj in meshes:
    assert len(obj.data.vertices) == 8 and len(obj.data.polygons) == 6 and not obj.modifiers
    vertices = [obj.matrix_world @ v.co for v in obj.data.vertices]
    lo = [min(v[i] for v in vertices) for i in range(3)]
    hi = [max(v[i] for v in vertices) for i in range(3)]
    assert all(all(min(abs(v[i] - lo[i]), abs(v[i] - hi[i])) < 1e-5 for i in range(3)) for v in vertices), "Rotated mesh is unsupported"
    # Blender Z-up -> Minecraft model Y-down; 16 model pixels = one block.
    origin = [lo[0] * 16, -hi[2] * 16, lo[1] * 16]
    size = [(hi[0] - lo[0]) * 16, (hi[2] - lo[2]) * 16, (hi[1] - lo[1]) * 16]
    assert 2 * (size[0] + size[2]) < TILE and size[1] + size[2] < HEIGHT
    u = materials.index(obj.active_material.name) * TILE
    values = ", ".join(f"{v:.6f}f" for v in origin + size)
    lines.append(f'        root.addOrReplaceChild("{obj.name}", CubeListBuilder.create().texOffs({u}, 0).addBox({values}), PartPose.ZERO);')
    parts.append({"name": obj.name, "origin": origin, "size": size, "material": obj.active_material.name, "uv": [u, 0]})

JAVA.write_text('''package cn.skyplanes;

// Generated from blender_3D/arrow.blend by tools/export_rocket.py. Do not hand-edit.
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

final class RocketGeometry {
    static ModelPart create() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
''' + "\n".join(lines) + f'''
        return LayerDefinition.create(mesh, {WIDTH}, {HEIGHT}).bakeRoot();
    }}
}}
''', encoding="utf-8")

# A small original pixel-art inventory silhouette, using the same material palette.
icon = bytearray(32 * 32 * 4)
def rect(x0, y0, x1, y1, material):
    color = colors[next(n for n in materials if material in n)]
    for y in range(y0, y1):
        for x in range(x0, x1):
            icon[(y * 32 + x) * 4:(y * 32 + x + 1) * 4] = bytes(color)
rect(15, 1, 17, 3, "Gray")
rect(14, 3, 18, 5, "Red")
rect(12, 5, 20, 7, "Red")
rect(10, 7, 22, 10, "Red")
rect(11, 10, 21, 23, "White")
rect(10, 22, 22, 25, "Red")
rect(12, 12, 20, 20, "Dark")
rect(13, 13, 19, 19, "Window")
rect(14, 14, 15, 16, "White")
rect(8, 21, 11, 28, "Red")
rect(6, 25, 8, 29, "Red")
rect(21, 21, 24, 28, "Red")
rect(24, 25, 26, 29, "Red")
rect(5, 29, 11, 31, "Dark")
rect(21, 29, 27, 31, "Dark")
rect(13, 25, 19, 27, "Gray")
rect(12, 27, 20, 29, "Dark")
png(ASSETS / "textures/item/rocket_kit.png", 32, 32, icon)
report = {"source": "blender_3D/arrow.blend", "sha256": hashlib.sha256(Path(bpy.data.filepath).read_bytes()).hexdigest(),
          "texture_size": [WIDTH, HEIGHT], "colors_srgb": colors, "parts": parts}
(ROOT / "blender_3D/rocket-export.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(f"Exported {len(parts)} cuboids, {len(materials)} colors, entity texture and inventory icon.")
