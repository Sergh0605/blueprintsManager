alter table bpm_file add temp bigint;

insert into bpm_file (content, temp) select logo, id from bpm_company;

update bpm_company
set logo_file_id = f.id
    from bpm_file as f
where bpm_company.id = f.temp;

alter table bpm_file drop column temp;

alter table bpm_company drop column logo;