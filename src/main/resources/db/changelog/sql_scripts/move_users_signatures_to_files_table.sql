alter table bpm_file add temp bigint;

insert into bpm_file (content, temp) select signature, id from bpm_user;

update bpm_user as u
set sign_file_id = f.id
    from bpm_file as f
where u.id = f.temp;

alter table bpm_file drop column temp;

alter table bpm_user drop column signature;