package com.ruleta.colores;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public enum RuletaColor {
    ROJO("rojo", "Rojo", 0xFF3B3B, 1, 30),
    NARANJA("naranja", "Naranja", 0xFF9A2E, 2, 345),
    AMARILLO("amarillo", "Amarillo", 0xFFE02E, 3, 300),
    VERDE("verde", "Verde", 0x3BE03B, 4, 255),
    CIAN("cian", "Cian", 0x2EE6E6, 5, 210),
    AZUL("azul", "Azul", 0x2828FF, 6, 165),
    MORADA("morada", "Morada", 0xB040FF, 7, 120),
    ROSA("rosa", "Rosa", 0xFF3FB4, 8, 75);

    /** Id especial: el servidor elige un color al azar. */
    public static final String RANDOM = "aleatorio";

    public final String id;
    public final String displayName;
    public final int rgb;
    /** Valor de custom_model_data que usa el item para mostrar esta ruleta. */
    public final int modelData;
    /** Angulo (grados, sentido horario desde arriba) del centro del sector de este color en la imagen spin_wheel.png. */
    public final int sectorAngle;

    RuletaColor(String id, String displayName, int rgb, int modelData, int sectorAngle) {
        this.sectorAngle = sectorAngle;
        this.id = id;
        this.displayName = displayName;
        this.rgb = rgb;
        this.modelData = modelData;
    }

    public Identifier texture() {
        return RuletaColores.id("textures/item/ruleta_" + id + ".png");
    }

    public static Identifier randomTexture() {
        return RuletaColores.id("textures/item/ruleta_aleatorio.png");
    }

    public static RuletaColor byId(String id) {
        for (RuletaColor c : values()) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    public static RuletaColor random(Random random) {
        RuletaColor[] all = values();
        return all[random.nextInt(all.length)];
    }
}
