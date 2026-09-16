package fr.tropimon.betterpc;

import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;

import java.lang.reflect.Method;

final class PcPortrait {
  private static final Method PROFILE_RENDERER = findProfileRenderer();
  private final FloatingState state = new FloatingState();
  private final Quaternionf rotation = new Quaternionf();
  private RenderablePokemon renderable;
  private boolean failed;

  void draw(DrawContext context, Pokemon pokemon, int x, int y, int size, boolean animate) {
    if (failed) return;
    context.draw();
    // Minecraft 1.21.1 scissor bounds do not follow the GUI matrix scale.
    var matrix = context.getMatrices().peek().getPositionMatrix();
    var top = matrix.transformPosition(new org.joml.Vector3f(x, y, 0));
    var bottom = matrix.transformPosition(new org.joml.Vector3f(x + size, y + size, 0));
    context.enableScissor(
        (int) Math.floor(top.x),
        (int) Math.floor(top.y),
        (int) Math.ceil(bottom.x),
        (int) Math.ceil(bottom.y));
    context.getMatrices().push();
    try {
      context.getMatrices().translate(x + size / 2.0, y, 1000.0);
      float scale = size / 44.0f;
      context.getMatrices().scale(scale, scale, scale);
      ItemStack held =
          pokemon.getHeldItemVisible() ? pokemon.heldItemNoCopy$common() : ItemStack.EMPTY;
      if (renderable == null)
        renderable = new RenderablePokemon(pokemon.getSpecies(), pokemon.getAspects(), held);
      else {
        renderable.setSpecies(pokemon.getSpecies());
        renderable.setAspects(pokemon.getAspects());
        renderable.setHeldItem(held);
      }
      state.setCurrentAspects(pokemon.getAspects());
      var client = net.minecraft.client.MinecraftClient.getInstance();
      float delta =
          client.isPaused() || !animate
              ? 0
              : Math.clamp(client.getRenderTickCounter().getLastFrameDuration(), 0, 2);
      rotation.rotationXYZ((float) Math.toRadians(13), (float) Math.toRadians(25), 0);
      drawProfilePokemon(context, delta);
    } catch (RuntimeException | LinkageError failure) {
      failed = true;
      BetterPcClient.LOGGER.warn("Portrait indisponible pour une ressource Pokémon.");
    } finally {
      context.getMatrices().pop();
      context.disableScissor();
    }
  }

  private void drawProfilePokemon(DrawContext context, float delta) {
    try {
      if (PROFILE_RENDERER.getParameterCount() == 16) {
        Class<?> transformType =
            Class.forName("com.cobblemon.mod.common.client.gui.ProfileTransformType");
        Object profileTransform = transformType.getField("PROFILE").get(null);
        PROFILE_RENDERER.invoke(
            null, renderable, context.getMatrices(), rotation, PoseType.PROFILE, state,
            delta, 20.0F, profileTransform, true,
            1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 15);
      } else {
        PROFILE_RENDERER.invoke(
            null, renderable, context.getMatrices(), rotation, PoseType.PROFILE, state,
            delta, 20.0F, true, false,
            1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
      }
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("API de portrait Cobblemon non compatible", exception);
    }
  }

  private static Method findProfileRenderer() {
    for (Method method : PokemonGuiUtilsKt.class.getMethods()) {
      if (method.getName().equals("drawProfilePokemon")
          && method.getParameterCount() >= 15
          && method.getParameterTypes()[0] == RenderablePokemon.class) {
        return method;
      }
    }
    throw new IllegalStateException("Rendu de portrait Cobblemon introuvable");
  }
}
