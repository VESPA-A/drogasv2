"""Conteudo da v2.0: itens novos, documentos, enciclopedia (textos pt/en), receitas novas."""
import art2
from art import hx
M = "minecraft:"

# ---------------------------------------------------------------- itens novos (id, pt, en, imagem, java)
DOC_META = {  # id: (pt, en, cor, [entradas liberadas])
    "doc_field_guide_basic": ("Guia de Campo: Plantio Básico", "Field Guide: Basic Planting", (80, 160, 70, 255),
        ["plant.cannabis", "plant.tobacco", "plant.poppy", "guide.climate", "guide.light_water", "rec.craft.cannabis_seeds", "rec.craft.poppy_seeds", "rec.craft.tobacco_seeds", "rec.craft.rolling_paper"]),
    "doc_mycology_notes": ("Notas de Micologia", "Mycology Notes", (80, 160, 70, 255),
        ["plant.psilocybin", "equip.organic_substrate", "guide.light_water", "rec.craft.psilocybin_spores", "rec.craft.organic_substrate"]),
    "doc_jungle_expedition": ("Diário de Expedição na Selva", "Jungle Expedition Diary", (80, 160, 70, 255),
        ["plant.coca", "plant.jagube", "plant.chacrona", "equip.trellis", "rec.craft.coca_seeds", "rec.craft.jagube_seeds", "rec.craft.chacrona_seeds", "rec.craft.trellis"]),
    "doc_drying_manual": ("Manual do Secador", "Dryer Manual", (130, 140, 165, 255),
        ["equip.dryer", "rec.craft.dryer", "rec.machine.DRYER.dried_cannabis", "rec.machine.DRYER.dried_tobacco", "rec.machine.DRYER.dried_magic_mushroom"]),
    "doc_grinder_manual": ("Manual do Triturador", "Grinder Manual", (130, 140, 165, 255),
        ["equip.grinder", "rec.craft.grinder", "rec.machine.GRINDER.ground_cannabis", "rec.machine.GRINDER.ground_tobacco", "rec.machine.GRINDER.raw_crystal"]),
    "doc_table_manual": ("Manual da Mesa de Processamento", "Processing Table Manual", (130, 140, 165, 255),
        ["equip.processing_table", "rec.craft.processing_table", "rec.machine.TABLE.joint", "rec.machine.TABLE.cigarette", "rec.machine.TABLE.cigar", "rec.machine.TABLE.cannabis_brownie"]),
    "doc_chem_safety": ("Protocolos de Laboratório", "Laboratory Protocols", (235, 190, 60, 255),
        ["equip.chemistry_bench", "rec.craft.chemistry_bench", "rec.machine.BENCH.solvent", "rec.machine.BENCH.chem_base", "rec.machine.BENCH.catalyst", "guide.safety", "guide.overdose"]),
    "doc_opioid_research": ("Pesquisa: Derivados da Papoula", "Research: Poppy Derivatives", (205, 70, 70, 255),
        ["plant.poppy", "sub.opium", "sub.morphine", "sub.heroin", "rec.machine.BENCH.opium_gum", "rec.machine.BENCH.morphine_base", "rec.machine.BENCH.morphine_vial", "rec.machine.BENCH.heroin_dose"]),
    "doc_stimulant_research": ("Pesquisa: Estimulantes", "Research: Stimulants", (205, 70, 70, 255),
        ["sub.cocaine", "sub.crack", "sub.meth", "rec.machine.BENCH.coca_paste", "rec.machine.BENCH.cocaine_powder", "rec.machine.BENCH.crack_rock", "rec.machine.BENCH.meth_shard"]),
    "doc_psychedelic_research": ("Pesquisa: Psicodélicos", "Research: Psychedelics", (205, 70, 70, 255),
        ["sub.lsd", "sub.psilocybin", "sub.dmt", "sub.ayahuasca", "rec.machine.BENCH.lsd_solution", "rec.machine.BENCH.dmt_crystals", "rec.machine.TABLE.lsd_blotter", "rec.machine.TABLE.dmt_dose", "rec.machine.FURNACE.ayahuasca_brew"]),
    "doc_synthetics_lab": ("Relatório do Laboratório: Sintéticos", "Lab Report: Synthetics", (205, 70, 70, 255),
        ["sub.mdma", "sub.ketamine", "sub.nebulon", "sub.zyrax", "rec.machine.TABLE.mdma_pill", "rec.machine.TABLE.nebulon_tab", "rec.machine.BENCH.ketamine_vial", "rec.machine.BENCH.zyrax_vial"]),
    "doc_brewing_notes": ("Notas de Destilaria", "Distillery Notes", (200, 130, 60, 255),
        ["equip.processing_furnace", "rec.craft.processing_furnace", "sub.alcohol", "rec.machine.FURNACE.beer", "rec.machine.FURNACE.wine", "rec.machine.FURNACE.spirit"]),
    "doc_greenhouse_blueprint": ("Projeto de Estufa", "Greenhouse Blueprint", (70, 170, 200, 255),
        ["equip.greenhouse_regulator", "rec.craft.greenhouse_regulator", "guide.greenhouse", "guide.climate"]),
    "doc_maintenance_log": ("Registro de Manutenção", "Maintenance Log", (130, 140, 165, 255),
        ["guide.maintenance", "equip.maintenance_kit", "equip.speed_module", "rec.craft.maintenance_kit", "rec.craft.speed_module_1", "rec.craft.speed_module_2", "rec.craft.speed_module_3"]),
    "doc_quality_study": ("Estudo de Qualidade", "Quality Study", (70, 170, 200, 255),
        ["guide.quality", "equip.organic_fertilizer", "rec.craft.organic_fertilizer"]),
    "doc_addiction_study": ("Relatório Clínico", "Clinical Report", (150, 90, 190, 255),
        ["guide.tolerance", "guide.dependence", "guide.withdrawal", "guide.overdose", "equip.emergency_kit", "equip.detox_tonic", "rec.craft.emergency_kit", "rec.craft.detox_tonic"]),
}
NOTES_META = {  # blocos "Anotações" achados nas estruturas
    "lab": ("Anotações do Laboratório", "Laboratory Notes", (230, 190, 60, 255), ["equip.chemistry_bench", "rec.machine.BENCH.solvent", "guide.safety", "guide.exploration"]),
    "greenhouse": ("Anotações da Estufa", "Greenhouse Notes", (80, 190, 90, 255), ["equip.greenhouse_regulator", "guide.greenhouse", "plant.cannabis", "plant.tobacco"]),
    "researcher": ("Anotações do Pesquisador", "Researcher Notes", (205, 70, 70, 255), ["guide.exploration", "guide.quality", "plant.psilocybin"]),
    "depot": ("Anotações do Depósito", "Depot Notes", (130, 140, 165, 255), ["guide.maintenance", "equip.maintenance_kit", "equip.speed_module"]),
}
NEW_ITEMS = [
    ("speed_module_1", "Módulo de Velocidade I", "Speed Module I", art2.module(1), ("module", 1)),
    ("speed_module_2", "Módulo de Velocidade II", "Speed Module II", art2.module(2), ("module", 2)),
    ("speed_module_3", "Módulo de Velocidade III", "Speed Module III", art2.module(3), ("module", 3)),
    ("maintenance_kit", "Kit de Manutenção", "Maintenance Kit", art2.toolbox(), ("plain", 16)),
    ("organic_fertilizer", "Adubo Orgânico", "Organic Fertilizer", art2.fertilizer(), ("fertilizer",)),
    ("encyclopedia", "Enciclopédia de Substâncias", "Substance Encyclopedia", art2.book(), ("encyclopedia",)),
] + [(i, m[0], m[1], art2.doc(m[2]), ("doc",)) for i, m in DOC_META.items()]
NEW_BLOCK_ITEMS = [("greenhouse_regulator", "Estufa Reguladora", "Greenhouse Regulator"), ("trellis", "Treliça", "Trellis"),
                   ("organic_substrate", "Substrato Orgânico", "Organic Substrate")] + \
                  [(f"field_notes_{k}", v[0], v[1]) for k, v in NOTES_META.items()]
