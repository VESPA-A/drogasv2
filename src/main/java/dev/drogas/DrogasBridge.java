package dev.drogas;

import java.util.function.IntConsumer;

/** Ponte entre codigo comum e codigo de cliente (evita carregar classes de cliente no servidor). */
public final class DrogasBridge {
    private DrogasBridge() {}

    /** Definido pelo cliente: abre a Enciclopedia na aba indicada (0 plantas ... 5 condicao). */
    public static IntConsumer openEncyclopedia = null;
}
