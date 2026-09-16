package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;
import fr.tropimon.betterpc.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPC.class, remap = false)
abstract class ClientPcMixin {
  @Inject(
      method = {"renameBox", "changeBoxWallpaper"},
      at = @At("TAIL"))
  private void betterpc$metadata(CallbackInfo ci) {
    BetterPcClient.changed();
  }

  @Inject(
      method =
          "set(Lcom/cobblemon/mod/common/api/storage/pc/PCPosition;Lcom/cobblemon/mod/common/pokemon/Pokemon;)V",
      at = @At("HEAD"))
  private void betterpc$observe(PCPosition position, Pokemon pokemon, CallbackInfo ci) {
    ClientPC self = (ClientPC) (Object) this;
    BetterPcClient.arrived(self.get(position), pokemon);
  }
}
