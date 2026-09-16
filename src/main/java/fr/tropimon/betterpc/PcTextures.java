package fr.tropimon.betterpc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Native Cobblemon PC housing and controls, colored collection cards and a small pixel heart. */
final class PcTextures {
  private static Identifier texture(String path) {
    return Identifier.of("cobblemon", "textures/gui/" + path + ".png");
  }

  static final Identifier BASE = texture("pc/pc_base");
  static final Identifier FRAME = texture("pc/pc_screen_overlay");
  static final Identifier SLOT = texture("summary/summary_party_slot");
  static final Identifier RELEASE = texture("pc/pc_release_button");
  static final Identifier SHINY = texture("summary/icon_shiny");
  static final Identifier NEXT = texture("pc/pc_arrow_next");
  static final Identifier PREVIOUS = texture("pc/pc_arrow_previous");
  static final Identifier TYPES = texture("types_small");
  private static final Identifier POKEDOLLAR =
      Identifier.of("tropimon_better_pc", "textures/gui/pokedollar.png");
  private static final Identifier MALE = texture("pc/gender_icon_male");
  private static final Identifier FEMALE = texture("pc/gender_icon_female");
  private static final String[] HEART = {
    "011000110", "111101111", "111111111", "111111111",
    "011111110", "001111100", "000111000", "000010000"
  };

  // Align the origin and source pixel size once, regardless of cell position.
  static void pixelOrigin(DrawContext c, int x, int y, float requested) {
    double gui = net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaleFactor();
    var matrix = c.getMatrices().peek().getPositionMatrix();
    float pixels = (float) (matrix.m00() * gui);
    float unit = Math.max(1, Math.round(requested * pixels)) / pixels;
    float sx =
        (float)
            ((Math.round((matrix.m30() + x * matrix.m00()) * gui) / gui - matrix.m30())
                / matrix.m00());
    float sy =
        (float)
            ((Math.round((matrix.m31() + y * matrix.m11()) * gui) / gui - matrix.m31())
                / matrix.m11());
    c.getMatrices().push();
    c.getMatrices().translate(sx, sy, 0);
    c.getMatrices().scale(unit, unit, 1);
  }

  static void gender(DrawContext c, com.cobblemon.mod.common.pokemon.Pokemon p, int x, int y) {
    var gender = p.getGender();
    if (gender == com.cobblemon.mod.common.pokemon.Gender.GENDERLESS) return;
    // One source pixel occupies one or more whole framebuffer pixels.
    // Match native 6 x 8 proportions instead of stretching to 9 x 12.
    pixelOrigin(c, x, y, 1);
    part(
        c,
        gender == com.cobblemon.mod.common.pokemon.Gender.MALE ? MALE : FEMALE,
        0,
        0,
        6,
        8,
        0,
        0,
        6,
        8,
        6,
        8);
    c.getMatrices().pop();
  }

  static void pokedollar(DrawContext c, int x, int y) {
    pixelOrigin(c, x, y, 1);
    tint(c, 0xFFD84D);
    part(c, POKEDOLLAR, 0, 0, 5, 8, 0, 0, 5, 8, 5, 8);
    tint(c, 0xFFFFFF);
    c.getMatrices().pop();
  }

  static void heart(DrawContext c, int x, int y, boolean filled) {
    pixelOrigin(c, x, y, 1.2f);
    for (int row = 0; row < HEART.length; row++)
      for (int col = 0; col < HEART[row].length(); col++) {
        if (HEART[row].charAt(col) != '1') continue;
        boolean edge =
            row == 0
                || row == HEART.length - 1
                || col == 0
                || col == 8
                || HEART[row - 1].charAt(col) == '0'
                || HEART[row + 1].charAt(col) == '0'
                || HEART[row].charAt(col - 1) == '0'
                || HEART[row].charAt(col + 1) == '0';
        if (filled || edge) c.fill(col, row, col + 1, row + 1, 0xFFFF3F59);
      }
    c.getMatrices().pop();
  }

  static void typeIcon(
      DrawContext context, com.cobblemon.mod.common.api.types.ElementalType type, int x, int y) {
    pixelOrigin(context, x, y, 1);
    part(context, TYPES, 0, 0, 13, 13, type.getTextureXMultiplier() * 18, 0, 18, 18, 324, 18);
    context.getMatrices().pop();
  }

