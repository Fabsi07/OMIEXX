package dev.ceosim.service;

import dev.ceosim.entity.Market;
import dev.ceosim.entity.NpcCompany;
import dev.ceosim.repository.NpcCompanyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Verwaltet alle NPC-Firmen.
 * - Beim Server-Start: 3–4 NPCs pro Startmarkt spawnen falls noch keine da sind
 * - Pro Tick: Auto-Wachstum basierend auf Persönlichkeit
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NpcService {

    private final NpcCompanyRepository npcRepo;
    private static final Random RANDOM = new Random();

    // NPC-Namen pro Markt
    private static final Map<Market, List<String>> NPC_NAMES = Map.of(
        Market.CONSUMER_TECH, List.of(
            "PixelNova", "SwiftByte", "CloudPulse", "NeonLoop", "DriftApp",
            "SparkUI", "FluxTech", "ZenMobile", "WaveOS", "GridAI"
        ),
        Market.ENTERPRISE_SAAS, List.of(
            "CoreSuite", "NexusFlow", "VaultEdge", "ProximaB2B", "SteelSync",
            "OmegaSaaS", "TerraStack", "PivotSoft", "ApexCloud", "BridgeOps"
        ),
        Market.FINTECH, List.of(
            "CoinBridge", "LedgerX", "PayStream", "VaultFi", "NordPay",
            "CapFlow", "TrustLedger", "FusionBank", "ClearFin", "PrimeWallet"
        ),
        Market.E_COMMERCE, List.of(
            "CartNova", "SwiftShop", "NexDrop", "BoltBox", "PrimeCart",
            "UrbanBasket", "FlashStore", "GridMart", "PeakShop", "ZenCart"
        ),
        Market.CYBERSECURITY, List.of(
            "IronShield", "VaultGuard", "ShadowSec", "NullByte", "PhantomOps",
            "FortressCyber", "StealthNet", "CipherLab", "DarkTrace", "ZeroProxy"
        )
    );

    // Persönlichkeiten mit Gewichtung: aggressive 30%, conservative 40%, innovative 30%
    private static final String[] PERSONALITIES = {
        "aggressive", "aggressive", "aggressive",
        "conservative", "conservative", "conservative", "conservative",
        "innovative", "innovative", "innovative"
    };

    // ── Server-Start: NPCs spawnen ────────────────────────────────────────
    @PostConstruct
    @Transactional
    public void spawnInitialNpcs() {
        int spawned = 0;
        for (Market market : Market.values()) {
            if (!market.isStartMarket()) continue;

            long existing = npcRepo.countByMarketAndAcquiredFalse(market);
            int toSpawn = (int) (3 - existing); // mind. 3 pro Markt

            List<String> names = NPC_NAMES.get(market);
            if (names == null) continue;

            for (int i = 0; i < toSpawn; i++) {
                String name = pickUnusedName(names);
                if (name == null) continue;

                String personality = PERSONALITIES[RANDOM.nextInt(PERSONALITIES.length)];
                long startValuation = 500_000L + RANDOM.nextLong(4_500_000L); // $5k–$50k

                NpcCompany npc = NpcCompany.builder()
                        .name(name)
                        .market(market)
                        .personality(personality)
                        .capital(startValuation / 2)
                        .valuation(startValuation)
                        .marketShare(BigDecimal.valueOf(2 + RANDOM.nextInt(8))) // 2–10%
                        .build();

                npcRepo.save(npc);
                spawned++;
            }
        }
        if (spawned > 0) log.info("NPC-Firmen gespawnt: {}", spawned);
    }

    // ── Pro Tick: Alle NPCs wachsen lassen ───────────────────────────────
    @Transactional
    public void tickAllNpcs() {
        List<NpcCompany> npcs = npcRepo.findByAcquiredFalse();
        for (NpcCompany npc : npcs) {
            growNpc(npc);
        }
        npcRepo.saveAll(npcs);
    }

    private void growNpc(NpcCompany npc) {
        double growthRate = switch (npc.getPersonality()) {
            case "aggressive"   -> 0.04 + RANDOM.nextDouble() * 0.04;  // 4–8%
            case "conservative" -> 0.01 + RANDOM.nextDouble() * 0.02;  // 1–3%
            case "innovative"   -> 0.02 + RANDOM.nextDouble() * 0.05;  // 2–7% (volatiler)
            default             -> 0.02;
        };

        // Innovative NPCs haben manchmal Rückschläge
        if ("innovative".equals(npc.getPersonality()) && RANDOM.nextInt(10) < 2) {
            growthRate = -0.02 - RANDOM.nextDouble() * 0.03;
        }

        long newVal = (long) (npc.getValuation() * (1 + growthRate));
        newVal = Math.max(100_000L, newVal); // Minimum $1k Valuation
        npc.setValuation(newVal);
        npc.setCapital((long) (newVal * 0.4));

        // Marktanteil leicht anpassen
        double shareChange = (RANDOM.nextDouble() - 0.45) * 0.5; // ±0.5%
        double newShare = npc.getMarketShare().doubleValue() + shareChange;
        npc.setMarketShare(BigDecimal.valueOf(Math.max(0.5, Math.min(30.0, newShare))));
    }

    // ── Hilfsmethode: unbenutzten Namen finden ──────────────────────────
    private String pickUnusedName(List<String> candidates) {
        List<String> shuffled = new java.util.ArrayList<>(candidates);
        java.util.Collections.shuffle(shuffled);
        return shuffled.stream()
                .filter(n -> !npcRepo.existsByName(n))
                .findFirst()
                .orElse(null);
    }

    // ── Neuen NPC für /admin spawn ───────────────────────────────────────
    @Transactional
    public NpcCompany spawnNpc(Market market, String name) {
        String personality = PERSONALITIES[RANDOM.nextInt(PERSONALITIES.length)];
        NpcCompany npc = NpcCompany.builder()
                .name(name)
                .market(market)
                .personality(personality)
                .capital(1_000_000L)
                .valuation(2_000_000L)
                .marketShare(BigDecimal.valueOf(3.0))
                .build();
        return npcRepo.save(npc);
    }
}
