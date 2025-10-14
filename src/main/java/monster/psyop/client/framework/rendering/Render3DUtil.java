package monster.psyop.client.framework.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import monster.psyop.client.Psyop;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.awt.*;

import static monster.psyop.client.Psyop.MC;

public final class Render3DUtil {
    private Render3DUtil() {
    }


    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose,
                                    float minX, float minY, float minZ,
                                    float maxX, float maxY, float maxZ,
                                    float r, float g, float b, float a) {
        addQuad(vc, pose, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0, 1, 0, r, g, b, a);
        addQuad(vc, pose, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, 0, -1, 0, r, g, b, a);
        addQuad(vc, pose, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, 0, 0, -1, r, g, b, a);
        addQuad(vc, pose, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, 0, 0, 1, r, g, b, a);
        addQuad(vc, pose, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, -1, 0, 0, r, g, b, a);
        addQuad(vc, pose, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 1, 0, 0, r, g, b, a);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose,
                                    float minX, float minY, float minZ,
                                    float maxX, float maxY, float maxZ,
                                    float r, float g, float b, float a) {
        addLine(vc, pose, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addLine(vc, pose, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        addLine(vc, pose, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addLine(vc, pose, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        addLine(vc, pose, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addLine(vc, pose, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    public static void drawTracer(VertexConsumer vc, PoseStack.Pose pose,
                                  float x1, float y1, float z1,
                                  float r, float g, float b, float a) {
        Vec3 look = MC.player.getViewVector(0.0f);
        float y0 = (float) -(look.y * 0.15);
        addLine(vc, pose, 0, y0, 0, x1, y1, z1, r, g, b, a);
    }

    public static void drawBillboardQuadFaces(VertexConsumer vc, PoseStack.Pose pose,
                                              float cx, float cy, float cz,
                                              float rx, float ry, float rz,
                                              float ux, float uy, float uz,
                                              float halfW, float halfH,
                                              float r, float g, float b, float a) {
        float p0x = cx - rx * halfW - ux * halfH;
        float p0y = cy - ry * halfW - uy * halfH;
        float p0z = cz - rz * halfW - uz * halfH;

        float p1x = cx + rx * halfW - ux * halfH;
        float p1y = cy + ry * halfW - uy * halfH;
        float p1z = cz + rz * halfW - uz * halfH;

        float p2x = cx + rx * halfW + ux * halfH;
        float p2y = cy + ry * halfW + uy * halfH;
        float p2z = cz + rz * halfW + uz * halfH;

        float p3x = cx - rx * halfW + ux * halfH;
        float p3y = cy - ry * halfW + uy * halfH;
        float p3z = cz - rz * halfW + uz * halfH;

        float nx = ry * uz - rz * uy;
        float ny = rz * ux - rx * uz;
        float nz = rx * uy - ry * ux;

        addQuad(vc, pose, p0x, p0y, p0z, p1x, p1y, p1z, p2x, p2y, p2z, p3x, p3y, p3z, nx, ny, nz, r, g, b, a);
    }

    public static void drawBillboardQuadEdges(VertexConsumer vc, PoseStack.Pose pose,
                                              float cx, float cy, float cz,
                                              float rx, float ry, float rz,
                                              float ux, float uy, float uz,
                                              float halfW, float halfH,
                                              float r, float g, float b, float a) {
        float p0x = cx - rx * halfW - ux * halfH;
        float p0y = cy - ry * halfW - uy * halfH;
        float p0z = cz - rz * halfW - uz * halfH;

        float p1x = cx + rx * halfW - ux * halfH;
        float p1y = cy + ry * halfW - uy * halfH;
        float p1z = cz + rz * halfW - uz * halfH;

        float p2x = cx + rx * halfW + ux * halfH;
        float p2y = cy + ry * halfW + uy * halfH;
        float p2z = cz + rz * halfW + uz * halfH;

        float p3x = cx - rx * halfW + ux * halfH;
        float p3y = cy - ry * halfW + uy * halfH;
        float p3z = cz - rz * halfW + uz * halfH;

        addLine(vc, pose, p0x, p0y, p0z, p1x, p1y, p1z, r, g, b, a);
        addLine(vc, pose, p1x, p1y, p1z, p2x, p2y, p2z, r, g, b, a);
        addLine(vc, pose, p2x, p2y, p2z, p3x, p3y, p3z, r, g, b, a);
        addLine(vc, pose, p3x, p3y, p3z, p0x, p0y, p0z, r, g, b, a);
    }

    public static void drawBillboardCornerQuads(VertexConsumer vc, PoseStack.Pose pose,
                                                float cx, float cy, float cz,
                                                float rx, float ry, float rz,
                                                float ux, float uy, float uz,
                                                float halfW, float halfH,
                                                float frac, float thick,
                                                float r, float g, float b, float a) {
        float f = Math.max(0.0f, Math.min(0.5f, frac));
        float t = Math.max(0.0005f, thick);
        java.util.function.BiConsumer<float[], float[]> addPlaneQuad = (pMin, pMax) -> {
            float x0 = pMin[0], y0 = pMin[1];
            float x1 = pMax[0], y1 = pMax[1];
            float nx = ry * uz - rz * uy;
            float ny = rz * ux - rx * uz;
            float nz = rx * uy - ry * ux;
            float P0x = cx + rx * x0 + ux * y0;
            float P0y = cy + ry * x0 + uy * y0;
            float P0z = cz + rz * x0 + uz * y0;

            float P1x = cx + rx * x1 + ux * y0;
            float P1y = cy + ry * x1 + uy * y0;
            float P1z = cz + rz * x1 + uz * y0;

            float P2x = cx + rx * x1 + ux * y1;
            float P2y = cy + ry * x1 + uy * y1;
            float P2z = cz + rz * x1 + uz * y1;

            float P3x = cx + rx * x0 + ux * y1;
            float P3y = cy + ry * x0 + uy * y1;
            float P3z = cz + rz * x0 + uz * y1;

            addQuad(vc, pose, P0x, P0y, P0z, P1x, P1y, P1z, P2x, P2y, P2z, P3x, P3y, P3z, nx, ny, nz, r, g, b, a);
        };

        float lenW = halfW * f;
        float lenH = halfH * f;

        addPlaneQuad.accept(new float[]{halfW - lenW, halfH - t}, new float[]{halfW, halfH});
        addPlaneQuad.accept(new float[]{halfW - t, halfH - lenH}, new float[]{halfW, halfH});

        addPlaneQuad.accept(new float[]{-halfW, halfH - t}, new float[]{-halfW + lenW, halfH});
        addPlaneQuad.accept(new float[]{-halfW, halfH - lenH}, new float[]{-halfW + t, halfH});

        addPlaneQuad.accept(new float[]{halfW - lenW, -halfH}, new float[]{halfW, -halfH + t});
        addPlaneQuad.accept(new float[]{halfW - t, -halfH}, new float[]{halfW, -halfH + lenH});

        addPlaneQuad.accept(new float[]{-halfW, -halfH}, new float[]{-halfW + lenW, -halfH + t});
        addPlaneQuad.accept(new float[]{-halfW, -halfH}, new float[]{-halfW + t, -halfH + lenH});
    }

    public static void drawCornerBox(VertexConsumer vc, PoseStack.Pose pose,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     float fraction,
                                     float r, float g, float b, float a) {
        float fx = Math.max(0f, Math.min(0.5f, fraction));
        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        float lx = dx * fx, ly = dy * fx, lz = dz * fx;

        addLine(vc, pose, minX, minY, minZ, minX + lx, minY, minZ, r, g, b, a);
        addLine(vc, pose, minX, minY, minZ, minX, minY, minZ + lz, r, g, b, a);
        addLine(vc, pose, maxX, minY, minZ, maxX - lx, minY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, minZ, maxX, minY, minZ + lz, r, g, b, a);
        addLine(vc, pose, maxX, minY, maxZ, maxX - lx, minY, maxZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, maxZ, maxX, minY, maxZ - lz, r, g, b, a);
        addLine(vc, pose, minX, minY, maxZ, minX + lx, minY, maxZ, r, g, b, a);
        addLine(vc, pose, minX, minY, maxZ, minX, minY, maxZ - lz, r, g, b, a);

        addLine(vc, pose, minX, maxY, minZ, minX + lx, maxY, minZ, r, g, b, a);
        addLine(vc, pose, minX, maxY, minZ, minX, maxY, minZ + lz, r, g, b, a);

        addLine(vc, pose, maxX, maxY, minZ, maxX - lx, maxY, minZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, minZ, maxX, maxY, minZ + lz, r, g, b, a);

        addLine(vc, pose, maxX, maxY, maxZ, maxX - lx, maxY, maxZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, maxZ, maxX, maxY, maxZ - lz, r, g, b, a);

        addLine(vc, pose, minX, maxY, maxZ, minX + lx, maxY, maxZ, r, g, b, a);
        addLine(vc, pose, minX, maxY, maxZ, minX, maxY, maxZ - lz, r, g, b, a);

        addLine(vc, pose, minX, minY, minZ, minX, minY + ly, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, minZ, maxX, minY + ly, minZ, r, g, b, a);
        addLine(vc, pose, maxX, minY, maxZ, maxX, minY + ly, maxZ, r, g, b, a);
        addLine(vc, pose, minX, minY, maxZ, minX, minY + ly, maxZ, r, g, b, a);

        addLine(vc, pose, minX, maxY, minZ, minX, maxY - ly, minZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, minZ, maxX, maxY - ly, minZ, r, g, b, a);
        addLine(vc, pose, maxX, maxY, maxZ, maxX, maxY - ly, maxZ, r, g, b, a);
        addLine(vc, pose, minX, maxY, maxZ, minX, maxY - ly, maxZ, r, g, b, a);
    }

    public static float distanceFadeAlpha(float baseAlpha, Vec3 camPos, Vec3 targetPos,
                                          double near, double far) {
        if (far <= near) return baseAlpha;
        double d = camPos.distanceTo(targetPos);
        if (d <= near) return baseAlpha;
        if (d >= far) return 0f;
        double t = 1.0 - (d - near) / (far - near);
        return (float) (baseAlpha * t);
    }

    public static float[] healthToColor(float health, float maxHealth, float alpha) {
        float pct = maxHealth <= 0f ? 0f : Math.max(0f, Math.min(1f, health / maxHealth));
        float hue = pct * (120f / 360f);
        float[] rgb = hsbToRgb(hue, 0.85f, 1.0f);
        return new float[]{rgb[0], rgb[1], rgb[2], alpha};
    }

    public static float[] hsbToRgb(float h, float s, float v) {
        int rgb = Color.HSBtoRGB(h, s, v);
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return new float[]{r, g, b};
    }

    public static void addQuad(VertexConsumer vc, PoseStack.Pose pose,
                               float x0, float y0, float z0,
                               float x1, float y1, float z1,
                               float x2, float y2, float z2,
                               float x3, float y3, float z3,
                               float nx, float ny, float nz,
                               float r, float g, float b, float a) {
        addQuadVarying(vc, pose, x0, y0, z0, r, g, b, a, x1, y1, z1, r, g, b, a, x2, y2, z2, r, g, b, a, x3, y3, z3, r, g, b, a, nx, ny, nz);
    }

    public static void addLine(VertexConsumer vc, PoseStack.Pose pose,
                               float x0, float y0, float z0,
                               float x1, float y1, float z1,
                               float r, float g, float b, float a) {
        vc.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
    }

    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose, AABB bbRelCam, float r, float g, float b, float a) {
        drawBoxFaces(vc, pose,
                (float) bbRelCam.minX, (float) bbRelCam.minY, (float) bbRelCam.minZ,
                (float) bbRelCam.maxX, (float) bbRelCam.maxY, (float) bbRelCam.maxZ,
                r, g, b, a);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose, AABB bbRelCam, float r, float g, float b, float a) {
        drawBoxEdges(vc, pose,
                (float) bbRelCam.minX, (float) bbRelCam.minY, (float) bbRelCam.minZ,
                (float) bbRelCam.maxX, (float) bbRelCam.maxY, (float) bbRelCam.maxZ,
                r, g, b, a);
    }

    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose, AABB bbWorld, Vec3 camPos,
                                    float r, float g, float b, float a) {
        AABB rel = bbWorld.move(-camPos.x, -camPos.y, -camPos.z);
        drawBoxFaces(vc, pose, rel, r, g, b, a);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose, AABB bbWorld, Vec3 camPos,
                                    float r, float g, float b, float a) {
        AABB rel = bbWorld.move(-camPos.x, -camPos.y, -camPos.z);
        drawBoxEdges(vc, pose, rel, r, g, b, a);
    }

    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos,
                                    float r, float g, float b, float a) {
        AABB bb = new AABB(pos);
        drawBoxFaces(vc, pose, bb, camPos, r, g, b, a);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos,
                                    float r, float g, float b, float a) {
        AABB bb = new AABB(pos);
        drawBoxEdges(vc, pose, bb, camPos, r, g, b, a);
    }

    public static void drawBlockBoxFaces(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float inflate,
                                         float r, float g, float b, float a) {
        AABB bb = new AABB(pos).inflate(inflate);
        drawBoxFaces(vc, pose, bb, camPos, r, g, b, a);
    }

    public static void drawBlockBoxEdges(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float inflate,
                                         float r, float g, float b, float a) {
        AABB bb = new AABB(pos).inflate(inflate);
        drawBoxEdges(vc, pose, bb, camPos, r, g, b, a);
    }

    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose, AABB bbWorld, Vec3 camPos, float[] rgba) {
        drawBoxFaces(vc, pose, bbWorld, camPos, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose, AABB bbWorld, Vec3 camPos, float[] rgba) {
        drawBoxEdges(vc, pose, bbWorld, camPos, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void drawBoxFaces(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float[] rgba) {
        drawBoxFaces(vc, pose, pos, camPos, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void drawBoxEdges(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float[] rgba) {
        drawBoxEdges(vc, pose, pos, camPos, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void drawBlockBoxFaces(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float inflate, float[] rgba) {
        drawBlockBoxFaces(vc, pose, pos, camPos, inflate, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void drawBlockBoxEdges(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, Vec3 camPos, float inflate, float[] rgba) {
        drawBlockBoxEdges(vc, pose, pos, camPos, inflate, rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void addQuadVarying(VertexConsumer vc, PoseStack.Pose pose,
                                      float x0, float y0, float z0, float r0, float g0, float b0, float a0,
                                      float x1, float y1, float z1, float r1, float g1, float b1, float a1,
                                      float x2, float y2, float z2, float r2, float g2, float b2, float a2,
                                      float x3, float y3, float z3, float r3, float g3, float b3, float a3,
                                      float nx, float ny, float nz) {
        vc.addVertex(pose, x0, y0, z0).setColor(r0, g0, b0, a0).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x1, y1, z1).setColor(r1, g1, b1, a1).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x2, y2, z2).setColor(r2, g2, b2, a2).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x3, y3, z3).setColor(r3, g3, b3, a3).setNormal(pose, nx, ny, nz);
    }

    public static void addLine(VertexConsumer vc, PoseStack.Pose pose, Vec3 p0, Vec3 p1, float r, float g, float b, float a) {
        vc.addVertex(pose, (float) p0.x, (float) p0.y, (float) p0.z).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
        vc.addVertex(pose, (float) p1.x, (float) p1.y, (float) p1.z).setColor(r, g, b, a).setNormal(pose, 0, 0, 1);
    }

    public static void addLineRel(VertexConsumer vc, PoseStack.Pose pose, Vec3 p0, Vec3 p1, float r, float g, float b, float a) {
        addLine(vc, pose, getCameraRelPos(p0), getCameraRelPos(p1), r, g, b, a);
    }

    public static void drawCircleEdges(VertexConsumer vc, PoseStack.Pose pose,
                                       float cx, float cy, float cz,
                                       float axX, float axY, float axZ,
                                       float bxX, float bxY, float bxZ,
                                       float radius, int segments,
                                       float r, float g, float b, float a) {
        int minAdaptive = Math.max(24, (int) Math.floor(Math.abs(radius) * 12.0));
        int seg = Math.max(minAdaptive, segments);
        float angleStep = (float) (2 * Math.PI / seg);
        float cos = (float) Math.cos(angleStep);
        float sin = (float) Math.sin(angleStep);

        float ux = radius;
        float uy = 0f;

        float prevX = cx + ux * axX + uy * bxX;
        float prevY = cy + ux * axY + uy * bxY;
        float prevZ = cz + ux * axZ + uy * bxZ;

        for (int i = 1; i <= seg; i++) {
            float nx = ux * cos - uy * sin;
            float ny = ux * sin + uy * cos;
            ux = nx;
            uy = ny;

            float x = cx + ux * axX + uy * bxX;
            float y = cy + ux * axY + uy * bxY;
            float z = cz + ux * axZ + uy * bxZ;
            addLine(vc, pose, prevX, prevY, prevZ, x, y, z, r, g, b, a);
            prevX = x;
            prevY = y;
            prevZ = z;
        }
    }

    public static void drawCircleEdgesXY(VertexConsumer vc, PoseStack.Pose pose,
                                         float cx, float cy, float cz,
                                         float radius, int segments,
                                         float r, float g, float b, float a) {
        drawCircleEdges(vc, pose, cx, cy, cz, 1, 0, 0, 0, 1, 0, radius, segments, r, g, b, a);
    }

    public static void drawCircleEdgesXZ(VertexConsumer vc, PoseStack.Pose pose,
                                         float cx, float cy, float cz,
                                         float radius, int segments,
                                         float r, float g, float b, float a) {
        drawCircleEdges(vc, pose, cx, cy, cz, 1, 0, 0, 0, 0, 1, radius, segments, r, g, b, a);
    }

    public static void drawCircleEdgesYZ(VertexConsumer vc, PoseStack.Pose pose,
                                         float cx, float cy, float cz,
                                         float radius, int segments,
                                         float r, float g, float b, float a) {
        drawCircleEdges(vc, pose, cx, cy, cz, 0, 1, 0, 0, 0, 1, radius, segments, r, g, b, a);
    }

    public static void drawSphere(VertexConsumer vc, PoseStack.Pose pose,
                                  float cx, float cy, float cz,
                                  float radius, int segments,
                                  float r, float g, float b, float a) {
        drawCircleEdgesXY(vc, pose, cx, cy, cz, radius, segments, r, g, b, a);
        drawCircleEdgesXZ(vc, pose, cx, cy, cz, radius, segments, r, g, b, a);
        drawCircleEdgesYZ(vc, pose, cx, cy, cz, radius, segments, r, g, b, a);
    }

    public static void drawSphereFaces(VertexConsumer vc, PoseStack.Pose pose,
                                       float cx, float cy, float cz,
                                       float radius, int segments,
                                       float r, float g, float b, float a) {
        int slices = Math.max(16, segments);
        int stacks = Math.max(8, segments / 2);
        drawSphereFaces(vc, pose, cx, cy, cz, radius, stacks, slices, r, g, b, a);
    }

    public static void drawSphereFaces(VertexConsumer vc, PoseStack.Pose pose,
                                       float cx, float cy, float cz,
                                       float radius, int stacks, int slices,
                                       float r, float g, float b, float a) {
        int st = Math.max(3, stacks);
        int sl = Math.max(8, slices);

        for (int i = 0; i < st; i++) {
            float v0 = i / (float) st;
            float v1 = (i + 1) / (float) st;
            float theta0 = v0 * (float) Math.PI;
            float theta1 = v1 * (float) Math.PI;
            float cosT0 = (float) Math.cos(theta0);
            float sinT0 = (float) Math.sin(theta0);
            float cosT1 = (float) Math.cos(theta1);
            float sinT1 = (float) Math.sin(theta1);

            for (int j = 0; j < sl; j++) {
                float u0 = j / (float) sl;
                float u1 = (j + 1) / (float) sl;
                float phi0 = u0 * (float) (2 * Math.PI);
                float phi1 = u1 * (float) (2 * Math.PI);
                float cosP0 = (float) Math.cos(phi0);
                float sinP0 = (float) Math.sin(phi0);
                float cosP1 = (float) Math.cos(phi1);
                float sinP1 = (float) Math.sin(phi1);

                float x00 = cx + radius * sinT0 * cosP0;
                float y00 = cy + radius * cosT0;
                float z00 = cz + radius * sinT0 * sinP0;

                float x01 = cx + radius * sinT0 * cosP1;
                float y01 = cy + radius * cosT0;
                float z01 = cz + radius * sinT0 * sinP1;

                float x11 = cx + radius * sinT1 * cosP1;
                float y11 = cy + radius * cosT1;
                float z11 = cz + radius * sinT1 * sinP1;

                float x10 = cx + radius * sinT1 * cosP0;
                float y10 = cy + radius * cosT1;
                float z10 = cz + radius * sinT1 * sinP0;

                float mnx = (x00 - cx) + (x01 - cx) + (x11 - cx) + (x10 - cx);
                float mny = (y00 - cy) + (y01 - cy) + (y11 - cy) + (y10 - cy);
                float mnz = (z00 - cz) + (z01 - cz) + (z11 - cz) + (z10 - cz);
                float len = (float) Math.sqrt(mnx * mnx + mny * mny + mnz * mnz);
                if (len > 1e-6f) {
                    mnx /= len;
                    mny /= len;
                    mnz /= len;
                } else {
                    mnx = 0f;
                    mny = 1f;
                    mnz = 0f;
                }

                addQuad(vc, pose, x00, y00, z00,
                        x01, y01, z01,
                        x11, y11, z11,
                        x10, y10, z10,
                        mnx, mny, mnz,
                        r, g, b, a);
            }
        }
    }

    public static void drawHeartEdgesXY(VertexConsumer vc, PoseStack.Pose pose,
                                        float cx, float cy, float cz,
                                        float size, int segments,
                                        float r, float g, float b, float a) {
        int seg = Math.max(16, segments);
        float scale = size / 16f;

        float t0 = 0f;
        float x0 = (float) (Math.pow(Math.sin(t0), 3) * 16.0) * scale + cx;
        float y0 = (float) ((13.0 * Math.cos(t0)) - (5.0 * Math.cos(2.0 * t0)) - (2.0 * Math.cos(3.0 * t0)) - Math.cos(4.0 * t0)) * scale + cy;
        float z0 = cz;

        pose.rotate(new Quaternionf().rotateXYZ(Psyop.RANDOM.nextFloat() * 360, 0, Psyop.RANDOM.nextFloat() * 360));

        for (int i = 1; i <= seg; i++) {
            float t = (float) (i * (2 * Math.PI / seg));
            float x = (float) (Math.pow(Math.sin(t), 3) * 16.0) * scale + cx;
            float y = (float) ((13.0 * Math.cos(t)) - (5.0 * Math.cos(2.0 * t)) - (2.0 * Math.cos(3.0 * t)) - Math.cos(4.0 * t)) * scale + cy;
            addLine(vc, pose, x0, y0, z0, x, y, cz, r, g, b, a);
            x0 = x;
            y0 = y;
            z0 = cz;
        }
    }

    public static void drawAxis(VertexConsumer vc, PoseStack.Pose pose,
                                float cx, float cy, float cz,
                                float length,
                                float r, float g, float b, float a) {
        addLine(vc, pose, cx - length, cy, cz, cx + length, cy, cz, r, g, b, a);
        addLine(vc, pose, cx, cy - length, cz, cx, cy + length, cz, r, g, b, a);
        addLine(vc, pose, cx, cy, cz - length, cx, cy, cz + length, r, g, b, a);
    }

    public static void drawAxisRel(VertexConsumer vc, PoseStack.Pose pose,
                                   Vec3 pos, float length,
                                   float r, float g, float b, float a) {
        Vec3 camRel = getCameraRelPos(pos);
        drawAxis(vc, pose, (float) camRel.x, (float) camRel.y, (float) camRel.z, length, r, g, b, a);
    }

    public static void drawCross(VertexConsumer vc, PoseStack.Pose pose,
                                 float cx, float cy, float cz,
                                 float size,
                                 float r, float g, float b, float a) {
        drawAxis(vc, pose, cx, cy, cz, size, r, g, b, a);
    }

    public static void drawCrossRel(VertexConsumer vc, PoseStack.Pose pose,
                                    Vec3 pos, float size,
                                    float r, float g, float b, float a) {
        Vec3 camRel = getCameraRelPos(pos);
        drawCross(vc, pose, (float) camRel.x, (float) camRel.y, (float) camRel.z, size, r, g, b, a);
    }

    public static Vec3 getCameraRelPos(Vec3 vec3) {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(vec3);
    }
}
