package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import fr.tropimon.betterpc.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientStorageManager.class, remap = false)
abstract class StorageManagerMixin {
  @Inject(
      method = {"onLogin", "onLogout"},
      at = @At("HEAD"))
  private void betterpc$reset(CallbackInfo ci) {
    BetterPcClient.reset();
  }

  @Inject(
      method = {"setParty", "createParty", "setPartyStore"},
      at = @At("TAIL"))
  private void betterpc$changed(CallbackInfo ci) {
    BetterPcClient.changed();
  }
}
