package cc.openstrata.platform.application;

import cc.openstrata.platform.infrastructure.adapter.InMemorySrsAdapter;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SrsAppServiceTest {
    private final InMemorySrsAdapter adapter = new InMemorySrsAdapter();
    private final SrsAppService svc = new SrsAppService(adapter);

    @Test
    void bindThenListBound() {
        adapter.registerSkill("s1");
        svc.bindSkill("a1", "s1");
        assertEquals(List.of("s1"), svc.boundSkills("a1"));
    }

    @Test
    void listSkillsReturnsRegistered() {
        adapter.registerSkill("s1");
        assertTrue(svc.listSkills().contains("s1"));
    }
}
