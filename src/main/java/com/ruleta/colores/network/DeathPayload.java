package com.ruleta.colores.network;

import com.ruleta.colores.RuletaColores;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> todos los clientes: reproduce la animacion de muerte. */
public record DeathPayload(String playerName, String key, List<String> args, String fallback) implements CustomPayload {
    public static final Id<DeathPayload> ID = new Id<>(RuletaColores.id("death"));
    public static final PacketCodec<RegistryByteBuf, DeathPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(64), DeathPayload::playerName,
            PacketCodecs.string(256), DeathPayload::key,
            PacketCodecs.string(256).collect(PacketCodecs.toList(16)), DeathPayload::args,
            PacketCodecs.string(1024), DeathPayload::fallback,
            DeathPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
