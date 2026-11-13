# Drogas Mod 2.0 — Minecraft Java 1.21.11 (Fabric)

Mod de **jogabilidade**: 17 substâncias fictícias, 7 culturas, máquinas, estruturas, enciclopédia com descoberta, qualidade
variável, tolerância/dependência/abstinência/overdose. Todas as receitas são fictícias e usam só itens do jogo.

> ## ⚠️ Estado desta entrega (leia primeiro)
> O código foi escrito **sem acesso a compilador do Minecraft nem ao jogo**. Portanto **nada foi compilado nem executado**,
> e **não há JAR pronto**. O que foi feito para reduzir o risco: as APIs sensíveis do 1.21.11 foram conferidas na documentação
> oficial do Fabric (blocos/entidades, menus, efeitos, data attachments, HUD, data components, sons, loot, transparência);
> todo o Java passa na análise sintática; `tools/verify.py` faz **864 verificações cruzadas** (código × texturas × modelos ×
> blockstates × lang pt/en × receitas × loot × sons × estruturas) — todas passam. **Isso não substitui testar no jogo.**
> Veja `TESTES.md` (roteiro de teste) e a seção "O que não foi testado" abaixo.

## Como gerar o JAR
1. **GitHub Actions** (sem instalar nada): suba a pasta num repositório; o workflow `build` gera o JAR (artefato `drogas-mod-jar`).
2. **Local:** JDK 21 + Gradle 9.2+: `gradle wrapper --gradle-version 9.2.1` e `./gradlew build` → `build/libs/drogas-2.0.0.jar`.
3. **IntelliJ:** abrir como projeto Gradle (JDK 21) e rodar `build`.
Confira versões em https://fabricmc.net/develop se o Gradle reclamar (`gradle.properties`). Instale Fabric Loader + Fabric API 1.21.11;
servidor **e** clientes precisam do mod. Compatível com Sodium/Iris (efeitos 2D no HUD, sem shaders próprios).

## O que há de novo na 2.0

### 1. Exploração e loot
- **Baús vanilla** (calabouços, minas, pirâmides, templos, vilas, naufrágios, mansões, fortalezas, bastiões, cidades antigas, end cities…)
  recebem itens do mod via `LootTableEvents.MODIFY`. Raridade (`tools/tiers.py` → `LootData.java`):
  **A comum** sementes básicas (maconha, tabaco, papoula, esporos) · **B incomum** sementes difíceis (coca, jagube, chacrona), kits, treliça, substrato, manuais ·
  **C rara** intermediários (solvente, base, catalisador, cristal bruto, pasta de coca…), módulo II, estufa reguladora, pesquisas · **D muito rara** produtos finais, bancada química, forno, módulo III.
- **4 estruturas próprias** (jigsaw + NBT, geração natural): *Laboratório Abandonado*, *Estufa Abandonada* (com culturas selvagens, trepadeira e reguladora vazia),
  *Casa de Pesquisador* e *Depósito de Equipamentos*. Cada uma tem baú com loot próprio (`data/drogas/loot_table/chests/`) e um bloco de **Anotações de campo**.
  Teste com `/locate structure drogas:abandoned_lab` (ou `abandoned_greenhouse`, `researcher_house`, `equipment_depot`).

### 2. Agricultura reformulada
- Cada planta tem **8 idades com visual próprio** × **3 níveis de estresse** (viçosa/amarelada/murcha) e **vigor** (0–3).
- **Ambiente** (`FarmEnv`): luz (faixa por planta), água/umidade da terra, **clima por bioma** (tags `drogas:climate/humid|temperate|arid|cold`, editáveis por datapack),
  substrato e **estufa**. Ideal → cresce mais e ganha vigor; ruim → perde vigor; péssimo → estresse e, no fim, **a planta morre** (pode devolver uma semente).
