package com.ruleta.colores.mixin;

import com.ruleta.colores.RuletaColores;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    /** Reemplaza el mensaje amarillo de "X joined the game" por uno verde con el icono de la ruleta. */
    @ModifyArg(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"),
            index = 0
    )
    private Text ruleta_colores$joinMessage(Text original) {
        if (original.getContent() instanceof TranslatableTextContent content) {
            String key = content.getKey();
            Object[] args = content.getArgs();
            if ((key.equals("multiplayer.player.joined") || key.equals("multiplayer.player.joined.renamed")) && args.length > 0) {
                String name = args[0] instanceof Text t ? t.getString() : String.valueOf(args[0]);
                MutableText icon = Text.literal("\uE000").setStyle(Style.EMPTY.withFont(RuletaColores.id("icons")));
                return Text.empty()
                        .append(icon)
                        .append(Text.literal(" " + name + " se conectó").formatted(Formatting.GREEN));
            }
        }
        return original;
    }
}
