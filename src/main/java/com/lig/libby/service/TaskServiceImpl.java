package com.lig.libby.service;

import com.lig.libby.domain.*;
import com.lig.libby.repository.BookRepository;
import com.lig.libby.repository.TaskRepository;
import com.lig.libby.repository.UserRepository;
import com.lig.libby.repository.WorkRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@ThreadSafe
@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WorkRepository workRepository;


    @Autowired
    public TaskServiceImpl(@NonNull TaskRepository taskRepository, @NonNull BookRepository bookRepository) {
        this.taskRepository = taskRepository;
        this.bookRepository = bookRepository;
    }

    private void setAvailableCommands(Task entity, @NonNull UserDetails userDetails) {
        if (entity != null && entity.getWorkflowStep() != null) {
            entity.setAvailableCommands(entity.getWorkflowStep().nextStatesString(userDetails));
        }
    }

    private void processCommand(Task entity, @NonNull UserDetails userDetails) {
        if (entity != null && entity.getCommand() != null) {
            String command = entity.getCommand();
            Task currentEntity = this.findById(entity.getId(), userDetails);
            if (currentEntity.getWorkflowStep() != null && currentEntity.getWorkflowStep().nextStatesString(userDetails).contains(entity.getCommand())) {
                entity.setWorkflowStep(Task.WorkflowStepEnum.valueOf(command));
                if (Task.WorkflowStepEnum.APPROVED.name().equals(command)) {
                    User user = new User();
                    user.setId(userDetails.getUsername());

                    entity.setAssignee(user);

                    Book approvedBook = new Book();
                    approvedBook.setLang(entity.getBookLang());
                    approvedBook.setAuthors(entity.getBookAuthors());
                    approvedBook.setAverageRating(0F);
                    approvedBook.setImageUrl(entity.getBookImageUrl());
                    approvedBook.setIsbn(entity.getBookIsbn());
                    approvedBook.setIsbn13(entity.getBookIsbn13());
                    approvedBook.setOriginalPublicationYear(entity.getBookOriginalPublicationYear());
                    approvedBook.setSmallImageUrl(entity.getBookSmallImageUrl());
                    approvedBook.setOriginalTitle(entity.getBookOriginalTitle());
                    approvedBook.setTitle(entity.getBookTitle());
                    approvedBook.setName(entity.getBookName());
                    Work approvedBookWork;
                    if (entity.getBookWork() != null) {
                        approvedBookWork = entity.getBookWork();
                    } else {
                        approvedBookWork = new Work();
                        workRepository.saveAndFind(approvedBookWork);
                    }
                    approvedBook.setWork(approvedBookWork);
                    bookRepository.saveAndFind(approvedBook);

                    approvedBookWork.setBestBook(approvedBook);
                    workRepository.saveAndFind(approvedBookWork);

                    entity.setBook(approvedBook);
                } else if (Task.WorkflowStepEnum.SUBMITTED.name().equals(command)) {
                    User admin = userRepository.findFirstWithAdminAuthority();
                    entity.setAssignee(admin);
                } else if (Task.WorkflowStepEnum.ESCALATED.name().equals(command)) {
                    User user = new User();
                    user.setId(currentEntity.getCreatedBy().getId());
                    entity.setAssignee(user);
                }
            }
        }
    }

    @Override
    public Task findById(@NonNull String id, @NonNull UserDetails userDetails) {
        Task entity = taskRepository.findById(id).orElse(null);
        setAvailableCommands(entity, userDetails);
        return entity;
    }

    @Override
    public @NonNull Page<Task> findAll(Predicate predicate, Pageable pageable, @NonNull UserDetails userDetails) {
        if(predicate == null) {
            predicate = new BooleanBuilder().and(QTask.task.id.isNotNull());
        }

        BooleanBuilder where = new BooleanBuilder();

        if (userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals(Authority.Roles.ADMIN))) {
            where.and(predicate);
        } else {
            where.and(predicate).and(QTask.task.createdBy.id.eq(userDetails.getUsername()));
        }

        Page<Task> page = taskRepository.findAll(where, pageable);
        page.stream().forEach(entity -> setAvailableCommands(entity, userDetails));
        return page;
    }

    @Override

    public @NonNull Task update(@NonNull Task entity, @NonNull UserDetails userDetails) {
        Task currentEntity = this.findById(entity.getId(), userDetails);
        if (userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch(a -> a.equals(Authority.Roles.ADMIN))
                && !(userDetails.getUsername().equals(currentEntity.getAssignee().getId()))) {
            throw new RuntimeException("Заявка находится на рассмотрении - редактирование не доступно");
        }
        if (currentEntity.getWorkflowStep().equals(Task.WorkflowStepEnum.APPROVED)) {
            throw new RuntimeException("Заявка уже одобрена - редактирование не доступно");
        }
        processCommand(entity, userDetails);
        taskRepository.saveAndFind(entity);
        return this.findById(entity.getId(), userDetails);
    }

    @Override

    public @NonNull Task create(@NonNull Task entity, @NonNull UserDetails userDetails) {
        User user = new User();
        user.setId(userDetails.getUsername());
        entity.setAssignee(user);
        entity.setWorkflowStep(Task.WorkflowStepEnum.INIT);
        taskRepository.saveAndFind(entity);
        return this.findById(entity.getId(), userDetails);
    }

    @Override
    public void deleteById(@NonNull String id) {
        taskRepository.deleteById(id);
    }
}
