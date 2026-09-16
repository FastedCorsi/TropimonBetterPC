package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler;
import fr.tropimon.betterpc.BetterPcClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** In-place updates must invalidate cached search text and IV sort values too. */
@Mixin(value = PokemonUpdatePacketHandler.class, remap = false)
abstract class PokemonUpdateMixin {
  @Inject(
      method =
          "handle(Lcom/cobblemon/mod/common/net/messages/client/PokemonUpdatePacket;Lnet/minecraft/client/MinecraftClient;)V",
      at = @At("TAIL"))
  private void betterpc$updated(CallbackInfo ci) {
    BetterPcClient.changed();
  }
}
