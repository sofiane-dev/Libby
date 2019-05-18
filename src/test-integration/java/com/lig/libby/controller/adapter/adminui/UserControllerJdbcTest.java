package com.lig.libby.controller.adapter.adminui;

import com.lig.libby.Main;
import com.lig.libby.repository.UserRepositoryJdbc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {Main.class})
@ActiveProfiles({"shellDisabled", "springJdbc"})
public class UserControllerJdbcTest extends UserControllerTest {
    @Test
    public void testRepositoryInterfaceImplementationAutowiring() {
        assertThat(super.userRepository instanceof UserRepositoryJdbc
                || AopUtils.getTargetClass(super.userRepository).equals(UserRepositoryJdbc.class)
        ).isTrue();
    }
}
