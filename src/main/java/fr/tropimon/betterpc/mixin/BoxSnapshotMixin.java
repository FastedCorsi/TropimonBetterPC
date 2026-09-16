package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.net.storage.pc.SetPCBoxHandler;
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPacket;
import fr.tropimon.betterpc.*;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SetPCBoxHandler.class, remap = false)
abstract class BoxSnapshotMixin {
  @Inject(
      method =
          "handle(Lcom/cobblemon/mod/common/net/messages/client/storage/pc/SetPCBoxPacket;Lnet/minecraft/client/MinecraftClient;)V",
      at = @At("TAIL"))
  private void betterpc$baseline(SetPCBoxPacket packet, MinecraftClient client, CallbackInfo ci) {
    var pc = CobblemonClient.INSTANCE.getStorage().getPcStores().get(packet.getStoreID());
    if (pc != null && packet.getBoxNumber() >= 0 && packet.getBoxNumber() < pc.getBoxes().size())
      for (var pokemon : pc.getBoxes().get(packet.getBoxNumber()).getSlots())
        BetterPcClient.remember(pokemon);
    BetterPcClient.changed();
  }
}
