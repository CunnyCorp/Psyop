package monster.psyop.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import monster.psyop.client.Psyop;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "setIcon", at = @At("HEAD"), cancellable = true)
    private void setCustomIcon(CallbackInfo ci) {
        try {
            String path = "assets/psyop/icon.png";

            ClassLoader cl = Psyop.class.getClassLoader();

            InputStream is = cl.getResourceAsStream(path);
            if (is == null) {
                Psyop.LOG.error("Custom icon NOT FOUND at: " + path);
                return;
            }

            URL url = cl.getResource(path);
            Psyop.LOG.info("Resolved icon path = " + url);

            NativeImage image = NativeImage.read(is);

            int w = image.getWidth();
            int h = image.getHeight();
            long pixelsPtr = image.getPointer();

            ByteBuffer pixelBuffer = MemoryUtil.memByteBuffer(pixelsPtr, w * h * 4);
            pixelBuffer.position(0);

            GLFWImage.Buffer glfwImages = GLFWImage.malloc(1);
            GLFWImage glfwImage = GLFWImage.malloc();

            glfwImage.set(w, h, pixelBuffer);
            glfwImages.put(0, glfwImage);

            long windowHandle = ((Window) (Object) this).getWindow();

            GLFW.glfwSetWindowIcon(windowHandle, glfwImages);

            glfwImages.free();
            glfwImage.free();
            image.close();

            ci.cancel();
        } catch (Exception e) {
            Psyop.LOG.error("Failed to load custom window icon", e);
        }
    }
}