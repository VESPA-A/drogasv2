package dev.drogas;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /drogas status | ajuda | usar <substancia> | reset  (usar/reset exigem debugCommands=true no config). */
public final class DrogasCommands {
    private DrogasCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("drogas")
                .executes(ctx -> help(ctx.getSource()))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("ajuda").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource())))
                .then(Commands.literal("conhecimento")
                    .then(Commands.literal("revelar").executes(ctx -> know(ctx.getSource(), true)))
                    .then(Commands.literal("limpar").executes(ctx -> know(ctx.getSource(), false))))
                .then(Commands.literal("usar")
                    .then(Commands.argument("substancia", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Substance.values()).map(s -> s.id), builder))
                        .executes(ctx -> use(ctx.getSource(), StringArgumentType.getString(ctx, "substancia")))))
                .then(Commands.literal("use")
                    .then(Commands.argument("substance", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Substance.values()).map(s -> s.id), builder))
                        .executes(ctx -> use(ctx.getSource(), StringArgumentType.getString(ctx, "substance")))))));
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> Component.translatable("command.drogas.help"), false);
        return 1;
    }

    private static int status(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        DrugState st = DrugManager.state(p);
        src.sendSuccess(() -> Component.translatable("command.drogas.status.header"), false);
        boolean any = false;
        for (Substance s : Substance.values()) {
            boolean active = p.hasEffect(ModEffects.of(s));
            if (st.tol(s) <= 0f && st.dep(s) <= 0f && st.loadOf(s) <= 0f && !active) continue;
            any = true;
            Component state = active ? Component.translatable("gui.drogas.state.active")
                    : st.withdrawalLevel(s) > 0 ? Component.translatable("gui.drogas.state.withdrawal." + st.withdrawalLevel(s))
                    : Component.translatable("gui.drogas.state.clean");
            Component line = Component.translatable("command.drogas.status.line", Component.translatable(s.translationKey()),
                    Math.round(st.tol(s) * 100), Math.round(st.dep(s) * 100), Math.round(st.loadOf(s) / s.odThreshold * 100), state);
            src.sendSuccess(() -> line, false);
        }
        if (!any) src.sendSuccess(() -> Component.translatable("command.drogas.status.empty"), false);
        return 1;
    }

    private static int use(CommandSourceStack src, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!DrogasConfig.get().debugCommands) { src.sendFailure(Component.translatable("command.drogas.debug_off")); return 0; }
        Substance s = Substance.byId(id);
        if (s == null) { src.sendFailure(Component.translatable("command.drogas.unknown", id)); return 0; }
        DrugManager.consume(src.getPlayerOrException(), s, 1.0f);
        src.sendSuccess(() -> Component.translatable("command.drogas.used", Component.translatable(s.translationKey())), false);
        return 1;
    }

    private static int reset(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!DrogasConfig.get().debugCommands) { src.sendFailure(Component.translatable("command.drogas.debug_off")); return 0; }
        DrugManager.reset(src.getPlayerOrException());
        src.sendSuccess(() -> Component.translatable("command.drogas.reset"), false);
        return 1;
    }

    private static int know(CommandSourceStack src, boolean reveal) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!DrogasConfig.get().debugCommands) { src.sendFailure(Component.translatable("command.drogas.debug_off")); return 0; }
        ServerPlayer p = src.getPlayerOrException();
        if (reveal) {
            Knowledge.unlockAll(p);
            src.sendSuccess(() -> Component.translatable("command.drogas.knowledge", KnowledgeData.ALL.size()), false);
        } else {
            p.setAttached(ModAttachments.KNOWLEDGE, KnowledgeState.EMPTY);
            src.sendSuccess(() -> Component.translatable("command.drogas.knowledge_clear"), false);
        }
        return 1;
    }
}
