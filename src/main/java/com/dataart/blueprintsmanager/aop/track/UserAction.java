package com.dataart.blueprintsmanager.aop.track;

import lombok.Getter;

@Getter
public enum UserAction {
    NONE(""),
    REG_USER("User with Login %s created."),
    LOGIN("Access granted for User with Login = %s."),
    REFRESH_TOKEN("Tokens was refreshed."),
    LOGOUT("User was logged out."),
    UPDATE_USER("User with ID = %s updated."),
    UPDATE_ROLES("Roles in User with ID = %s updated."),
    UPDATE_PROJECT("Project with ID = %s updated"),
    DELETE_PROJECT("Project with ID = %s marked as deleted."),
    RESTORE_PROJECT("Project with ID = %s restored."),
    CREATE_PROJECT("Project with code %s created."),
    DOWNLOAD_PROJECT("Project in PDF Downloaded. Project ID = %s"),
    REASSEMBLY_PROJECT("Project with ID = %s reassembled"),
    DOWNLOAD_DOCUMENT("Document in PDF Downloaded. Project ID = %s, Document ID = %s"),
    DELETE_DOCUMENT("Document with ID = %s marked as deleted."),
    RESTORE_DOCUMENT("Project with ID = %s restored."),
    REASSEMBLY_DOCUMENT("Document with ID = %s reassembled."),
    UPDATE_DOCUMENT("Document with ID = %s updated"),
    CREATE_DOCUMENT("Document with Name = %s created in Project with ID = %s"),
    UPDATE_COMPANY("Company with ID = %s updated"),
    CREATE_COMPANY("Company with Name %s created."),
    DELETE_COMPANY("Company with ID = %s marked as deleted."),
    RESTORE_COMPANY("Project with ID = %s restored."),
    CREATE_COMMENT("Comment for Project with ID = %s created."),
    DELETE_COMMENT("Comment with ID = %s marked as deleted."),
    RESTORE_COMMENT("Comment with ID = %s restored.");





    private final String afterActionLogMessageTemplate;

    UserAction(String afterActionLogMessageTemplate) {
        this.afterActionLogMessageTemplate = afterActionLogMessageTemplate;
    }
}
