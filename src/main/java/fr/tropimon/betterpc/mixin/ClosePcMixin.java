package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler;
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket;
import fr.tropimon.betterpc.*;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClosePCHandler.class, remap = false)
abstract class ClosePcMixin {
  @Inject(
      method =
          "handle(Lcom/cobblemon/mod/common/net/messages/client/storage/pc/ClosePCPacket;Lnet/minecraft/client/MinecraftClient;)V",
      at = @At("HEAD"))
  private void betterpc$close(ClosePCPacket packet, MinecraftClient client, CallbackInfo ci) {
    PcTeamBuilder.closeFromServer(packet.getStoreID());
    if (client.currentScreen instanceof BetterPcScreen pc
        && (packet.getStoreID() == null || pc.storeId().equals(packet.getStoreID())))
      pc.closeFromServer();
  }
}
