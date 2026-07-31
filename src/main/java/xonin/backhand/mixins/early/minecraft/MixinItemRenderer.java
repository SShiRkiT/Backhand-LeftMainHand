package xonin.backhand.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.hooks.ItemRendererHooks;
import xonin.backhand.utils.BackhandConfigClient;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    /**
     * In left-handed mode, mirror the main hand rendering to appear on the left side.
     * Only applies when rendering to the MAIN framebuffer (FBO 0).
     * Shader mods call this method during offscreen passes (gbuffer, shadow map,
     * translucent) — we must NOT modify GL state in those passes, or the
     * corrupted offscreen buffers will cause GUI flickering and missing items.
     */
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void backhand$preRenderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (BackhandConfigClient.LeftHandedMode) {
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            if (!BackhandUtils.isUsingOffhand(player)) {
                // Query the current framebuffer binding.
                // 0 = default/main framebuffer (final display).
                // Non-zero = offscreen buffer used by shader mods for
                // shadow maps, gbuffer, or compositing passes.
                // We only mirror when rendering to the main display.
                int currentFBO = GL11.glGetInteger(0x8CA6); // GL_FRAMEBUFFER_BINDING
                if (currentFBO != 0) {
                    return; // Offscreen shader pass — leave GL state untouched
                }

                GL11.glPushMatrix();
                // Mirror X-axis to move the hand from right side to left side
                GL11.glScalef(-1.0F, 1.0F, 1.0F);
                // glScalef(-1,1,1) inverts face winding, so disable culling
                GL11.glDisable(GL11.GL_CULL_FACE);
                backhand$mainHandMirrored = true;
            }
        }
    }

    /**
     * Render the offhand item after the main hand is rendered.
     * In left-handed mode, restore culling and pop the mirror matrix first.
     */
    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void backhand$renderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (backhand$mainHandMirrored) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            backhand$mainHandMirrored = false;
        }

        ItemRendererHooks.renderOffhandReturn(frame);
    }

    @ModifyExpressionValue(
        method = "renderItemInFirstPerson",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;isInvisible()Z"))
    private boolean backhand$renderItemInFirstPerson(boolean original) {
        if (BackhandConfigClient.RenderEmptyOffhandAtRest) return original;
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (BackhandUtils.isUsingOffhand(player)) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "renderItemInFirstPerson",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;getItemInUseCount()I"))
    private int backhand$renderItemInFirstPerson(int original) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        if (offhand == null) return original;
        if (BackhandUtils.isUsingOffhand(player)) {
            return ((IBackhandPlayer) player).isOffhandItemInUse() ? original : 0;
        }

        return ((IBackhandPlayer) player).isOffhandItemInUse() ? 0 : original;
    }

    /**
     * Tracks whether we pushed a mirror matrix for the main hand in left-handed mode.
     */
    @Unique
    private boolean backhand$mainHandMirrored = false;
}
