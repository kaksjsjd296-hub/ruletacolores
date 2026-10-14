package com.ruleta.colores.client;

import com.ruleta.colores.RuletaColor;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** Cuadro de notificacion arriba a la izquierda: titulo + icono, mensaje y "Presiona L para ocultar". */
public final class NotificationHud {
    private static boolean visible = false;
    private static int rgb = 0xFFFFFF;
    private static Identifier icon = RuletaColor.randomTexture();
    private static String title = "";
    private static String message = "";

    private NotificationHud() {}

    public static void show(String colorId, String newTitle, String newMessage) {
        RuletaColor color = RuletaColor.byId(colorId);
        rgb = color != null ? color.rgb : 0xFFFFFF;
        icon = color != null ? color.texture() : RuletaColor.randomTexture();
        title = newTitle == null ? "" : newTitle;
        message = newMessage == null ? "" : newMessage;
        visible = true;
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.2f));
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void hide() {
        visible = false;
    }

    public static void render(DrawContext ctx) {
        if (!visible) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;

        final int x = 4, y = 4, pad = 4;
        final int boxW = Math.min(200, ctx.getScaledWindowWidth() - 8);
        final float titleScale = 1.5f, bodyScale = 1.2f;
        final int color = 0xFF000000 | rgb;

        Text titleText = Text.literal(title.toUpperCase()).setStyle(Style.EMPTY.withBold(true));
        int iconSize = 14;
        int titleBarH = (int) (9 * titleScale) + pad * 2;

        List<OrderedText> lines = tr.wrapLines(Text.literal(message), (int) ((boxW - pad * 2) / bodyScale));
        int bodyH = (int) (lines.size() * 9 * bodyScale);
        int hintH = 12;
        int totalH = titleBarH + (lines.isEmpty() ? 0 : bodyH + pad) + hintH + pad;

        // fondo
        ctx.fill(x, y, x + boxW, y + totalH, 0xE0141A24);
        ctx.fill(x, y, x + boxW, y + titleBarH, 0xF0000000);

        // titulo
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x + pad, y + pad, 0);
        ctx.getMatrices().scale(titleScale, titleScale, 1f);
        ctx.drawText(tr, titleText, 0, 0, color, true);
        ctx.getMatrices().pop();

        // icono de la ruleta que toco, al lado del titulo
        int titleW = (int) (tr.getWidth(titleText) * titleScale);
        int iconX = Math.min(x + pad + titleW + 4, x + boxW - iconSize - pad);
        ctx.drawTexture(icon, iconX, y + (titleBarH - iconSize) / 2, iconSize, iconSize, 0f, 0f, 128, 128, 128, 128);

        // mensaje
        int ty = y + titleBarH + pad;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x + pad, ty, 0);
        ctx.getMatrices().scale(bodyScale, bodyScale, 1f);
        int ly = 0;
        for (OrderedText line : lines) {
            ctx.drawText(tr, line, 0, ly, color, true);
            ly += 9;
        }
        ctx.getMatrices().pop();

        // pista
        Text hint = Text.literal("Presiona ").formatted(Formatting.WHITE)
                .append(Text.literal("L").formatted(Formatting.YELLOW))
                .append(Text.literal(" para ocultar este mensaje").formatted(Formatting.WHITE));
        ctx.drawText(tr, hint, x + pad, y + totalH - hintH - 1, 0xFFFFFFFF, true);
    }
}
