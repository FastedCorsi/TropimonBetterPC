package fr.tropimon.betterpc.mixin;

import com.cobblemon.mod.common.api.mark.Mark;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Select the official translated overloads rather than the Kotlin String getters. */
@Mixin(value = Mark.class, remap = false)
public interface MarkTextAccessor {
  @Invoker("getName")
  MutableText betterPc$name();

  @Invoker("getDescription")
  MutableText betterPc$description();
}
