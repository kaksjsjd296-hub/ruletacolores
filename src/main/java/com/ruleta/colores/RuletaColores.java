package com.ruleta.colores;

import com.ruleta.colores.network.DeathPayload;
import com.ruleta.colores.network.SpinPayload;
import com.ruleta.colores.network.OpenMenuPayload;
import com.ruleta.colores.network.SubmitPayload;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuletaColores implements ModInitializer {
    public static final String MOD_ID = "ruleta_colores";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Item RULETA;
    public static SoundEvent DEATH_SOUND;
    public static SoundEvent SPIN_SOUND;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        RULETA = Registry.register(Registries.ITEM, id("ruleta"), new RuletaItem(new Item.Settings().maxCount(1)));
        DEATH_SOUND = Registry.register(Registries.SOUND_EVENT, id("death_animation"), SoundEvent.of(id("death_animation")));
        SPIN_SOUND = Registry.register(Registries.SOUND_EVENT, id("spin"), SoundEvent.of(id("spin")));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(RULETA));

        // Paquetes (registrados en comun para que funcione en servidor dedicado y en un solo jugador)
        PayloadTypeRegistry.playS2C().register(OpenMenuPayload.ID, OpenMenuPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpinPayload.ID, SpinPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DeathPayload.ID, DeathPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SubmitPayload.ID, SubmitPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SubmitPayload.ID, (payload, context) ->
                handleSubmit(context.player(), payload));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("ruleta")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("menu").executes(this::openMenu))
                        .then(CommandManager.literal("dar").executes(this::giveItem))));

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                if (player.isSpectator()) {
                    return false; // los espectadores no vuelven a "morir"
                }
                handleDeath(player, source);
                return false; // cancela la muerte real: sin pantalla de reaparecer
            }
            return true;
        });
    }

    // ------------------------------------------------------------------ comandos

    private int openMenu(CommandContext<net.minecraft.server.command.ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
            if (!ServerPlayNetworking.canSend(player, OpenMenuPayload.ID)) {
                ctx.getSource().sendError(Text.literal("Necesitas el mod Ruleta de Colores instalado en tu cliente."));
                return 0;
            }
            ServerPlayNetworking.send(player, new OpenMenuPayload());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Este comando solo lo puede usar un jugador."));
            return 0;
        }
    }

    private int giveItem(CommandContext<net.minecraft.server.command.ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
            ItemStack stack = new ItemStack(RULETA);
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("Este comando solo lo puede usar un jugador."));
            return 0;
        }
    }

    // ------------------------------------------------------------------ ruleta

    private void handleSubmit(ServerPlayerEntity player, SubmitPayload payload) {
        if (!player.hasPermissionLevel(2)) {
            return;
        }
        RuletaColor color = RuletaColor.RANDOM.equals(payload.color())
                ? RuletaColor.random(player.getRandom())
                : RuletaColor.byId(payload.color());
        if (color == null) {
            return;
        }
        String title = sanitize(payload.title(), 48);
        String message = sanitize(payload.message(), 160);

        // El icono de la ruleta (item) cambia al color que toco
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(RULETA)) {
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(color.modelData));
            }
        }

        SpinPayload spin = new SpinPayload(color.id, title, message, payload.config().clamped(), player.getRandom().nextInt(21) - 10);
        MinecraftServer server = player.getServer();
        if (server == null) return;
        for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(target, SpinPayload.ID)) {
                ServerPlayNetworking.send(target, spin);
            }
        }
    }

    private static String sanitize(String s, int max) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 32 && c != 127 && c != '§') sb.append(c);
        }
        String out = sb.toString().trim();
        return out.length() > max ? out.substring(0, max) : out;
    }

    // ------------------------------------------------------------------ muerte

    private void handleDeath(ServerPlayerEntity player, DamageSource source) {
        ServerWorld world = player.getServerWorld();
        MinecraftServer server = world.getServer();
        Text deathMessage = source.getDeathMessage(player);

        // 1) Mensaje en el chat para todos: quien murio y por que
        server.getPlayerManager().broadcast(
                Text.empty().append(Text.literal("☠ ")).append(deathMessage.copy()).formatted(Formatting.RED), false);

        // 2) Animacion de muerte para todos los jugadores
        String key = "";
        List<String> args = new ArrayList<>();
        if (deathMessage.getContent() instanceof TranslatableTextContent content) {
            key = content.getKey();
            for (Object o : content.getArgs()) {
                args.add(o instanceof Text t ? t.getString() : String.valueOf(o));
            }
        }
        DeathPayload payload = new DeathPayload(player.getName().getString(), key, args, deathMessage.getString());
        for (ServerPlayerEntity target : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(target, DeathPayload.ID)) {
                ServerPlayNetworking.send(target, payload);
            }
        }

        // 3) Cabeza con su skin sobre una valla de madera
        placeMemorial(world, player);

        // 4) Soltar inventario y experiencia (como una muerte normal)
        if (!world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            player.getInventory().dropAll();
            int xp = Math.min(player.experienceLevel * 7, 100);
            if (xp > 0) {
                ExperienceOrbEntity.spawn(world, player.getPos(), xp);
            }
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
        }

        // 5) Pasar a espectador, sin pantalla de reaparecer
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.clearStatusEffects();
        player.extinguish();
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0;
        if (player.getY() < world.getBottomY() + 5) {
            player.requestTeleport(player.getX(), world.getBottomY() + 70, player.getZ());
        }
        player.changeGameMode(GameMode.SPECTATOR);
    }

    private void placeMemorial(ServerWorld world, ServerPlayerEntity player) {
        BlockPos start = player.getBlockPos();
        int minY = world.getBottomY();
        BlockPos pos = start;

        // salir de bloques solidos / liquidos
        for (int i = 0; i < 10 && (!world.getBlockState(pos).isReplaceable() || !world.getFluidState(pos).isEmpty()); i++) {
            pos = pos.up();
        }
        // bajar hasta el suelo
        BlockPos ground = pos;
        for (int i = 0; i < 40 && ground.getY() > minY + 1
                && world.getBlockState(ground.down()).isReplaceable()
                && world.getFluidState(ground.down()).isEmpty(); i++) {
            ground = ground.down();
        }
        if (!world.getBlockState(ground.down()).isReplaceable()) {
            pos = ground; // hay suelo cerca
        }

        if (!world.getBlockState(pos).isReplaceable() || !world.getBlockState(pos.up()).isReplaceable()) {
            return; // sin espacio, no se coloca
        }

        world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState());
        int rotation = MathHelper.floor(player.getYaw() * 16.0f / 360.0f + 0.5f) & 15;
        BlockPos headPos = pos.up();
        world.setBlockState(headPos, Blocks.PLAYER_HEAD.getDefaultState().with(SkullBlock.ROTATION, rotation));
        if (world.getBlockEntity(headPos) instanceof SkullBlockEntity skull) {
            skull.setOwner(new ProfileComponent(player.getGameProfile()));
        }
    }
}