  static void ballIcon(
      DrawContext context, com.cobblemon.mod.common.pokemon.Pokemon pokemon, int x, int y) {
    var id = pokemon.getCaughtBall().getName();
    var texture = Identifier.of(id.getNamespace(), "textures/gui/ball/" + id.getPath() + ".png");
    part(context, texture, x, y, 12, 15, 0, 0, 18, 22, 18, 44);
  }

  static void panel(DrawContext context, int x, int y, int w, int h) {
    tint(context, 0xFFFFFF);
    part(context, BASE, x, y, w, h, 120, 60, 1, 1, 349, 205);
    slice(context, FRAME, x, y, w, h, 0, 174, 155, 174, 155, 3);
    tint(context, 0xFFFFFF);
  }

  static void outerPanel(DrawContext context, int x, int y, int w, int h) {
    tint(context, 0xFFFFFF);
    frame(context, x, y, w, h);
  }

  static void slot(DrawContext context, int x, int y, int w, int h, boolean highlighted) {
    tint(context, 0x58BBAA);
    // The native texture also contains a party footer; keep only its framed face.
    slice(context, SLOT, x, y, w, h, highlighted ? 27 : 0, 42, 20, 46, 54, 5);
    tint(context, 0xFFFFFF);
  }

  static void coloredSlot(
      DrawContext context,
      int x,
      int y,
      int w,
      int h,
      boolean hovered,
      boolean selected,
      String primary,
      String secondary) {
    tint(context, typeColor(primary));
    slice(context, SLOT, x, y, w, h, hovered ? 27 : 0, 42, 20, 46, 54, 5);
    if (secondary != null && !secondary.equalsIgnoreCase(primary)) {
      diagonal(context, x, y, w, h, hovered, typeColor(secondary));
    }

    tint(context, 0xFFFFFF);
    if (selected) {
      int border = 0xFFFF5367;
      context.fill(x + 6, y + 6, x + w - 6, y + h - 6, 0x55F02F46);
      context.fill(x + 4, y + 4, x + w - 4, y + 6, border);
      context.fill(x + 4, y + h - 6, x + w - 4, y + h - 4, border);
      context.fill(x + 4, y + 6, x + 6, y + h - 6, border);
      context.fill(x + w - 6, y + 6, x + w - 4, y + h - 6, border);
    }
  }

