package com.ruleta.colores.client;

import com.ruleta.colores.RuletaColor;
import com.ruleta.colores.network.SubmitPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Menu de /ruleta menu: elegir color (o aleatorio), escribir titulo y mensaje, y enviar a todos. */
public class RuletaMenuScreen extends Screen {
    private String selected = RuletaColor.RANDOM;
    private TextFieldWidget titleField;
    private TextFieldWidget messageField;

    public RuletaMenuScreen() {
        super(Text.literal("Ruleta de Colores"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int size = 30, gap = 4;
        int count = RuletaColor.values().length + 1;
        int totalW = count * size + (count - 1) * gap;
        int startX = cx - totalW / 2;
        int y = 44;

        for (int i = 0; i < RuletaColor.values().length; i++) {
            RuletaColor c = RuletaColor.values()[i];
            addDrawableChild(new ColorButton(startX + i * (size + gap), y, size, c.id, c.displayName, c.texture()));
        }
        addDrawableChild(new ColorButton(startX + (count - 1) * (size + gap), y, size,
                RuletaColor.RANDOM, "Aleatorio", RuletaColor.randomTexture()));

        titleField = new TextFieldWidget(textRenderer, cx - 130, 108, 260, 20, Text.literal("Título"));
        titleField.setMaxLength(48);
        titleField.setPlaceholder(Text.literal("Título (ej: CAMBIO DE DIFICULTAD)"));
        addDrawableChild(titleField);

        messageField = new TextFieldWidget(textRenderer, cx - 130, 148, 260, 20, Text.literal("Mensaje"));
        messageField.setMaxLength(160);
        messageField.setPlaceholder(Text.literal("Mensaje que verán todos los jugadores"));
        addDrawableChild(messageField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Enviar a todos"), b -> send())
                .dimensions(cx - 130, 182, 128, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(cx + 2, 182, 128, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Configurar animación"), b -> client.setScreen(new AnimSettingsScreen(this)))
                .dimensions(cx - 130, 208, 260, 20).build());

        setInitialFocus(titleField);
    }

    private void send() {
        String title = titleField.getText().trim();
        String message = messageField.getText().trim();
        if (title.isEmpty() && message.isEmpty()) {
            title = "AVISO";
        }
        ClientPlayNetworking.send(new SubmitPayload(selected, title, message, AnimSettings.current()));
        close();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFF);

        RuletaColor c = RuletaColor.byId(selected);
        String label = c != null ? c.displayName : "Aleatorio";
        int color = c != null ? (0xFF000000 | c.rgb) : 0xFFFFFFFF;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Color elegido: " + label), width / 2, 30, color);
        ctx.drawTextWithShadow(textRenderer, "Título", width / 2 - 130, 96, 0xA0A0A0);
        ctx.drawTextWithShadow(textRenderer, "Mensaje", width / 2 - 130, 136, 0xA0A0A0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private class ColorButton extends ClickableWidget {
        private final String colorId;
        private final Identifier texture;

        ColorButton(int x, int y, int size, String colorId, String name, Identifier texture) {
            super(x, y, size, size, Text.literal(name));
            this.colorId = colorId;
            this.texture = texture;
            setTooltip(Tooltip.of(Text.literal(name)));
        }

        @Override
        protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            boolean isSelected = selected.equals(colorId);
            int border = isSelected ? 0xFFFFFFFF : (isHovered() ? 0xFFAAAAAA : 0xFF404040);
            ctx.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, border);
            ctx.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101018);
            ctx.drawTexture(texture, getX() + 1, getY() + 1, width - 2, height - 2, 0f, 0f, 128, 128, 128, 128);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            selected = colorId;
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }
}
