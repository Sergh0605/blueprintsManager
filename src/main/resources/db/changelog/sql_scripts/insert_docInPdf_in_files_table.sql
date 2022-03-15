alter table bpm_file add temp bigint;

insert into bpm_file (temp) select id from bpm_document;

update bpm_document as d
set document_in_pdf_id = f.id
from bpm_file as f
where d.id = f.temp;

alter table bpm_file drop column temp;