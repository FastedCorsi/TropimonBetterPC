package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler;
import fr.tropimon.betterpc.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SetPartyReferenceHandler.class, remap = false)
abstract class PartyReadyMixin {
  @Inject(
      method =
          "handle(Lcom/cobblemon/mod/common/net/messages/client/storage/party/SetPartyReferencePacket;Lnet/minecraft/client/MinecraftClient;)V",
      at = @At("TAIL"))
  private void betterpc$ready(CallbackInfo ci) {
    BetterPcClient.ready();
  }
}
