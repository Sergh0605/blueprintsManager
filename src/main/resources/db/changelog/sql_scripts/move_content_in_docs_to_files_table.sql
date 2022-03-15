alter table bpm_file add temp bigint;

insert into bpm_file (content, temp) select content_in_pdf, id from bpm_document;

update bpm_document as d
set content_in_pdf_id = f.id
    from bpm_file as f
where d.id = f.temp;

alter table bpm_file drop column temp;

alter table bpm_document drop column content_in_pdf;

alter table bpm_document drop column document_in_pdf;

update bpm_document
set reassembly_required = true;

alter table bpm_project drop column project_in_pdf;

update bpm_project
set reassembly_required = true;