- **Colheita variável e qualidade:** o loot depende de vigor/estresse (1–4 unidades) e marca a **qualidade** (Ruim/Padrão/Boa/Excelente).
- **Cogumelos não usam terra arada:** crescem em substrato (micélio, podzol, lama, musgo, terra, *Substrato Orgânico*), no escuro (sol mata) e com umidade.
- **Jagube trepadeira:** a base amadurece e lança uma videira que **sobe por paredes, troncos ou Treliça** (até 6 blocos); colhe-se com botão direito (volta ao estágio 1).
- **Estufa funcional:** vidro por cima + *Estufa Reguladora* ativa (combustível + balde d'água) a até 6 blocos = clima ideal, luz mínima e crescimento 50% mais rápido.
- **Adubo Orgânico** (+vigor, −estresse). Ambientes: campo, sombra (chacrona), caverna/porão (cogumelos), selva/pântano (coca, jagube), estufa (tudo).

### 3. Máquinas
Cada máquina mantém função clara: **Mesa** (montagem, sem combustível) · **Bancada Química** (reações, usa combustível) · **Secador** (grátis ao sol, +50%; de noite/chuva usa combustível) ·
**Triturador** (sem combustível, desgaste alto) · **Forno** (fermentar/cozinhar, combustível). Todas têm: **barra de progresso**, **slot de melhoria** (Módulos I/II/III = ×1,5/×2/×3, com mais combustível e desgaste;
nível III arrisca qualidade), **desgaste/manutenção** (sujo −15%, gasto −35% e −1 qualidade, quebrado para; *Kit de Manutenção* com botão direito remove 60%), **animação** (textura animada,
partículas e sons de trabalho), **painel informativo** (receita, estado, tempo restante, velocidade, desgaste, combustível) e **hoppers** (entram pelos lados/topo, saída por baixo).

### 4. Progressão e descobertas — Enciclopédia de Substâncias
Item craftável desde o início (receita liberada por advancement). **Só mostra o que você descobriu** (plantas, equipamentos, substâncias, receitas, guias) + aba *Condição* + *Opções*.
Descoberta por: **documentos** achados em baús (16 tipos), **Anotações de campo** das estruturas, **uso** (substância), **colheita** (planta), **abrir a máquina** (equipamento),
**experimentar na máquina** (a receita concluída é registrada), possuir um equipamento/semente (engenharia reversa) e eventos de jogo (primeira overdose, abstinência, tolerância…).
`/drogas conhecimento revelar|limpar` (requer `debugCommands`) serve para testes.

### 5. Efeitos e qualidade
- **Qualidade** (componente `drogas:quality`): Ruim = −30% potência, impurezas (náusea/fome) e +25% carga de overdose; Excelente = +30% potência, +25% duração, −10% carga. Mostra no tooltip.
- **Estilos visuais por substância** (2D, Sodium/Iris): faixas coloridas em túnel (LSD), hiperespaço (DMT), ondulação (cogumelos/ayahuasca), "pálpebras" (opioides), linhas de varredura (meth), grade neon (Nebulon),
  imagem dupla (álcool/cetamina), batida nas laterais (MDMA), flashes (cocaína/crack), barras de ferro (Zyrax) + FOV e balanço de câmera.
- **Animações de consumo por forma:** pílula, bebida, fumar (animação de corneta + fumaça), cheirar, injetar, mastigar; **26 sons próprios** (.ogg sintetizados) para itens, máquinas, efeitos e UI.
- **Condição:** HUD com ícone/tempo/nível de cada efeito, "sinais vitais" (tolerância amarela, dependência vermelha, 10 segmentos), linha de abstinência tremendo/pulsando e batimento nas bordas da tela.

### 6. Estabilidade
Dados do jogador em *data attachments* persistentes/copiados na morte/sincronizados só com o dono; efeitos são `MobEffect` (sync nativo); lógica de máquinas no servidor; falhas de injeção de mixin são ignoradas (`require = 0`).
**Correção da v1.0:** plantas/trepadeira/treliça agora são registradas na camada `CUTOUT` (sem isso apareceriam com fundo opaco) e `noCollission()` → `noCollision()`.

## Configuração (`config/drogas.json`)
`durationScale`, `substanceDurationScale`, `intensityScale`, `toleranceRate`, `dependenceRate`, `recoveryRate`, `overdoseLethal`, `debugCommands`, `hud`, `cameraSway`, `fovEffects`,
`hallucinationParticles`, `visualIntensity`. As opções **visuais** também estão na aba *Opções* da Enciclopédia (salvam sozinhas).

## O que não foi testado / limites (honestidade)
- **Compilação e execução:** nunca rodaram. Pontos mais prováveis de ajuste: `ItemTooltipCallback` (cliente), construtor público de `MenuType` (`ModMenus`), `Consumable.Builder` e `ItemUseAnimation` (`ModItems`),
  `VineBlock` (`JagubeClimberBlock`: `PROPERTY_BY_DIRECTION`), `DrugState.STREAM_CODEC`, `LootInjector`, mixin `getFov`.
- **Estruturas:** NBT/JSON validados estruturalmente (leitura de volta do NBT), mas **nunca geradas num mundo**: posição, rotação, adaptação ao terreno e frequência precisam de ajuste fino (`tools/structures.py`).
- **Multiplayer:** projetado com lógica no servidor e sincronização por attachments/MobEffect; não foi testado com 2+ jogadores.
- **Balanceamento** (tempos, desgaste, taxas de loot, crescimento) é estimativa; ajuste em `Substance.java`, `CropSpec.java`, `tools/tiers.py`.
- **Desfoque real** não existe (exigiria shader de pós-processamento, que conflita com Iris): é simulado por tint/vinheta/estilos/balanço.
- Sons são **sintetizados** (qualidade de protótipo); texturas são pixel-art procedural.
- Overdose usa dano "mágico" (sem tipo de dano/mensagem de morte próprios). Descoberta por *entrar numa estrutura* foi trocada por **Anotações de campo** (mais robusta que consultar o StructureManager).
- Não há integração com ModMenu; as opções ficam na Enciclopédia. Receitas vanilla não aparecem no livro de receitas (exceto as iniciais) — é proposital.
- Máquinas quebradas por **explosão** perdem o conteúdo (só quebrar com a mão solta os itens).

## Estrutura
```
src/main/java/dev/drogas/            código comum (+ client/ HUD, telas, estilos; mixin/client/)
  *Data.java, ItemCatalog, MachineRecipes, SoundCatalog   GERADOS por tools/generate2.py
src/main/resources/                  fabric.mod.json, assets (texturas, modelos, sons, lang pt_br/en_us), data (receitas, loot, tags, estruturas)
tools/                               generate2.py (orquestrador), generate.py (base), art*.py, sounds.py, structures.py, tiers.py, content.py, verify.py
```
Para regenerar tudo: `python3 tools/generate2.py` (precisa de Pillow, numpy, scipy e ffmpeg); para verificar: `python3 tools/verify.py`.
