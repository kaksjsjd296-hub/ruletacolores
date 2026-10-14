package com.ruleta.colores.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ruleta.colores.RuletaColores;
import com.ruleta.colores.network.AnimConfig;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Ajustes de la animacion guardados en el cliente del operador (config/ruleta_colores_anim.json). */
public final class AnimSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AnimConfig current = null;

    private AnimSettings() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("ruleta_colores_anim.json");
    }

    public static AnimConfig current() {
        if (current == null) {
            current = AnimConfig.defaults();
            try {
                Path f = file();
                if (Files.exists(f)) {
                    try (Reader r = Files.newBufferedReader(f)) {
                        AnimConfig loaded = GSON.fromJson(r, AnimConfig.class);
                        if (loaded != null) current = loaded.clamped();
                    }
                }
            } catch (Exception e) {
                RuletaColores.LOGGER.warn("No se pudo leer la configuracion de animacion", e);
            }
        }
        return current;
    }

    public static void set(AnimConfig config) {
        current = config.clamped();
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(current, w);
        } catch (Exception e) {
            RuletaColores.LOGGER.warn("No se pudo guardar la configuracion de animacion", e);
        }
    }
}
