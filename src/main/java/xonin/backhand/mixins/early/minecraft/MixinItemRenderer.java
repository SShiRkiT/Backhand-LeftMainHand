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
     * Critically, save/restore the active matrix mode so that shader mods which
     * change glMatrixMode during transparent-item rendering don't cause our
     * glPushMatrix/glPopMatrix to operate on the wrong stack.
     */
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void backhand$preRenderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (BackhandConfigClient.LeftHandedMode) {
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            if (!BackhandUtils.isUsingOffhand(player)) {
                // Save current matrix mode in case a shader mod changed it
                // (e.g. to PROJECTION or TEXTURE for translucent passes).
                // 0x0BA0 = GL_MATRIX_MODE
                int savedMatrixMode = GL11.glGetInteger(0x0BA0);
                // Force MODELVIEW so glPushMatrix/glPopMatrix operate on
                // the correct stack regardless of shader interference.
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glScalef(-1.0F, 1.0F, 1.0F);
                GL11.glDisable(GL11.GL_CULL_FACE);
                backhand$mainHandMirrored = true;
                backhand$savedMatrixMode = savedMatrixMode;
            }
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("RETURN"))
    private void backhand$renderItemInFirstPerson(float frame, CallbackInfo ci) {
        if (backhand$mainHandMirrored) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            // Ensure pop happens on MODELVIEW stack
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            // Restore whatever matrix mode was active before our push
            GL11.glMatrixMode(backhand$savedMatrixMode);
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

    @Unique
    private boolean backhand$mainHandMirrored = false;

    @Unique
    private int backhand$savedMatrixMode = GL11.GL_MODELVIEW;
}
