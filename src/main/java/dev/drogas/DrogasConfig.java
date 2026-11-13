package dev.drogas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/** Configuracao em config/drogas.json (criada automaticamente na primeira execucao). */
public final class DrogasConfig {
    // ---- servidor / gameplay
    /** Multiplicador global de duracao dos efeitos. */
    public float durationScale = 1.0f;
    /** Multiplicador global da intensidade (nivel) dos efeitos. */
    public float intensityScale = 1.0f;
    public float toleranceRate = 1.0f;
    public float dependenceRate = 1.0f;
    /** Velocidade de recuperacao (queda de tolerancia/dependencia). */
    public float recoveryRate = 1.0f;
    /** Se false, a overdose nunca mata (deixa o jogador com 1 de vida). */
    public boolean overdoseLethal = true;
    /** Libera /drogas usar e /drogas reset para qualquer jogador (para testes). */
    public boolean debugCommands = false;
    /** Multiplicador de duracao por substancia (id -> fator). */
    public Map<String, Float> substanceDurationScale = new LinkedHashMap<>();
    // ---- cliente / visual
    public boolean hud = true;
    public boolean cameraSway = true;
    public boolean fovEffects = true;
    public boolean hallucinationParticles = true;
    /** 0 desliga os efeitos visuais; 1 e o padrao; 2 dobra a intensidade. */
    public float visualIntensity = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DrogasConfig instance = new DrogasConfig();

    public static DrogasConfig get() {
        return instance;
    }

    public float durationFor(Substance s) {
        return Math.max(0.05f, durationScale * substanceDurationScale.getOrDefault(s.id, 1.0f));
    }

    public static void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("drogas.json");
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            DrogasMod.LOGGER.warn("Não foi possível salvar {}", path, e);
        }
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("drogas.json");
        DrogasConfig cfg = null;
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                cfg = GSON.fromJson(r, DrogasConfig.class);
            } catch (IOException | RuntimeException e) {
                DrogasMod.LOGGER.error("Não foi possível ler {}; usando padrões.", path, e);
            }
        }
        if (cfg == null) cfg = new DrogasConfig();
        if (cfg.substanceDurationScale == null) cfg.substanceDurationScale = new LinkedHashMap<>();
        for (Substance s : Substance.values()) cfg.substanceDurationScale.putIfAbsent(s.id, 1.0f);
        instance = cfg;
        try (Writer w = Files.newBufferedWriter(path)) {
            GSON.toJson(cfg, w);
        } catch (IOException e) {
            DrogasMod.LOGGER.warn("Não foi possível salvar {}", path, e);
        }
    }
}
