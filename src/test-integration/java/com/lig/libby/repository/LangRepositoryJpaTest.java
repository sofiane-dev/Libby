package com.lig.libby.repository;

import com.lig.libby.domain.Lang;
import com.lig.libby.repository.common.DataJpaAuditConfig;
import com.lig.libby.repository.common.EntityFactory;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = {DataJpaAuditConfig.class}))
@Import({LangRepository.class, LangRepositoryJpa.class})
@ActiveProfiles({"shellDisabled", "springJpa", "LangRepositoryJpaTest"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LangRepositoryJpaTest {

    private final LangRepositoryTest langRepositoryTest;

    @Autowired
    public LangRepositoryJpaTest(@NonNull LangRepository repository, @NonNull TestEntityManager em, @NonNull EntityFactory<Lang> entityFactoryLang, @NonNull EntityManager em2) {
        langRepositoryTest = new LangRepositoryTest(repository, em, entityFactoryLang, em2);
    }

    @Test
    public void saveAndQueryTest() {
        langRepositoryTest.saveAndQueryTest();
    }

    @Test
    public void updateAndQueryTest() {
        langRepositoryTest.updateAndQueryTest();
    }

    @Test
    public void findWithPredicateTest() {
        langRepositoryTest.findWithPredicateTest();
    }

    @Test
    public void testRepositoryInterfaceImplementationAutowiring() {
        assertThat(langRepositoryTest.repository instanceof LangRepositoryJpa
                || AopUtils.getTargetClass(langRepositoryTest.repository).equals(LangRepositoryJpa.class)
        ).isTrue();
    }

    @Profile("LangRepositoryJpaTest")
    @TestConfiguration
    static class IntegrationTestConfiguration extends LangRepositoryTest.IntegrationTestConfiguration {
    }
}