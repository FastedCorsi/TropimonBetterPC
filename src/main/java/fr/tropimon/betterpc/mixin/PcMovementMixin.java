package fr.tropimon.betterpc.mixin;

import fr.tropimon.betterpc.BetterPcScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Block local movement even when an inventory-movement mod allows input with a screen open. */
@Mixin(ClientPlayerEntity.class)
public abstract class PcMovementMixin {
  @Inject(
      method = "tickMovement",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/input/Input;tick(ZF)V",
              shift = At.Shift.AFTER))
  private void betterpc$stopMovement(CallbackInfo ci) {
    if (!(MinecraftClient.getInstance().currentScreen instanceof BetterPcScreen)
        && !fr.tropimon.betterpc.PcTeamBuilder.blocksMovement()) return;
    ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
    var input = player.input;
    input.movementForward = input.movementSideways = 0;
    input.pressingForward = input.pressingBack = input.pressingLeft = input.pressingRight = false;
    input.jumping = input.sneaking = false;
    player.setSprinting(false);
    var velocity = player.getVelocity();
    player.setVelocity(0, player.getAbilities().flying ? 0 : velocity.y, 0);
  }
}
