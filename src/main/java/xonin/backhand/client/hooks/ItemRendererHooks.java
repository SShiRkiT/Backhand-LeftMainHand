package xonin.backhand.client.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import thaumcraft.common.items.relics.ItemThaumometer;
import xonin.backhand.api.core.BackhandUtils;
import xonin.backhand.api.core.IBackhandPlayer;
import xonin.backhand.client.utils.BackhandRenderHelper;
import xonin.backhand.utils.BackhandConfig;
import xonin.backhand.utils.BackhandConfigClient;
import xonin.backhand.utils.Mods;

public class ItemRendererHooks {

    /**
     * Extracted outside the mixin to be used in Angelica for Backhand compat.
     * 
     * Rendering logic:
     * - Default mode: Main hand renders normally (right hand), offhand is mirrored (left hand)
     * - Left-handed mode: Main hand is mirrored (left hand), offhand renders normally (right hand)
     */
    public static void renderOffhandReturn(float frame) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (BackhandUtils.isUsingOffhand(player)) return;

        ItemStack renderedMainhandItem = Minecraft.getMinecraft().entityRenderer.itemRenderer.itemToRender;
        ItemStack renderedOffhandItem = BackhandRenderHelper.itemRenderer.itemToRender;
        if (!BackhandConfigClient.RenderEmptyOffhandAtRest && renderedOffhandItem == null) {
            if (!BackhandConfig.EmptyOffhand) {
                return;
            }

            if (((IBackhandPlayer) player).getOffSwingProgress(frame) == 0) {
                return;
            }
        }

        if (usesBothHands(renderedMainhandItem)) {
            return;
        }

        BackhandRenderHelper.firstPersonFrame = frame;

        if (BackhandConfigClient.LeftHandedMode) {
            // LEFT-HANDED MODE:
            // Main hand renders in left hand position (mirrored) - this is handled by vanilla/MixinItemRenderer
            // Offhand renders in right hand position (NOT mirrored)
            renderOffhandRightHanded(player, renderedOffhandItem, frame);
        } else {
            // DEFAULT MODE:
            // Main hand renders in right hand position (not mirrored) - vanilla
            // Offhand renders in left hand position (mirrored)
            renderOffhandLeftHanded(player, renderedOffhandItem, frame);
        }
    }

    /**
     * Default: render the offhand item in the left hand position (mirrored).
     */
    private static void renderOffhandLeftHanded(EntityClientPlayerMP player, ItemStack renderedOffhandItem,
        float frame) {
        if (usesBothHands(renderedOffhandItem)) {
            BackhandUtils
                .useOffhandItem(player, false, () -> BackhandRenderHelper.itemRenderer.renderItemInFirstPerson(frame));
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_FRONT);
            GL11.glPushMatrix();
            GL11.glScalef(-1, 1, 1);
            float f3 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
            float f4 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
            GL11.glRotatef((player.rotationPitch - f3) * -0.1F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((player.rotationYaw - f4) * -0.1F, 0.0F, 1.0F, 0.0F);
            BackhandUtils
                .useOffhandItem(player, false, () -> BackhandRenderHelper.itemRenderer.renderItemInFirstPerson(frame));
            GL11.glPopMatrix();
            GL11.glCullFace(GL11.GL_BACK);
        }
    }

    /**
     * Left-handed mode: render the offhand item in the RIGHT hand position (NOT mirrored).
     */
    private static void renderOffhandRightHanded(EntityClientPlayerMP player, ItemStack renderedOffhandItem,
        float frame) {
        if (usesBothHands(renderedOffhandItem)) {
            BackhandUtils
                .useOffhandItem(player, false, () -> BackhandRenderHelper.itemRenderer.renderItemInFirstPerson(frame));
        } else {
            // No mirroring - render in right hand position naturally
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPushMatrix();
            float f3 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
            float f4 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
            GL11.glRotatef((player.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((player.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);
            BackhandUtils
                .useOffhandItem(player, false, () -> BackhandRenderHelper.itemRenderer.renderItemInFirstPerson(frame));
            GL11.glPopMatrix();
            GL11.glCullFace(GL11.GL_BACK);
        }
    }

    private static boolean usesBothHands(ItemStack item) {
        return item != null && (item.getItem() instanceof ItemMap
            || Mods.THAUMCRAFT.isLoaded() && item.getItem() instanceof ItemThaumometer);
    }
}
