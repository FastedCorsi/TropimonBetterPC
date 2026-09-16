package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.net.storage.pc.OpenPCHandler;
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket;
import fr.tropimon.betterpc.*;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OpenPCHandler.class, remap = false)
abstract class OpenPcMixin {
  @Inject(
      method =
          "handle(Lcom/cobblemon/mod/common/net/messages/client/storage/pc/OpenPCPacket;Lnet/minecraft/client/MinecraftClient;)V",
      at = @At("TAIL"))
  private void betterpc$open(OpenPCPacket packet, MinecraftClient client, CallbackInfo ci) {
    if (client.currentScreen instanceof PCGUI pc
        && pc.getPc().getUuid().equals(packet.getStoreID())) BetterPcClient.opened(pc);
  }
}
