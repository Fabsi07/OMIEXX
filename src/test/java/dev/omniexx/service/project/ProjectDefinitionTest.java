package dev.omniexx.service.project;

import dev.omniexx.entity.Market;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ProjectDefinitionTest {

    @ParameterizedTest
    @EnumSource(value = Market.class, names = {"CONSUMER_TECH","ENTERPRISE_SAAS","FINTECH","E_COMMERCE","CYBERSECURITY"})
    void jederStartmarktHatMindestensSechsProjekte(Market market) {
        List<ProjectDefinition> projects = ProjectDefinition.forMarket(market);
        assertThat(projects)
                .as("Markt %s sollte mindestens 6 Projekte haben", market)
                .hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void alleProjectKeysSindEindeutig() {
        List<String> allKeys = ProjectDefinition.ALL.values().stream()
                .flatMap(List::stream)
                .map(ProjectDefinition::getKey)
                .toList();
        assertThat(allKeys).doesNotHaveDuplicates();
    }

    @Test
    void alleProjecteHabenPositiveKostenUndDauer() {
        ProjectDefinition.ALL.values().stream()
                .flatMap(List::stream)
                .forEach(p -> {
                    assertThat(p.getCost())
                            .as("Projekt %s: Kosten müssen positiv sein", p.getKey())
                            .isPositive();
                    assertThat(p.getDurationTicks())
                            .as("Projekt %s: Dauer muss positiv sein", p.getKey())
                            .isPositive();
                    assertThat(p.getName())
                            .as("Projekt %s: Name darf nicht leer sein", p.getKey())
                            .isNotBlank();
                });
    }

    @Test
    void byKeyGibtKorrektesProjectZurueck() {
        ProjectDefinition def = ProjectDefinition.byKey("cs_zero_trust");
        assertThat(def).isNotNull();
        assertThat(def.getMarket()).isEqualTo(Market.CYBERSECURITY);
        assertThat(def.getCost()).isPositive();
    }

    @Test
    void byKeyGibtNullFuerUnbekanntenKey() {
        assertThat(ProjectDefinition.byKey("gibts_nicht")).isNull();
    }
}
