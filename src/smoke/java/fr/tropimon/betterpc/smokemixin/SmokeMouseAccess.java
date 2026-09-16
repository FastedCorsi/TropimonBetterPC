package fr.tropimon.betterpc.smokemixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Moves only the isolated test client's logical pointer, never the operating-system pointer. */
@Mixin(Mouse.class)
public interface SmokeMouseAccess {
  @Invoker("onCursorPos")
  void betterpcSmoke$cursor(long window, double x, double y);
}
