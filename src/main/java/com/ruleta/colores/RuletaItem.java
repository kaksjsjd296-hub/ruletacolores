package com.ruleta.colores;

import com.ruleta.colores.network.OpenMenuPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RuletaItem extends Item {
    public RuletaItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            if (!player.hasPermissionLevel(2)) {
                player.sendMessage(Text.literal("Solo los operadores pueden usar la ruleta.").formatted(Formatting.RED), true);
            } else if (ServerPlayNetworking.canSend(player, OpenMenuPayload.ID)) {
                ServerPlayNetworking.send(player, new OpenMenuPayload());
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
    }
}
