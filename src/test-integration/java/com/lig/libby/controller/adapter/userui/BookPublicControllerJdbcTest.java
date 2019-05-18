package com.lig.libby.controller.adapter.userui;

import com.lig.libby.Main;
import com.lig.libby.controller.adapter.adminui.BookControllerTest;
import com.lig.libby.repository.BookRepositoryJdbc;
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
public class BookPublicControllerJdbcTest extends BookPublicControllerTest {
    @Test
    public void testRepositoryInterfaceImplementationAutowiring() {
        assertThat(super.bookRepository instanceof BookRepositoryJdbc
                || AopUtils.getTargetClass(super.bookRepository).equals(BookRepositoryJdbc.class)
        ).isTrue();
    }
}
