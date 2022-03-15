/*CoverPage*/
UPDATE public.bpm_document_type SET unmodified = true, default_page_number = 1 WHERE id = 1;

/*TitlePage*/
UPDATE public.bpm_document_type SET unmodified = true, default_page_number = 2 WHERE id = 2;

/*Contents*/
UPDATE public.bpm_document_type SET unmodified = true, default_page_number = 3 WHERE id = 3;

/*Text*/
UPDATE public.bpm_document_type SET unmodified = false, default_page_number = 4 WHERE id = 4;

/*DrawingTemplate*/
UPDATE public.bpm_document_type SET unmodified = false, default_page_number = 5 WHERE id = 5;