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
     * Push a mirror matrix BEFORE the vanilla rendering, and fix face culling.
     */
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void backhand$preRenderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (BackhandConfigClient.LeftHandedMode) {
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            // Only mirror main hand; offhand renders separately via ItemRendererHooks
            if (!BackhandUtils.isUsingOffhand(player)) {
                GL11.glPushMatrix();
                // Mirror X-axis to move the hand from right side to left side
                GL11.glScalef(-1.0F, 1.0F, 1.0F);
                // glScalef(-1,1,1) inverts face winding (CCW -> CW), so OpenGL
                // would cull front faces and render back faces by default.
                // Tell GL that clockwise is now front to restore correct culling.
                GL11.glFrontFace(GL11.GL_CW);
                backhand$mainHandMirrored = true;
            }
        }
    }

    /**
     * Render the offhand item after the main hand is rendered.
     * In left-handed mode, restore face culling and pop the mirror matrix first.
     */
    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void backhand$renderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (backhand$mainHandMirrored) {
            GL11.glFrontFace(GL11.GL_CCW);
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
