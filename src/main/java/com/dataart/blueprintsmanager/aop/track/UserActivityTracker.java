package com.dataart.blueprintsmanager.aop.track;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserActivityTracker {
    UserAction action() default UserAction.NONE;

    String login() default "";

    String projectId() default "";

    String documentId() default "";

    String userId() default "";

    String projectCode() default "";

    String documentName() default "";

    String companyId() default "";
    String companyName() default "";

    String commentId() default "";
}
