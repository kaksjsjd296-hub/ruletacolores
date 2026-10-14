package com.ruleta.colores.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ruleta.colores.RuletaColor;
import com.ruleta.colores.RuletaColores;
import com.ruleta.colores.network.AnimConfig;
import com.ruleta.colores.network.SpinPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * Animacion de la ruleta girando en pantalla (la ven todos los jugadores).
 * Gira con frenado suave, cae en el color elegido y despues aparece la notificacion.
 * Para usar el arte del texture pack, reemplaza textures/gui/spin_wheel.png y spin_pointer.png.
 */
public final class SpinAnimation {
    private static final Identifier WHEEL = RuletaColores.id("textures/gui/spin_wheel.png");
    private static final Identifier POINTER = RuletaColores.id("textures/gui/spin_pointer.png");
    private static final int HOLD_TICKS = 30;
    private static final int FADE_TICKS = 12;

    private static boolean active = false;
    private static float age = 0f;
    private static int spinTicks = 100;
    private static float finalAngle = 0f;
    private static int lastSector = Integer.MIN_VALUE;
    private static boolean customSound = false;

    private static RuletaColor target;
    private static String title = "";
    private static String message = "";
    private static AnimConfig cfg = AnimConfig.defaults();

    private SpinAnimation() {}

    public static void start(SpinPayload p) {
        start(p.color(), p.title(), p.message(), p.config().clamped(), p.jitter());
    }

    public static void start(String colorId, String newTitle, String newMessage, AnimConfig config, int jitter) {
        RuletaColor color = RuletaColor.byId(colorId);
        title = newTitle;
        message = newMessage;
        if (color == null) {
            NotificationHud.show(colorId, newTitle, newMessage);
            return;
        }
        target = color;
        cfg = config;
        spinTicks = cfg.durationSec() * 20;
        float base = (((-color.sectorAngle + jitter) % 360) + 360) % 360;
        finalAngle = cfg.spins() * 360f + base;
        age = 0f;
        lastSector = Integer.MIN_VALUE;
        active = true;

        MinecraftClient mc = MinecraftClient.getInstance();
        customSound = mc.getResourceManager().getResource(RuletaColores.id("sounds/spin.ogg")).isPresent();
        if (customSound && cfg.volume() > 0) {
            mc.getSoundManager().play(PositionedSoundInstance.master(RuletaColores.SPIN_SOUND, 1.0f, cfg.volume() / 100f));
        }
    }

    private static float angleAt(float t) {
        float p = Math.min(1f, t / spinTicks);
        float eased = 1f - (float) Math.pow(1f - p, 3);
        return finalAngle * eased;
    }

    public static void tick() {
        if (!active) return;
        age += 1f;
        float total = spinTicks + HOLD_TICKS + FADE_TICKS;

        if (age <= spinTicks && !customSound && cfg.volume() > 0) {
            int sector = (int) Math.floor((angleAt(age) + 7.5f) / 45f);
            if (lastSector != Integer.MIN_VALUE && sector != lastSector) {
                float pitch = 1.9f - 0.9f * Math.min(1f, age / spinTicks);
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, pitch, cfg.volume() / 100f));
            }
            lastSector = sector;
        }

        if (age >= total) {
            active = false;
            NotificationHud.show(target.id, title, message);
        }
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!active) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        float t = age + tickCounter.getTickDelta(false);
        float total = spinTicks + HOLD_TICKS + FADE_TICKS;

        float alpha = 1f;
        if (t < 8f) alpha = t / 8f;
        else if (t > total - FADE_TICKS) alpha = Math.max(0f, (total - t) / FADE_TICKS);

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int size = Math.max(32, sh * cfg.sizePercent() / 100);
        float cx = sw / 2f;
        float cy = switch (cfg.position()) {
            case 1 -> size / 2f + 12;
            case 2 -> sh - size / 2f - 12;
            default -> sh / 2f;
        };

        int dimA = (int) (255 * (cfg.dim() / 100f) * alpha);
        if (dimA > 3) ctx.fill(0, 0, sw, sh, dimA << 24);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angleAt(t)));
        ctx.drawTexture(WHEEL, -size / 2, -size / 2, size, size, 0f, 0f, 256, 256, 256, 256);
        ctx.getMatrices().pop();

        int pw = Math.max(12, (int) (size * 0.2f));
        ctx.drawTexture(POINTER, (int) (cx - pw / 2f), (int) (cy - size / 2f - pw * 0.6f), pw, pw, 0f, 0f, 64, 64, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // resultado cuando termina de girar
        int a = (int) (255 * alpha);
        if (t >= spinTicks && a > 3) {
            float pulse = 1f + 0.06f * (float) Math.sin((t - spinTicks) * 0.5f);
            ctx.getMatrices().push();
            ctx.getMatrices().translate(cx, cy + size / 2f + 8, 0);
            ctx.getMatrices().scale(2f * pulse, 2f * pulse, 1f);
            ctx.drawCenteredTextWithShadow(tr, Text.literal("¡" + target.displayName + "!"), 0, 0, (a << 24) | target.rgb);
            ctx.getMatrices().pop();
        }
    }
}
