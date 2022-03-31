DELETE FROM bpm_comment;
DELETE FROM bpm_document;
DELETE FROM bpm_project_history;
DELETE FROM bpm_project;
DELETE FROM bpm_user_to_role WHERE user_id <> '1';
DELETE FROM bpm_token;
DELETE FROM bpm_user WHERE login <> 'admin';
DELETE FROM bpm_company;
