package monster.psyop.client.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
    @Accessor("user")
    void setUser(User user);

    @Accessor("userApiService")
    void setUserApiService(UserApiService userApiService);

    @Accessor("minecraftSessionService")
    void setMinecraftSessionService(MinecraftSessionService minecraftSessionService);

    @Accessor("authenticationService")
    void setAuthenticationService(YggdrasilAuthenticationService authenticationService);
}
