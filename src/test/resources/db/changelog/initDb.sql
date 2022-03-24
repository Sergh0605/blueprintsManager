INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_company (name, signer_position, signer_name, city, logo_file_id)
VALUES ('ООО"Инженерные системы"', 'Исполнительный директор', 'Иванов И.И.', 'Воронеж', currval(pg_get_serial_sequence('bpm_file', 'id')));

INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_company (name, signer_position, signer_name, city, logo_file_id)
VALUES ('ООО"Строительная компания"', 'Генеральный директор', 'Иванов И.И.', 'Воронеж', currval(pg_get_serial_sequence('bpm_file', 'id')));

INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_user (last_name, login, password, company_id, email, sign_file_id)
VALUES ('Петров', 'admin', '$2a$10$b.GrSRTIxNNhOo2nGjx59OyK0r7U8gof6TV13qz/9Oy2nEmlq1yhy', (SELECT MAX(id) FROM bpm_company), 'puzakovsergei@gmail.com', currval(pg_get_serial_sequence('bpm_file', 'id')));
INSERT INTO public.bpm_user_to_role (role_id, user_id) VALUES (1, lastval());

INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_user (last_name, login, password, company_id, email, sign_file_id)
VALUES ('Иванов', 'editor', '$2a$10$dF4aQG5LTZbNMBSJyJ0y/uhCtYqspZ4e4DYBg.tpQeslas.L81B56', (SELECT MAX(id) FROM bpm_company), 'puzakovsergei@gmail.com', currval(pg_get_serial_sequence('bpm_file', 'id')));
INSERT INTO public.bpm_user_to_role (role_id, user_id) VALUES (2, lastval());

INSERT INTO public.bpm_file (content)
VALUES (null);
INSERT INTO public.bpm_user (last_name, login, password, company_id, email, sign_file_id)
VALUES ('Лавочкин', 'user', '$2a$10$NhxfC47mS37ZXYv6R3LxVeSUssK3qJJxlhZJz0acI2wadYSZkc9iW', (SELECT MAX(id) FROM bpm_company), 'puzakovsergei@gmail.com', currval(pg_get_serial_sequence('bpm_file', 'id')));
INSERT INTO public.bpm_user_to_role (role_id, user_id) VALUES (3, lastval());