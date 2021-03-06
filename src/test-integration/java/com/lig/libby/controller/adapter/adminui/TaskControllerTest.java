package com.lig.libby.controller.adapter.adminui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lig.libby.Main;
import com.lig.libby.controller.adapter.anonymousui.AuthControllerTest;
import com.lig.libby.controller.adapter.anonymousui.dto.LoginRequestDto;
import com.lig.libby.core.TestUtil;
import com.lig.libby.core.TestUtil.TestArgs;
import com.lig.libby.domain.*;
import com.lig.libby.domain.core.PersistentObject;
import com.lig.libby.repository.*;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource(properties = {"spring.batch.job.enabled=false"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {Main.class})
public class TaskControllerTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    public TaskRepository taskRepository;

    @Autowired
    private BookRepository bookRepository;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private javax.servlet.Filter springSecurityFilterChain;


    @BeforeEach
    public void setup() {
        TestUtil.setAuthenticationForCurrentThreadLocal(authenticationManager, "admin@localhost", "admin");
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test
    @Transactional
    public void findOneTest() throws Exception {
        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
        Lang lang = TestUtil.createAndSaveLang(langRepository);
        Work work = TestUtil.createAndSaveWork(workRepository);
        Task task = TestUtil.createAndSaveTask(user, lang, work, taskRepository);

        Task assertResponce = new Task();
        assertResponce.setId(task.getId());

        assertResponce.setBookName(task.getBookName());

        Lang langDto = new Lang();
        langDto.setId(task.getBookLang().getId());
        assertResponce.setBookLang(langDto);

        Work workDto = new Work();
        workDto.setId(task.getBookWork().getId());
        assertResponce.setBookWork(workDto);

        assertResponce.setBookTitle(task.getBookTitle());
        assertResponce.setWorkflowStep(task.getWorkflowStep());
        assertResponce.setVersion(task.getVersion());

        User userDto = new User();
        userDto.setId(task.getCreatedBy().getId());
        assertResponce.setCreatedBy(userDto);
        assertResponce.setLastUpdBy(userDto);

        User assertDto = new User();
        assertDto.setId(task.getAssignee().getId());
        assertResponce.setAssignee(assertDto);

        User admin = userRepository.findByEmail("admin@localhost").orElse(null);
        //fields that assigned in dto in other layers
        assertResponce.setAvailableCommands(Arrays.asList(Task.WorkflowStepEnum.SUBMITTED.name()));
        Task response = TestUtil.getDtoByIdByUser(admin, "admin", task, "/tasks/", mockMvc, Task.class);

        assertAll(
                () -> assertThat(assertResponce).isEqualToComparingFieldByField(response)
        );
    }


    @Transactional
    @TestFactory
    Stream<DynamicTest> dynamicFindAllTest() {
        List<TestArgs<User, String, String, List<Task>, String, String, String>> inputList;
        {
            User admin = userRepository.findByEmail("admin@localhost").orElse(null);
            User user1 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
            User user2 = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);

            Lang task12Lang = TestUtil.createAndSaveLang(langRepository);
            Work task13Work = TestUtil.createAndSaveWork(workRepository);
            TestUtil.setAuthenticationForCurrentThreadLocal(authenticationManager, user1.getEmail(), "test");
            Task task1 = TestUtil.createAndSaveTask(user1, task12Lang, task13Work, taskRepository);

            TestUtil.setAuthenticationForCurrentThreadLocal(authenticationManager, user2.getEmail(), "test");
            Task task2 = TestUtil.createAndSaveTask(user2, task12Lang, TestUtil.createAndSaveWork(workRepository), taskRepository);
            Task task3 = TestUtil.createAndSaveTask(user2, TestUtil.createAndSaveLang(langRepository), task13Work, taskRepository);

            inputList = Arrays.asList(
                    //search all && role access
                    new TestArgs<>(admin, "admin", "/tasks/",
                            Arrays.asList(task1, task2, task3), null, null, null),

                    //search by child Entity field & role access
                    new TestArgs<>(admin, "admin", "/tasks/?bookLang.id=" + task12Lang.getId(),
                            Arrays.asList(task1, task2), null, null, null),

                    //search by child Entity field & role access
                    new TestArgs<>(admin, "admin", "/tasks/?bookWork.id=" + task13Work.getId(),
                            Arrays.asList(task1, task3), null, null, null),

                    //search by value field
                    new TestArgs<>(admin, "admin", "/tasks/?id=" + task1.getId(),
                            Arrays.asList(task1), null, null, null),

                    //search by value field with OR
                    new TestArgs<>(admin, "admin", "/tasks/?id=" + task1.getId() + "&id=" + task2.getId(),
                            Arrays.asList(task1, task2), null, null, null)
            );
        }

        return inputList.stream()
                .map(test -> DynamicTest.dynamicTest(
                        " When call url: " + test.getArgC() +
                                " by user: " + test.getArgA().getEmail() +
                                " loggined with password: " + test.getArgB() +
                                " then see only tasks: " + test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.joining(",")),
                        () -> {
                            String responsePageJson = TestUtil.getDtoPageByQueryParamsByUser(test.getArgA(), test.getArgB(), test.getArgC(), mockMvc);
                            TestUtil.HelperPage<Task> responsePage = new ObjectMapper().readerFor(new TypeReference<TestUtil.HelperPage<Task>>() {
                            }).readValue(responsePageJson);
                            assertAll(
                                    () -> assertThat(responsePage.stream().map(PersistentObject::getId).collect(Collectors.toList())).
                                            containsExactlyInAnyOrderElementsOf(test.getArgD().stream().map(PersistentObject::getId).collect(Collectors.toList()))
                            );
                        }
                        )
                );
    }


    @Test
    @Transactional
    void create() throws Exception {
        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
        Lang lang = TestUtil.createAndSaveLang(langRepository);
        Work work = TestUtil.createAndSaveWork(workRepository);

        Task requestDto = new Task();

        Lang langDto = new Lang();
        langDto.setId(lang.getId());
        requestDto.setBookLang(langDto);

        Work workDto = new Work();
        workDto.setId(work.getId());
        requestDto.setBookWork(workDto);

        requestDto.setId("test-task-" + UUID.randomUUID().toString().replaceAll("-", ""));
        requestDto.setBookName("book-name-" + requestDto.getId());
        requestDto.setBookTitle("book-title-" + requestDto.getId());

        User admin = userRepository.findByEmail("admin@localhost").orElse(null);
        Task responseDto = TestUtil.postDtoByUser(admin, "admin", requestDto, "/tasks/", mockMvc, Task.class);

        //copy request and enrich with fields that backend should set itself
        Task expectedResponseDTO = (Task) SerializationUtils.clone(requestDto);
        expectedResponseDTO.setVersion(0);

        User userDto = new User();
        userDto.setId(admin.getId());
        expectedResponseDTO.setAssignee(userDto);

        expectedResponseDTO.setCreatedBy(userDto);
        expectedResponseDTO.setLastUpdBy(userDto);
        expectedResponseDTO.setWorkflowStep(Task.WorkflowStepEnum.INIT);
        expectedResponseDTO.setAvailableCommands(Arrays.asList(Task.WorkflowStepEnum.SUBMITTED.name()));

        //copy responce fields we cannot predict
        expectedResponseDTO.setCreatedDate(responseDto.getCreatedDate());
        expectedResponseDTO.setUpdatedDate(responseDto.getUpdatedDate());


        assertAll(
                () -> assertThat(responseDto).isEqualToComparingFieldByField(expectedResponseDTO)
        );

    }

    @Test
    @Transactional
    void update() throws Exception {
        User admin = userRepository.findByEmail("admin@localhost").orElse(null);
        User user = TestUtil.createAndSaveUserWithAdminRole(passwordEncoder, authorityRepository, userRepository);
        Lang lang = TestUtil.createAndSaveLang(langRepository);
        Work work = TestUtil.createAndSaveWork(workRepository);

        //пользователь создает заявку на новую книгу
        TestUtil.setAuthenticationForCurrentThreadLocal(authenticationManager, user.getEmail(), "test");
        Task task = TestUtil.createAndSaveTask(user, lang, work, taskRepository);

        //пользователь отправляет на рассмотрение заявку на новую книгу
        {
            Task createdTask = TestUtil.getDtoByIdByUser(user, "test", task, "/tasks/", mockMvc, Task.class);

            Task taskUserSubmitDtoRequest = (Task) SerializationUtils.clone(createdTask);

            assertThat(createdTask.getAvailableCommands().stream()).contains(Task.WorkflowStepEnum.SUBMITTED.name());
            taskUserSubmitDtoRequest.setCommand(Task.WorkflowStepEnum.SUBMITTED.name());
            TestUtil.putDtoByUser(user, "test", taskUserSubmitDtoRequest, "/tasks/", mockMvc, Task.class);
        }

        //администратор отправляет заявку на исправление пользователю
        {
            Task taskAdminSubmitDtoResponse = TestUtil.getDtoByIdByUser(admin, "admin", task, "/tasks/", mockMvc, Task.class);

            Task taskAdminEscalateDtoRequest = (Task) SerializationUtils.clone(taskAdminSubmitDtoResponse);

            assertThat(taskAdminSubmitDtoResponse.getAvailableCommands().stream()).contains(Task.WorkflowStepEnum.ESCALATED.name());
            taskAdminEscalateDtoRequest.setCommand(Task.WorkflowStepEnum.ESCALATED.name());
            TestUtil.putDtoByUser(admin, "admin", taskAdminEscalateDtoRequest, "/tasks/", mockMvc, Task.class);
        }

        //пользователь делает повторную отправку на рассмотрение
        {
            Task taskUserEscalateDtoResponse = TestUtil.getDtoByIdByUser(user, "test", task, "/tasks/", mockMvc, Task.class);

            Task taskUserSubmitAgainDtoRequest = (Task) SerializationUtils.clone(taskUserEscalateDtoResponse);

            assertThat(taskUserEscalateDtoResponse.getAvailableCommands().stream()).contains(Task.WorkflowStepEnum.SUBMITTED.name());

            taskUserSubmitAgainDtoRequest.setCommand(Task.WorkflowStepEnum.SUBMITTED.name());
            TestUtil.putDtoByUser(user, "test", taskUserSubmitAgainDtoRequest, "/tasks/", mockMvc, Task.class);
        }

        //администратор принимает делает повторную заявку - создается книга на основе заявки
        {
            Task taskAdminSubmitAgainDtoResponse = TestUtil.getDtoByIdByUser(admin, "admin", task, "/tasks/", mockMvc, Task.class);

            Task taskAdminApproveDtoRequest = (Task) SerializationUtils.clone(taskAdminSubmitAgainDtoResponse);

            assertThat(taskAdminSubmitAgainDtoResponse.getAvailableCommands().stream()).contains(Task.WorkflowStepEnum.APPROVED.name());
            taskAdminApproveDtoRequest.setCommand(Task.WorkflowStepEnum.APPROVED.name());
            TestUtil.putDtoByUser(admin, "admin", taskAdminApproveDtoRequest, "/tasks/", mockMvc, Task.class);
        }

        //успешно создана книга по заявке
        Iterable<Book> createdBooks = bookRepository.findAll(new BooleanBuilder().and(QBook.book.title.eq(task.getBookTitle())));

        assertAll(
                () -> assertThat(createdBooks).hasSize(1)
        );

    }


    @Test
    @Transactional
    void delete() throws Exception {
        User user = TestUtil.createAndSaveUserWithUserRole(passwordEncoder, authorityRepository, userRepository);
        Lang lang = TestUtil.createAndSaveLang(langRepository);
        Work work = TestUtil.createAndSaveWork(workRepository);
        Task task = TestUtil.createAndSaveTask(user, lang, work, taskRepository);

        AtomicReference<String> responceBodyRef = new AtomicReference<>();
        User admin = userRepository.findByEmail("admin@localhost").orElse(null);
        String token = AuthControllerTest.authUser(mockMvc, LoginRequestDto.builder().email(admin.getEmail()).password("admin").build());
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/" + task.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(mvcResult -> responceBodyRef.set(mvcResult.getResponse().getContentAsString()));

        assertAll(
                () -> assertThat(taskRepository.findById(task.getId())).isEmpty()
        );
    }
}
