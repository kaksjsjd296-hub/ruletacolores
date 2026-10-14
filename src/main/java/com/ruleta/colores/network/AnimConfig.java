package com.ruleta.colores.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.MathHelper;

/** Ajustes de la animacion de la ruleta (los elige el operador en el menu; el servidor los limita). */
public record AnimConfig(int sizePercent, int durationSec, int spins, int position, int volume, int dim) {
    public static final PacketCodec<RegistryByteBuf, AnimConfig> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, AnimConfig::sizePercent,
            PacketCodecs.VAR_INT, AnimConfig::durationSec,
            PacketCodecs.VAR_INT, AnimConfig::spins,
            PacketCodecs.VAR_INT, AnimConfig::position,
            PacketCodecs.VAR_INT, AnimConfig::volume,
            PacketCodecs.VAR_INT, AnimConfig::dim,
            AnimConfig::new);

    public static AnimConfig defaults() {
        return new AnimConfig(45, 5, 5, 0, 80, 50);
    }

    /** size 15-90 % de la altura; duracion 1-15 s; vueltas 1-12; posicion 0 centro/1 arriba/2 abajo; volumen 0-100; oscurecer 0-90 %. */
    public AnimConfig clamped() {
        return new AnimConfig(
                MathHelper.clamp(sizePercent, 15, 90),
                MathHelper.clamp(durationSec, 1, 15),
                MathHelper.clamp(spins, 1, 12),
                MathHelper.clamp(position, 0, 2),
                MathHelper.clamp(volume, 0, 100),
                MathHelper.clamp(dim, 0, 90));
    }
}
