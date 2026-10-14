package com.ruleta.colores.network;

import com.ruleta.colores.RuletaColores;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: abre el menu de la ruleta. */
public record OpenMenuPayload() implements CustomPayload {
    public static final Id<OpenMenuPayload> ID = new Id<>(RuletaColores.id("open_menu"));
    public static final PacketCodec<RegistryByteBuf, OpenMenuPayload> CODEC = PacketCodec.unit(new OpenMenuPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
