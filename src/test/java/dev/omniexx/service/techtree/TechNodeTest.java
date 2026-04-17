package dev.omniexx.service.techtree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TechNodeTest {

    @Test
    void genau15NodesVorhanden() {
        assertThat(TechNode.values()).hasSize(15);
    }

    @Test
    void jederPillarHatGenau5Nodes() {
        for (TechNode.Pillar pillar : TechNode.Pillar.values()) {
            List<TechNode> nodes = TechNode.byPillar(pillar);
            assertThat(nodes).as("Pillar %s sollte 5 Nodes haben", pillar).hasSize(5);
        }
    }

    @Test
    void alleKeysSindEindeutig() {
        List<String> keys = Arrays.stream(TechNode.values()).map(TechNode::getKey).toList();
        assertThat(keys).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @EnumSource(TechNode.class)
    void alleNodeHabenPositiveKosten(TechNode node) {
        assertThat(node.getCost()).isPositive();
        assertThat(node.getRpCost()).isPositive();
        assertThat(node.getName()).isNotBlank();
    }

    @Test
    void byKeyFindetKorrektNode() {
        TechNode node = TechNode.byKey("ops_process_automation");
        assertThat(node).isNotNull();
        assertThat(node.getPillar()).isEqualTo(TechNode.Pillar.OPERATIONS);
        assertThat(node.getTier()).isEqualTo(1);
    }
}