  private static void diagonal(
      DrawContext context, int x, int y, int w, int h, boolean hovered, int color) {
    // Clip every part of the native slot, including its border, to the second-type half.
    tint(context, 0xFFFFFF);
    com.mojang.blaze3d.systems.RenderSystem.setShader(
        net.minecraft.client.render.GameRenderer::getPositionTexColorProgram);
    com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, SLOT);
    var vertices =
        net.minecraft.client.render.Tessellator.getInstance()
            .begin(
                net.minecraft.client.render.VertexFormat.DrawMode.TRIANGLES,
                net.minecraft.client.render.VertexFormats.POSITION_TEXTURE_COLOR);
    var matrix = context.getMatrices().peek().getPositionMatrix();
    int argb = 0xFF000000 | color;
    float[] xs = {0, 5, w - 5, w}, ys = {0, 5, h - 5, h};
    float[] us = {0, 5, 37, 42}, vs = {0, 5, 15, 20};
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 3; col++) {
        float[][] quad = {
          {xs[col], ys[row], us[col], vs[row]},
          {xs[col], ys[row + 1], us[col], vs[row + 1]},
          {xs[col + 1], ys[row + 1], us[col + 1], vs[row + 1]},
          {xs[col + 1], ys[row], us[col + 1], vs[row]}
        };
        java.util.List<float[]> clipped = new java.util.ArrayList<>(5);
        for (int i = 0; i < 4; i++) {
          float[] a = quad[i], b = quad[(i + 1) % 4];
          float da = a[0] / w + a[1] / h - 1, db = b[0] / w + b[1] / h - 1;
          if (da >= 0) clipped.add(a);
          if ((da >= 0) != (db >= 0)) {
            float t = da / (da - db);
            float[] cross = new float[4];
            for (int axis = 0; axis < 4; axis++) cross[axis] = a[axis] + t * (b[axis] - a[axis]);
            clipped.add(cross);
          }
        }

        for (int i = 1; i + 1 < clipped.size(); i++)
          for (int index : new int[] {0, i, i + 1}) {
            float[] p = clipped.get(index);
            vertices
                .vertex(matrix, x + p[0], y + p[1], 0)
                .texture(p[2] / 46, (p[3] + (hovered ? 27 : 0)) / 54)
                .color(argb);
          }
      }

    net.minecraft.client.render.BufferRenderer.drawWithGlobalProgram(vertices.end());
  }

  private static int typeColor(String type) {
    return switch (type.toLowerCase(java.util.Locale.ROOT)) {
      case "fire" -> 0xED9565;
      case "water" -> 0x65A8ED;
      case "grass" -> 0x7DD982;
      case "electric" -> 0xE6CA60;
      case "ice" -> 0x82DBE8;
      case "fighting" -> 0xD57C72;
      case "poison" -> 0xBE82DB;
      case "ground" -> 0xCEA576;
      case "flying" -> 0xA5B0EB;
      case "psychic" -> 0xE281B5;
      case "bug" -> 0xAFCE76;
      case "rock" -> 0xC2AF82;
      case "ghost" -> 0xA38ACC;
      case "dragon" -> 0x9F90ED;
      case "dark" -> 0xB2A0A8;
      case "steel" -> 0xA0C2D6;
      case "fairy" -> 0xE8A0D0;
      default -> 0xB3BFD0;
    };
  }

  private static void frame(DrawContext c, int x, int y, int w, int h) {
    // The central PC housing is a separate region of pc_base. Preserve its angled
    // corners and 21-pixel top/bottom rails; expand only the straight middle sections.
    int[] sourceX = {77, 111, 233, 267}, sourceY = {6, 27, 183, 204};
    int[] targetX = {x, x + 34, x + w - 34, x + w};
    int[] targetY = {y, y + 21, y + h - 21, y + h};
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 3; col++) {
        part(
            c,
            BASE,
            targetX[col],
            targetY[row],
            targetX[col + 1] - targetX[col],
            targetY[row + 1] - targetY[row],
            sourceX[col],
            sourceY[row],
            sourceX[col + 1] - sourceX[col],
            sourceY[row + 1] - sourceY[row],
            349,
            205);
      }
  }

  private static void tint(DrawContext context, int rgb) {
    context.draw();
    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(
        ((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, 1);
  }

  static void input(DrawContext context, int x, int y, int w, int h) {
    tint(context, 0x8194A0);
    slice(context, RELEASE, x, y, w, h, 0, 58, 16, 58, 32, 2);
    tint(context, 0xFFFFFF);
  }

  static void button(
      DrawContext context,
      int x,
      int y,
      int w,
      int h,
      boolean active,
      boolean hover,
      boolean danger) {
    tint(context, !active ? 0x6D7880 : danger ? 0xFFFFFF : hover ? 0x85EDD3 : 0x92BDE5);
    if (danger && active) slice(context, RELEASE, x, y, w, h, 16, 58, 16, 58, 32, 2);
    else slice(context, RELEASE, x, y, w, h, 0, 58, 16, 58, 32, 2);
    tint(context, 0xFFFFFF);
  }

  static void icon(
      DrawContext context, Identifier texture, int x, int y, int size, int sourceSize) {
    part(context, texture, x, y, size, size, 0, 0, sourceSize, sourceSize, sourceSize, sourceSize);
  }

  static void part(
      DrawContext context,
      Identifier texture,
      int x,
      int y,
      int w,
      int h,
      int u,
      int v,
      int sw,
      int sh,
      int tw,
      int th) {
    context.draw();
    com.mojang.blaze3d.systems.RenderSystem.enableBlend();
    com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
    context.drawTexture(texture, x, y, w, h, (float) u, (float) v, sw, sh, tw, th);
  }

  // Keep the original pixel corners and borders while expanding only the middle.
  private static void slice(
      DrawContext context,
      Identifier texture,
      int x,
      int y,
      int w,
      int h,
      int v,
      int sw,
      int sh,
      int tw,
      int th,
      int border) {
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 3; col++) {
        int dx = x + (col == 0 ? 0 : col == 1 ? border : w - border);
        int dy = y + (row == 0 ? 0 : row == 1 ? border : h - border);
        int u = col == 0 ? 0 : col == 1 ? border : sw - border;
        int sv = v + (row == 0 ? 0 : row == 1 ? border : sh - border);
        part(
            context,
            texture,
            dx,
            dy,
            col == 1 ? w - border * 2 : border,
            row == 1 ? h - border * 2 : border,
            u,
            sv,
            col == 1 ? sw - border * 2 : border,
            row == 1 ? sh - border * 2 : border,
            tw,
            th);
      }
  }
}
