package com.ruleta.colores.network;

import com.ruleta.colores.RuletaColores;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> todos los clientes: gira la ruleta en pantalla y cae en "color"; despues sale la notificacion. */
public record SpinPayload(String color, String title, String message, AnimConfig config, int jitter) implements CustomPayload {
    public static final Id<SpinPayload> ID = new Id<>(RuletaColores.id("spin"));
    public static final PacketCodec<RegistryByteBuf, SpinPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(32), SpinPayload::color,
            PacketCodecs.string(128), SpinPayload::title,
            PacketCodecs.string(512), SpinPayload::message,
            AnimConfig.CODEC, SpinPayload::config,
            PacketCodecs.VAR_INT, SpinPayload::jitter,
            SpinPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