NEW_CRAFTING = [
    ("speed_module_1", 1, [M+"iron_ingot", M+"copper_ingot", M+"redstone", M+"redstone"]),
    ("speed_module_2", 1, ["speed_module_1", M+"gold_ingot", M+"redstone", M+"glowstone_dust"]),
    ("speed_module_3", 1, ["speed_module_2", M+"diamond", M+"redstone", M+"quartz"]),
    ("maintenance_kit", 1, [M+"iron_nugget", M+"iron_nugget", M+"string", M+"paper", M+"coal"]),
    ("greenhouse_regulator", 1, [M+"copper_ingot", M+"copper_ingot", M+"iron_ingot", M+"glass", M+"bucket", M+"redstone"]),
    ("trellis", 2, [M+"stick", M+"stick", M+"stick", M+"stick", M+"string"]),
    ("organic_substrate", 2, [M+"dirt", M+"rotten_flesh", M+"bone_meal", M+"brown_mushroom"]),
    ("organic_fertilizer", 2, [M+"bone_meal", M+"bone_meal", M+"rotten_flesh"]),
    ("encyclopedia", 1, [M+"book", M+"paper", M+"iron_nugget"]),
]
STARTER_RECIPES = ["encyclopedia", "medical_clipboard"]   # liberadas por advancement no primeiro tick
QUALITY_EXCLUDE = {"rolling_paper", "syringe", "maintenance_kit", "organic_fertilizer", "encyclopedia", "medical_clipboard",
                   "emergency_kit", "detox_tonic", "speed_module_1", "speed_module_2", "speed_module_3"}

