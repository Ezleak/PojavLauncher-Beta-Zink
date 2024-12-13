package top.fifthlight.touchcontroller.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.event.HudRenderCallback;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private static Identifier CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE;

    @Shadow @Final private static Identifier CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;

    @Shadow @Final private static Identifier CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE;

    @Inject(
            method = "renderCrosshair",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
                ordinal = 0
            ),
            cancellable = true
    )
    public void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
        boolean shouldRender = HudRenderCallback.INSTANCE.getCROSSHAIR().invoker().onCrosshairRender(context, tickCounter);
        if (!shouldRender) {
            if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR) {
                float attackCooldownProgress = this.client.player.getAttackCooldownProgress(0.0f);
                boolean renderFullTexture = false;
                if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity && attackCooldownProgress >= 1.0f) {
                    renderFullTexture = this.client.player.getAttackCooldownProgressPerTick() > 5.0f && this.client.targetedEntity.isAlive();
                }
                int x = context.getScaledWindowWidth() / 2 ;
                int y = context.getScaledWindowHeight() / 2;
                if (renderFullTexture) {
                    context.drawGuiTexture(RenderLayer::getCrosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_TEXTURE, x - 8, y - 8, 16, 16);
                } else if (attackCooldownProgress < 1.0f) {
                    int progress = (int)(attackCooldownProgress * 17.0f);
                    context.drawGuiTexture(RenderLayer::getCrosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, x - 8, y - 2, 16, 4);
                    context.drawGuiTexture(RenderLayer::getCrosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 16, 4, 0, 0, x - 8, y - 2, progress, 4);
                }
            }
            callbackInfo.cancel();
        }
    }
}