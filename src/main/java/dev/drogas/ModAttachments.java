package dev.drogas;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
    private ModAttachments() {}

    /** Tolerancia, dependencia, carga e abstinencia: persistente, copiado na morte e sincronizado com o dono. */
    public static final AttachmentType<DrugState> DRUG_STATE = AttachmentRegistry.create(
            DrogasMod.id("drug_state"),
            builder -> builder
                    .initializer(() -> DrugState.EMPTY)
                    .persistent(DrugState.CODEC)
                    .copyOnDeath()
                    .syncWith(DrugState.STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

    /** Enciclopedia: entradas descobertas (persistente, copiado na morte, sincronizado com o dono). */
    public static final AttachmentType<KnowledgeState> KNOWLEDGE = AttachmentRegistry.create(
            DrogasMod.id("knowledge"),
            builder -> builder
                    .initializer(() -> KnowledgeState.EMPTY)
                    .persistent(KnowledgeState.CODEC)
                    .copyOnDeath()
                    .syncWith(KnowledgeState.STREAM_CODEC, AttachmentSyncPredicate.targetOnly()));

    public static void init() {
        // carregar a classe registra os attachments
    }
}
