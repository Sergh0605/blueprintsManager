INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_company (name, signer_position, signer_name, city, logo_file_id)
VALUES ('ООО"Инженерные системы"', 'Исполнительный директор', 'Иванов И.И.', 'Воронеж', lastval());

INSERT INTO public.bpm_file (content) VALUES (null);
INSERT INTO public.bpm_company (name, signer_position, signer_name, city, logo_file_id)
VALUES ('ООО"Строительная компания"', 'Генеральный директор', 'Иванов И.И.', 'Воронеж', lastval());