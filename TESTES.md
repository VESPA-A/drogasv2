# Roteiro de testes (Survival e multiplayer)

Ative `debugCommands` em `config/drogas.json` para os atalhos (`/drogas usar <id>`, `/drogas reset`, `/drogas conhecimento revelar|limpar`).

## Registro e inicialização
1. Abrir o jogo: não deve haver erro no log (`[drogas] Drogas Mod carregado: 17 substâncias, N itens`). Aba criativa "Drogas Mod" com todos os itens e blocos.
2. Mundo novo e mundo da v1.0: carregar sem crash; a Enciclopédia deve estar craftável (livro + papel + pepita de ferro) e a Prancheta Médica também.

## Culturas
3. Plantar cada semente (precisa craftar/achar): maconha/papoula/tabaco/coca/jagube em terra arada; chacrona em terra; **cogumelos só em substrato (terra arada NÃO deve aceitar)**.
4. Observar as 8 idades e o visual de estresse: plantar maconha no escuro (luz < 7) → estresse (amarelo → marrom) → morre. Plantas **não** devem aparecer com fundo preto/branco (CUTOUT).
5. Colher uma planta madura com vigor alto e outra estressada: quantidades e **qualidade** diferentes (tooltip "Qualidade").
6. Jagube: base madura + Treliça/parede ao lado → deve surgir videira acima; botão direito colhe.
7. Estufa: vidro por cima + Estufa Reguladora com carvão e balde d'água → coca fora da selva deve crescer; ao acabar o combustível, volta ao normal.

## Máquinas
8. Colocar cada máquina; abrir; ver painel informativo. Secador ao sol (sem combustível) × à noite (precisa combustível).
9. Receita desconhecida: o painel mostra "reação desconhecida"; ao concluir, a entrada de receita aparece na Enciclopédia.
10. Módulo de velocidade I/II/III: velocidade % sobe; consumo de combustível e desgaste sobem. Nível III: às vezes −1 qualidade.
11. Desgaste: forçar muitas produções → "sujo/gasto/quebrado"; Kit de Manutenção (botão direito na máquina) reduz.
12. Hoppers: hopper acima/lado alimenta; hopper abaixo retira só a saída.

## Descoberta
13. Nova conta/`/drogas conhecimento limpar`: Enciclopédia só tem "Introdução"; entradas aparecem como "???".
14. Ler documento → entradas liberadas e documento consumido; Anotações de campo (estrutura) liberam um grupo.

## Estruturas e loot
15. `/locate structure drogas:abandoned_lab` (idem greenhouse/researcher/depot); visitar: sem blocos flutuando, baús com loot, bloco de Anotações.
16. Abrir baús de calabouços/vilas/templos: itens do mod aparecem raramente; produtos finais só muito raramente.

## Efeitos e multiplayer
17. Usar cada forma (fumar, cheirar, injetar, pílula, bebida, mastigar): animação e som corretos. Efeitos visuais variados; Iris/Sodium ligados sem artefatos.
18. Tolerância sobe e reduz o efeito; dependência → abstinência (HUD pulsa); Kit de Emergência em overdose; sair e entrar do mundo: dados preservados; morrer: tolerância/dependência preservadas.
19. Servidor dedicado + 2 clientes: efeitos, enciclopédia e HUD de cada jogador independentes; máquinas funcionam sem desync.
