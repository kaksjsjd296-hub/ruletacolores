package com.ruleta.colores.client;

import com.ruleta.colores.RuletaColor;
import com.ruleta.colores.network.AnimConfig;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** Configuracion de la animacion de la ruleta (se guarda en tu cliente y se envia al servidor con cada giro). */
public class AnimSettingsScreen extends Screen {
    private static final String[] POSITIONS = {"Centro", "Arriba", "Abajo"};

    private final Screen parent;
    private int size, duration, spins, position, volume, dim;
    private ButtonWidget positionButton;

    public AnimSettingsScreen(Screen parent) {
        super(Text.literal("Configurar animación"));
        this.parent = parent;
        AnimConfig c = AnimSettings.current();
        size = c.sizePercent();
        duration = c.durationSec();
        spins = c.spins();
        position = c.position();
        volume = c.volume();
        dim = c.dim();
    }

    private AnimConfig config() {
        return new AnimConfig(size, duration, spins, position, volume, dim).clamped();
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int w = 220, x = cx - w / 2;
        int y = 36, step = 24;

        addDrawableChild(new IntSlider(x, y, w, "Tamaño", "%", 15, 90, size, v -> size = v));
        addDrawableChild(new IntSlider(x, y + step, w, "Duración", " s", 1, 15, duration, v -> duration = v));
        addDrawableChild(new IntSlider(x, y + step * 2, w, "Vueltas", "", 1, 12, spins, v -> spins = v));
        addDrawableChild(new IntSlider(x, y + step * 3, w, "Volumen", "%", 0, 100, volume, v -> volume = v));
        addDrawableChild(new IntSlider(x, y + step * 4, w, "Oscurecer pantalla", "%", 0, 90, dim, v -> dim = v));

        positionButton = addDrawableChild(ButtonWidget.builder(positionText(), b -> {
            position = (position + 1) % POSITIONS.length;
            b.setMessage(positionText());
        }).dimensions(x, y + step * 5, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Probar animación"), b -> {
            AnimConfig c = config();
            RuletaColor color = RuletaColor.values()[(int) (Math.random() * RuletaColor.values().length)];
            SpinAnimation.start(color.id, "PRUEBA", "Así se verá la ruleta.", c, 0);
            close();
        }).dimensions(x, y + step * 6 + 6, w / 2 - 2, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Restablecer"), b -> {
            AnimConfig d = AnimConfig.defaults();
            size = d.sizePercent(); duration = d.durationSec(); spins = d.spins();
            position = d.position(); volume = d.volume(); dim = d.dim();
            clearAndInit();
        }).dimensions(x + w / 2 + 2, y + step * 6 + 6, w / 2 - 2, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Guardar y volver"), b -> close())
                .dimensions(x, y + step * 7 + 10, w, 20).build());
    }

    private Text positionText() {
        return Text.literal("Posición: " + POSITIONS[position]);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFF);
    }

    @Override
    public void close() {
        AnimSettings.set(config());
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class IntSlider extends SliderWidget {
        private final String label, suffix;
        private final int min, max;
        private final IntConsumer onChange;

        IntSlider(int x, int y, int w, String label, String suffix, int min, int max, int value, IntConsumer onChange) {
            super(x, y, w, 20, Text.empty(), (value - min) / (double) (max - min));
            this.label = label;
            this.suffix = suffix;
            this.min = min;
            this.max = max;
            this.onChange = onChange;
            updateMessage();
        }

        private int intValue() {
            return MathHelper.clamp((int) Math.round(min + value * (max - min)), min, max);
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + intValue() + suffix));
        }

        @Override
        protected void applyValue() {
            onChange.accept(intValue());
        }
    }
}
