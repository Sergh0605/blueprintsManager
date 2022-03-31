package com.dataart.blueprintsmanager.aop.track;

import com.dataart.blueprintsmanager.persistence.entity.UserActivityEntity;
import com.dataart.blueprintsmanager.service.UserActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static com.dataart.blueprintsmanager.config.security.SecurityUtil.getCurrentUserLogin;

@Aspect
@Component
@Slf4j
@AllArgsConstructor
public class LogAndTrackUserActivityAspect {
    private final UserActivityService userActivityService;

    @Around("@annotation(userActivityTracker)")
    public Object logAndTrackActivity(ProceedingJoinPoint joinPoint, UserActivityTracker userActivityTracker) throws Throwable {
        UserActivityEntity userActivity = UserActivityEntity.builder()
                .action(userActivityTracker.action())
                .login(getCurrentUserLogin())
                .build();
        try {
            Object object = joinPoint.proceed();
            userActivity.setMessage(getMessage(joinPoint, userActivityTracker));
            return object;
        } catch (CannotAcquireLockException e) {
            userActivity.setMessage(e.getCause().getCause().getMessage());
            throw e;
        } catch (Exception e) {
            userActivity.setMessage(e.getMessage());
            throw e;
        } finally {
            userActivityService.save(userActivity);
        }
    }

    private String getMessage(ProceedingJoinPoint joinPoint, UserActivityTracker userActivityTracker) {
        EvaluationContext context = prepareParametersContext(joinPoint);
        UserAction action = userActivityTracker.action();
        return switch (action) {
            case NONE -> "";
            case REG_USER, LOGIN ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.login()));
            case REFRESH_TOKEN, LOGOUT -> action.getAfterActionLogMessageTemplate();
            case UPDATE_USER, UPDATE_ROLES ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.userId()));
            case UPDATE_PROJECT, DELETE_PROJECT, RESTORE_PROJECT, DOWNLOAD_PROJECT, REASSEMBLY_PROJECT, CREATE_COMMENT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.projectId()));
            case CREATE_PROJECT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.projectCode()));
            case DOWNLOAD_DOCUMENT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.projectId()), getStringParam(context, userActivityTracker.documentId()));
            case DELETE_DOCUMENT, RESTORE_DOCUMENT, UPDATE_DOCUMENT, REASSEMBLY_DOCUMENT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.documentId()));
            case CREATE_DOCUMENT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.documentName()), getStringParam(context, userActivityTracker.projectId()));
            case UPDATE_COMPANY, RESTORE_COMPANY, DELETE_COMPANY ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.companyId()));
            case CREATE_COMPANY ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.companyName()));
            case DELETE_COMMENT, RESTORE_COMMENT ->
                    String.format(action.getAfterActionLogMessageTemplate(), getStringParam(context, userActivityTracker.commentId()));
        };
    }

    private Object getParam(EvaluationContext context, String paramName) {
        if (!StringUtils.hasText(paramName)) {
            return "";
        }
        try {
            ExpressionParser expressionParser = new SpelExpressionParser();

            return expressionParser.parseExpression(paramName).getValue(context);
        } catch (Exception e) {
            log.warn("Error during parsing value (Pattern: {}). Reason: {}", paramName, e.getMessage());
            return null;
        }
    }

    private String getStringParam(EvaluationContext context, String paramName) {
        return (String) getParam(context, paramName);
    }

    private EvaluationContext prepareParametersContext(JoinPoint proceedingJoinPoint) {
        EvaluationContext context = new StandardEvaluationContext();
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        for (int i = 0; i < method.getParameterAnnotations().length; i++) {
            Annotation[] annotations = method.getParameterAnnotations()[i];
            if (annotations.length != 0) {
                Optional<Annotation> paramName = Arrays.stream(annotations)
                        .filter(a -> a.annotationType().equals(ParamName.class))
                        .findFirst();
                Object arg = proceedingJoinPoint.getArgs()[i];
                paramName.ifPresent(annotation ->
                        context.setVariable(((ParamName) paramName.get()).value(), arg));
            }
        }
        return context;
    }

}
