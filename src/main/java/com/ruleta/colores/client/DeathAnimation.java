package com.ruleta.colores.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ruleta.colores.RuletaColores;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Animacion de muerte que ven todos los jugadores.
 *
 * Si el resource pack/mod incluye frames en textures/gui/death/frame_N.png y death_animation.json,
 * se reproducen. Si no, se muestra una animacion de respaldo (fundido oscuro + texto rojo).
 */
public final class DeathAnimation {
    private static final int TOTAL_TICKS = 120; // 6 segundos

    private static boolean active = false;
    private static float age = 0f;
    private static String name = "";
    private static Text cause = Text.empty();

    // config de frames
    private static int frames = 0;
    private static int fps = 12;
    private static int texW = 256, texH = 256;

    private DeathAnimation() {}

    public static void start(String playerName, String key, List<String> args, String fallback) {
        name = playerName;
        if (key != null && !key.isEmpty()) {
            cause = Text.translatable(key, args.toArray());
        } else {
            cause = Text.literal(fallback);
        }
        loadConfig();
        age = 0f;
        active = true;

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean hasCustomSound = mc.getResourceManager()
                .getResource(RuletaColores.id("sounds/death_animation.ogg")).isPresent();
        if (hasCustomSound) {
            mc.getSoundManager().play(PositionedSoundInstance.master(RuletaColores.DEATH_SOUND, 1.0f));
        } else {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_DEATH, 1.0f));
        }
    }

    private static void loadConfig() {
        frames = 0;
        try {
            Optional<Resource> res = MinecraftClient.getInstance().getResourceManager()
                    .getResource(RuletaColores.id("death_animation.json"));
            if (res.isPresent()) {
                try (Reader reader = res.get().getReader()) {
                    JsonObject o = JsonParser.parseReader(reader).getAsJsonObject();
                    frames = o.has("frames") ? o.get("frames").getAsInt() : 0;
                    fps = o.has("fps") ? o.get("fps").getAsInt() : 12;
                    texW = o.has("width") ? o.get("width").getAsInt() : 256;
                    texH = o.has("height") ? o.get("height").getAsInt() : 256;
                }
            }
        } catch (Exception e) {
            RuletaColores.LOGGER.warn("No se pudo leer death_animation.json, usando animacion de respaldo", e);
            frames = 0;
        }
    }

    public static void tick() {
        if (!active) return;
        age += 1f;
        if (age >= TOTAL_TICKS) active = false;
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!active) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        float t = age + tickCounter.getTickDelta(false);

        float alpha = 1f;
        if (t < 10f) alpha = t / 10f;
        else if (t > TOTAL_TICKS - 20f) alpha = Math.max(0f, (TOTAL_TICKS - t) / 20f);

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        // fondo oscuro con tinte rojo
        int bgA = (int) (170 * alpha);
        if (bgA > 3) ctx.fill(0, 0, sw, sh, (bgA << 24) | 0x100000);

        int textTop = sh / 2;

        if (frames > 0) {
            int idx = Math.min(frames - 1, (int) (t / 20f * fps));
            Identifier tex = RuletaColores.id("textures/gui/death/frame_" + idx + ".png");
            int h = (int) (sh * 0.45f);
            int w = (int) (h * (texW / (float) texH));
            int x = (sw - w) / 2;
            int y = (int) (sh * 0.12f);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            ctx.drawTexture(tex, x, y, w, h, 0f, 0f, texW, texH, texW, texH);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            textTop = y + h + 8;
        } else {
            // respaldo: calavera grande que "late"
            float pulse = 1f + 0.08f * (float) Math.sin(t * 0.4f);
            int a = (int) (255 * alpha);
            if (a > 3) {
                ctx.getMatrices().push();
                ctx.getMatrices().translate(sw / 2f, sh * 0.30f, 0);
                ctx.getMatrices().scale(8f * pulse, 8f * pulse, 1f);
                ctx.drawCenteredTextWithShadow(tr, "☠", 0, -4, (a << 24) | 0xFF2020);
                ctx.getMatrices().pop();
            }
            textTop = (int) (sh * 0.30f) + 50;
        }

        int a = (int) (255 * alpha);
        if (a <= 3) return;
        int red = (a << 24) | 0xFF3030;

        // nombre del jugador (grande, rojo)
        ctx.getMatrices().push();
        ctx.getMatrices().translate(sw / 2f, textTop, 0);
        ctx.getMatrices().scale(3f, 3f, 1f);
        ctx.drawCenteredTextWithShadow(tr, name, 0, 0, red);
        ctx.getMatrices().pop();

        // causa de la muerte debajo, en rojo
        List<OrderedText> lines = tr.wrapLines(cause, (int) (sw * 0.8f / 1.5f));
        ctx.getMatrices().push();
        ctx.getMatrices().translate(sw / 2f, textTop + 34, 0);
        ctx.getMatrices().scale(1.5f, 1.5f, 1f);
        int ly = 0;
        for (OrderedText line : lines) {
            int lw = tr.getWidth(line);
            ctx.drawText(tr, line, -lw / 2, ly, red, true);
            ly += 10;
        }
        ctx.getMatrices().pop();
    }
}
