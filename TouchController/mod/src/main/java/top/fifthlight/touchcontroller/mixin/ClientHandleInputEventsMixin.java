package top.fifthlight.touchcontroller.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.event.ClientHandleInputEvents;

@Mixin(MinecraftClient.class)
public abstract class ClientHandleInputEventsMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Shadow
    private int itemUseCooldown;

    @Shadow
    @Nullable
    public Screen currentScreen;

    @Shadow
    protected abstract void handleBlockBreaking(boolean breaking);

    @Shadow
    @Final
    public Mouse mouse;

    @Shadow
    protected abstract boolean doAttack();

    @Shadow
    protected abstract void doItemPick();

    @Shadow
    protected abstract void doItemUse();

    @Unique
    private boolean attacked = false;
    @Unique
    private boolean itemUsed = false;
    @Unique
    private boolean hitEmptyBlockState = false;

    @SuppressWarnings("StatementWithEmptyBody")
    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
                    shift = At.Shift.NONE
            ),
            cancellable = true
    )
    private void handleInputEvents(CallbackInfo ci) {
        ci.cancel();

        hitEmptyBlockState = false;
        attacked = this.options.attackKey.isPressed() && mouse.isCursorLocked();
        itemUsed = options.useKey.isPressed();

        var context = new ClientHandleInputEvents.InputContext() {
            @Override
            public void hitEmptyBlockState() {
                hitEmptyBlockState = true;
            }

            @Override
            public void doItemUse() {
                if (!player.isUsingItem()) {
                    ClientHandleInputEventsMixin.this.doItemUse();
                }
                itemUsed = true;
            }

            @Override
            public void doItemPick() {
                ClientHandleInputEventsMixin.this.doItemPick();
            }

            @Override
            public boolean doAttack() {
                attacked = true;
                return ClientHandleInputEventsMixin.this.doAttack();
            }
        };
        ClientHandleInputEvents.INSTANCE.getHANDLE_INPUT().invoker().onHandleInput((MinecraftClient) (Object) this, context);

        if (player.isUsingItem()) {
            if (!itemUsed) {
                interactionManager.stopUsingItem(this.player);
            }
            while (options.attackKey.wasPressed()) {
            }
            while (options.useKey.wasPressed()) {
            }
            while (options.pickItemKey.wasPressed()) {
            }
        } else {
            while (options.attackKey.wasPressed()) {
                hitEmptyBlockState |= doAttack();
            }
            while (options.useKey.wasPressed()) {
                doItemUse();
            }
            while (options.pickItemKey.wasPressed()) {
                doItemPick();
            }
        }

        if (itemUsed && itemUseCooldown == 0 && !this.player.isUsingItem()) {
            doItemUse();
        }
        handleBlockBreaking(currentScreen == null && !hitEmptyBlockState && attacked);
    }
}
