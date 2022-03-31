INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_user (last_name, login, password, sign_file_id)
VALUES ('Администратор', 'admin', '$2a$10$b.GrSRTIxNNhOo2nGjx59OyK0r7U8gof6TV13qz/9Oy2nEmlq1yhy', currval(pg_get_serial_sequence('bpm_file', 'id')));
INSERT INTO public.bpm_user_to_role (role_id, user_id) VALUES (1, lastval());