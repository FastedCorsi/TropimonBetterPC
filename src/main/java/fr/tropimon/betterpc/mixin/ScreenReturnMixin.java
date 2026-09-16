package fr.tropimon.betterpc.mixin;

import fr.tropimon.betterpc.PcTeamBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
abstract class ScreenReturnMixin {
  @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
  private Screen betterpc$return(Screen next) {
    return PcTeamBuilder.returnTarget(next);
  }
}
