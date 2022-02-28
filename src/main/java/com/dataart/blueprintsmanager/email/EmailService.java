package com.dataart.blueprintsmanager.email;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class EmailService {
    static final DateTimeFormatter editTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final TemplateEngine templateEngine;
    private final JavaMailSender emailSender;
    private final String from;
    private final String applicationUrl;

    public EmailService(TemplateEngine templateEngine, JavaMailSender emailSender, @Value("${spring.mail.username}") String from, @Value("${bpm.application.url}") String applicationUrl) {
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
        this.from = from;
        this.applicationUrl = applicationUrl;
    }

    @Async
    public void sendEmailOnProjectEdit(ProjectEntity project) {
        sendEmailForProject(project, "edited");
    }

    @Async
    public void sendEmailOnProjectReassembly(ProjectEntity project) {
        sendEmailForProject(project, "reassembled");
    }

    @Async
    public void sendEmailOnProjectCreate(ProjectEntity project) {
        sendEmailForProject(project, "created");
    }

    @Async
    public void sendEmailOnDocumentEdit(DocumentEntity document) {
        sendEmailForDocument(document, "edited");
    }

    @Async
    public void sendEmailOnDocumentCreate(DocumentEntity document) {
        sendEmailForDocument(document, "created");
    }

    private void sendEmailForProject(ProjectEntity project, String status) {
        log.info("Try to send email notifications for users in Project {}", project.getId());
        Set<UserEntity> users = getContextForProject(project);
        users.forEach(user -> {
            Context context = new Context();
            context.setVariable("status", status);
            context.setVariable("project", project);
            context.setVariable("editTime", project.getEditTime().format(editTimeFormatter));
            context.setVariable("url", applicationUrl);
            context.setVariable("user", user);
            String process = templateEngine.process("emailTemplates/projectEmailTemplate", context);
            String to = user.getEmail();
            String subject = String.format("Project %s was %s", project.getCode(), status);
            try {
                sendEmail(to, subject, process);
            } catch (Exception e) {
                log.warn("Unable to send email notification for user {}  with email address = {} in Project {}", user.getLogin(), user.getEmail(), project.getId(), e);
                return;
            }
            log.info("Email notification for user {} in Project {} has been send", user.getLogin(), project.getId());
        });
    }

    private void sendEmailForDocument(DocumentEntity document, String status) {
        log.info("Try to send email notifications for users in Document {}", document.getId());
        Set<UserEntity> users = getContextForDocument(document);
        users.forEach(user -> {
            Context context = new Context();
            context.setVariable("status", status);
            context.setVariable("document", document);
            context.setVariable("editTime", document.getEditTime().format(editTimeFormatter));
            context.setVariable("url", applicationUrl);
            context.setVariable("user", user);
            String process = templateEngine.process("emailTemplates/documentEmailTemplate", context);
            String to = user.getEmail();
            String subject = String.format("Document %s was %s in Project %s", document.getName(), status, document.getProject().getCode());
            try {
                sendEmail(to, subject, process);
            } catch (Exception e) {
                log.warn("Unable to send email notification for user {} with email address = {} in Document {}", user.getLogin(), user.getEmail(), document.getId());
                return;
            }
            log.info("Email notification for user {} in Document {} has been send", user.getLogin(), document.getId());
        });
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        emailSender.send(message);
    }

    private Set<UserEntity> getContextForProject(ProjectEntity project) {
        Set<UserEntity> users = new HashSet<>();
        Optional.ofNullable(project.getDesigner())
                .ifPresent(users::add);
        Optional.ofNullable(project.getChief())
                .ifPresent(users::add);
        Optional.ofNullable(project.getSupervisor())
                .ifPresent(users::add);
        Optional.ofNullable(project.getController())
                .ifPresent(users::add);
        return users;
    }

    private Set<UserEntity> getContextForDocument(DocumentEntity document) {
        Set<UserEntity> users = getContextForProject(document.getProject());
        Optional.ofNullable(document.getDesigner())
                .ifPresent(users::add);
        Optional.ofNullable(document.getSupervisor())
                .ifPresent(users::add);
        return users;
    }
}
