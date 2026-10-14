package com.ruleta.colores.network;

import com.ruleta.colores.RuletaColores;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> servidor: color elegido + titulo + mensaje + ajustes de animacion. */
public record SubmitPayload(String color, String title, String message, AnimConfig config) implements CustomPayload {
    public static final Id<SubmitPayload> ID = new Id<>(RuletaColores.id("submit"));
    public static final PacketCodec<RegistryByteBuf, SubmitPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(32), SubmitPayload::color,
            PacketCodecs.string(128), SubmitPayload::title,
            PacketCodecs.string(512), SubmitPayload::message,
            AnimConfig.CODEC, SubmitPayload::config,
            SubmitPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
