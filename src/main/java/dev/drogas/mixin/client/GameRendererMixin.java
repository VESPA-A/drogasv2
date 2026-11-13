package dev.drogas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.drogas.client.ClientEffects;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Altera o FOV conforme as substancias ativas (percepcao de distancia). Sem shaders e sem tocar no pipeline
 * de renderizacao, portanto compativel com Sodium/Iris. Se a assinatura de getFov mudar, o injetor e ignorado.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyReturnValue(method = "getFov", at = @At("RETURN"), require = 0)
    private float drogas$modifyFov(float original) {
        float d = ClientEffects.fovDelta();
        return d == 0f ? original : original + d;
    }
}