# ---------------------------------------------------------------- textos da enciclopedia (pt, en)
PLANT_TXT = {
 "cannabis": ("Arbusto de folhas serrilhadas. Prefere clima temperado, bastante luz e terra arada úmida. Plantas vigorosas rendem mais e com melhor qualidade.",
              "Serrated-leaf shrub. Prefers temperate climate, strong light and moist farmland. Vigorous plants yield more and better quality."),
 "poppy": ("Flor de cápsula resistente: aceita clima temperado ou árido e luz média. A cápsula madura é a matéria-prima de toda a linha dos opioides.",
           "Hardy pod flower: tolerates temperate or arid climate and medium light. The ripe pod is the raw material of the whole opioid line."),
 "tobacco": ("Folhas largas que exigem água por perto e muita luz. Depois de colhida, a folha precisa ser curada no secador.",
             "Broad leaves that need water nearby and strong light. After harvest the leaf must be cured in the dryer."),
 "psilocybin": ("Cultura de sombra: cresce em substrato (micélio, podzol, lama, musgo ou Substrato Orgânico), no escuro e com umidade. O sol mata o cultivo.",
                "Shade culture: grows on substrate (mycelium, podzol, mud, moss or Organic Substrate), in the dark and humid. Sunlight kills it."),
 "coca": ("Exigente: luz forte e clima úmido. Fora da selva cresce muito devagar. Numa estufa ativa, qualquer bioma serve.",
          "Demanding: strong light and humid climate. Outside jungles it grows very slowly. In an active greenhouse any biome works."),
 "jagube": ("Trepadeira: a base cresce em terra arada com água por perto e, havendo apoio (treliça, parede ou tronco), sobe formando ramos colhíveis com o botão direito.",
            "Climbing vine: the base grows on farmland near water and, given support (trellis, wall or log), climbs forming branches harvested with right-click."),
 "chacrona": ("Arbusto de meia-sombra que cresce em terra comum e prefere umidade. As folhas são a base do chá e de extratos.",
              "Half-shade shrub that grows on ordinary soil and likes humidity. Its leaves are the basis of the brew and extracts."),
}
EQUIP_TXT = {
 "processing_table": ("Mesa de Processamento", "Processing Table", "Bancada de montagem: combina até 3 ingredientes em produtos prontos (cigarros, brownies, comprimidos, selos). Não usa combustível.", "Assembly bench: combines up to 3 ingredients into finished products. Uses no fuel."),
 "chemistry_bench": ("Bancada Química", "Chemistry Bench", "Reações de laboratório: exige combustível e fica mais lenta quando suja. É onde nascem solventes, bases e intermediários.", "Lab reactions: needs fuel and slows down when dirty. Where solvents, bases and intermediates are made."),
 "dryer": ("Secador", "Dryer", "Seca plantas ao sol (até 50% mais rápido com céu aberto). À noite ou na chuva precisa de combustível no slot dedicado.", "Dries plants in sunlight (up to 50% faster under open sky). At night or in rain it needs fuel in its slot."),
 "grinder": ("Triturador", "Grinder", "Tritura matéria seca e cristais. Não usa combustível, mas o desgaste é rápido: mantenha com um Kit de Manutenção.", "Grinds dry matter and crystals. No fuel, but wear is fast: service it with a Maintenance Kit."),
 "processing_furnace": ("Forno de Processamento", "Processing Furnace", "Fermenta e cozinha: o combustível gasto cresce com a velocidade. Faz cerveja, vinho, cachaça e o chá de ayahuasca.", "Ferments and cooks: fuel use grows with speed. Makes beer, wine, spirit and the ayahuasca brew."),
 "storage_crate": ("Recipiente de Armazenamento", "Storage Crate", "Recipiente com 27 espaços. Itens de qualidades diferentes não se empilham.", "27-slot container. Items of different quality do not stack."),
 "greenhouse_regulator": ("Estufa Reguladora", "Greenhouse Regulator", "Com combustível e água, plantas sob vidro e a até 6 blocos recebem clima ideal, luz mínima e crescimento 50% mais rápido.", "With fuel and water, plants under glass within 6 blocks get ideal climate, minimum light and 50% faster growth."),
 "trellis": ("Treliça", "Trellis", "Apoio para trepadeiras. Vinhas de jagube sobem por qualquer face sólida; a treliça é o jeito mais fácil de oferecer uma.", "Support for climbers. Jagube vines climb any solid face; the trellis is the easiest to provide."),
 "organic_substrate": ("Substrato Orgânico", "Organic Substrate", "Substrato fértil para cogumelos: clima ideal e crescimento mais rápido que micélio ou podzol.", "Fertile mushroom substrate: ideal climate and faster growth than mycelium or podzol."),
 "speed_module": ("Módulos de Velocidade", "Speed Modules", "Melhorias de 3 níveis (x1.5, x2, x3) no slot de melhoria. Aceleram a máquina, mas gastam mais combustível e desgastam mais. No nível III há risco de perder qualidade.", "3-tier upgrades (x1.5, x2, x3) for the upgrade slot. Faster machine, more fuel and wear. Tier III may lower quality."),
 "maintenance_kit": ("Kit de Manutenção", "Maintenance Kit", "Botão direito numa máquina remove 60% do desgaste. Máquinas gastas ficam lentas, produzem pior e por fim param.", "Right-click a machine to remove 60% of its wear. Worn machines slow down, produce worse and finally stop."),
 "emergency_kit": ("Kit de Emergência", "Emergency Kit", "Interrompe overdose ou bad trip e reduz a carga acumulada. Deixa uma fraqueza passageira.", "Stops an overdose or bad trip and reduces accumulated load. Leaves brief weakness."),
 "detox_tonic": ("Tônico Desintoxicante", "Detox Tonic", "Acelera a recuperação: reduz a dependência e a tolerância de todas as substâncias.", "Speeds recovery: lowers dependence and tolerance for every substance."),
 "organic_fertilizer": ("Adubo Orgânico", "Organic Fertilizer", "Use em uma planta: aumenta o vigor e alivia o estresse, melhorando a qualidade da colheita.", "Use on a plant: raises vigor and eases stress, improving harvest quality."),
}
SUB_TXT = {
 "cannabis": ("Relaxante de efeito suave e longo. Regeneração leve em troca de lentidão e fome.", "Mild, long relaxant. Light regeneration in exchange for slowness and hunger."),
 "cocaine": ("Estimulante curto e agressivo: velocidade e mineração mais rápidas; coração em disparada e tolerância veloz.", "Short aggressive stimulant: faster movement and mining; racing heart and fast tolerance."),
 "crack": ("Versão fumada e intensa: pico curtíssimo, dependência altíssima e risco alto de overdose.", "Smoked, intense version: very short peak, extreme dependence and high overdose risk."),
 "lsd": ("Psicodélico de longa duração: distorção visual e alcance ampliado. Sem dependência, mas com tolerância muito rápida.", "Long-lasting psychedelic: visual distortion and extended reach. No dependence, very fast tolerance."),
 "psilocybin": ("Cogumelo de visão ondulante e percepção aumentada; náusea no começo.", "Mushroom with wavy vision and heightened perception; nausea at the start."),
 "opium": ("Resina sedativa: armadura e regeneração em troca de lentidão e dependência moderada.", "Sedative resin: armor and regeneration for slowness and moderate dependence."),
 "morphine": ("Analgésico forte: maior proteção e regeneração, com dependência real.", "Strong painkiller: better protection and regeneration, with real dependence."),
 "heroin": ("Proteção máxima e visão em túnel; dependência altíssima e abstinência severa.", "Maximum protection and tunnel vision; extreme dependence and severe withdrawal."),
 "meth": ("Estimulante prolongado: velocidade, dano e mineração; fome intensa e 'crash' na abstinência.", "Long stimulant: speed, damage and mining; intense hunger and a 'crash' in withdrawal."),
 "mdma": ("Entactógeno: euforia, velocidade leve e regeneração; o corpo cobra em fome e calor.", "Entactogen: euphoria, light speed and regeneration; the body charges hunger and heat."),
 "ketamine": ("Dissociativo: queda segura e visão em túnel; lento e perigoso se misturado a depressores.", "Dissociative: safe falls and tunnel vision; slow and dangerous mixed with depressants."),
 "dmt": ("Experiência breve e extrema: quase imobiliza. Altíssimo risco de bad trip.", "Brief extreme experience: nearly immobilizes. Very high bad-trip risk."),
 "ayahuasca": ("Chá longo: purga com náusea, depois visão noturna e alcance ampliado.", "Long brew: nausea purge, then night vision and extended reach."),
 "tobacco": ("Estímulo leve para mineração e ataque; cria dependência moderada.", "Mild boost to mining and attack; moderate dependence."),
 "alcohol": ("Dano e resistência a empurrão, mas cambaleio. Cada dose soma; mistura perigosa com opioides.", "Damage and knockback resistance, but staggering. Doses add up; dangerous with opioids."),
 "nebulon": ("Sintética fictícia: velocidade, pulo alto e queda segura; fome e tremor.", "Fictional synthetic: speed, high jump and safe falls; hunger and tremor."),
 "zyrax": ("Sintética fictícia de tanque: armadura e dano altos, mas muito lento.", "Fictional tank synthetic: high armor and damage, but very slow."),
}
GUIDE_TXT = {
 "intro": ("Introdução", "Introduction", "Esta enciclopédia só registra o que você descobre. Explore estruturas abandonadas, leia documentos, cultive, processe e use. Tudo é fictício e feito para o jogo.", "This encyclopedia only records what you discover. Explore abandoned structures, read documents, grow, process and use. Everything is fictional and game-only."),
 "climate": ("Clima e Biomas", "Climate and Biomes", "Cada planta tem climas ideais e tolerados (úmido, temperado, árido, frio). Fora deles ela estressa e pode morrer. Estufas ignoram o clima.", "Each plant has ideal and tolerated climates (humid, temperate, arid, cold). Outside them it stresses and may die. Greenhouses ignore climate."),
 "light_water": ("Luz e Água", "Light and Water", "Fora da faixa de luz a planta para de crescer. Sem água por perto (ou com terra seca) ela perde vigor. Água por perto e terra úmida ajudam.", "Outside its light range a plant stops growing. Without nearby water (or on dry soil) it loses vigor."),
 "quality": ("Qualidade", "Quality", "A qualidade (Ruim, Padrão, Boa, Excelente) vem do vigor da planta e do estado da máquina. Melhor qualidade = efeito mais forte, longo e limpo; pior = impurezas e overdose mais fácil.", "Quality (Poor, Standard, Good, Excellent) comes from plant vigor and machine condition. Better = stronger, longer, cleaner; worse = impurities and easier overdose."),
 "tolerance": ("Tolerância", "Tolerance", "Usar a mesma substância repetidamente reduz seu efeito (até 75%). A tolerância cai aos poucos quando você fica limpo.", "Repeated use of a substance reduces its effect (up to 75%). Tolerance slowly falls while you stay clean."),
 "dependence": ("Dependência", "Dependence", "Substâncias viciantes geram dependência. Quanto maior, mais cedo e mais forte é a abstinência quando você para.", "Addictive substances build dependence. The higher it is, the earlier and stronger withdrawal hits when you stop."),
 "withdrawal": ("Abstinência", "Withdrawal", "Tem 3 níveis e sintomas próprios de cada substância. Usar alivia, mas realimenta o ciclo; atravessar o período reduz a dependência aos poucos.", "Has 3 levels and substance-specific symptoms. Using relieves it but feeds the cycle; enduring it slowly lowers dependence."),
 "overdose": ("Overdose e Bad Trip", "Overdose and Bad Trip", "Cada dose soma carga que decai com o tempo. Passar do limite causa overdose (dano) ou bad trip. Opioides + álcool e estimulantes entre si somam risco. Use o Kit de Emergência.", "Each dose adds load that decays over time. Passing the limit causes overdose (damage) or a bad trip. Opioids + alcohol and stimulants stack risk. Use the Emergency Kit."),
 "maintenance": ("Manutenção", "Maintenance", "Máquinas se desgastam a cada produção: sujas ficam lentas, gastas produzem pior, quebradas param. Use o Kit de Manutenção.", "Machines wear with each product: dirty ones slow down, worn ones produce worse, broken ones stop. Use the Maintenance Kit."),
 "greenhouse": ("Estufas", "Greenhouses", "Estufa funcional = vidro por cima + Estufa Reguladora ativa (combustível e água) a até 6 blocos. Dá clima ideal, luz mínima e crescimento mais rápido.", "A working greenhouse = glass overhead + an active Greenhouse Regulator (fuel and water) within 6 blocks. Ideal climate, minimum light, faster growth."),
 "exploration": ("Exploração", "Exploration", "Laboratórios, estufas, casas de pesquisadores e depósitos abandonados guardam sementes, materiais, equipamentos e documentos. As anotações de campo revelam entradas.", "Abandoned labs, greenhouses, researcher houses and depots hold seeds, materials, equipment and documents. Field notes reveal entries."),
 "safety": ("Segurança", "Safety", "Tudo é fictício, mas o jogo pune abuso: espace as doses, vigie a carga e leve um Kit de Emergência.", "Everything is fictional, but the game punishes abuse: space your doses, watch your load and carry an Emergency Kit."),
}
