alter table users add column username varchar(80);
alter table users add column password_hash varchar(200);

update users
set username = case
    when cpf = '11122233344' then 'doador'
    when cnpj = '12345678000199' then 'ong'
    when cpf = '99988877766' then 'admin'
    when cpf is not null then cpf
    when cnpj is not null then cnpj
    else concat('user-', id)
end;

update users
set password_hash = 'pbkdf2_sha256$310000$cmVtZWRpYXItZGVmYXVsdC1kaXNhYmxlZA==$AG3uOc/51LdhK38IiuO6iP9DEwQ8bXe6ez5u78JrXV8=';

update users
set password_hash = 'pbkdf2_sha256$310000$cmVtZWRpYXItZG9hZG9yLXNhbHQ=$AlYa/5DD7ao4lQ9wL0D6eSBn5D2AS2NG4FDDW4Tu6kM='
where username = 'doador';

update users
set password_hash = 'pbkdf2_sha256$310000$cmVtZWRpYXItb25nLXNhbHQ=$xM33jtdYQwlZNwpuNQDSGvHsW2OyWIPfdqv7uOOZ4wc='
where username = 'ong';

update users
set password_hash = 'pbkdf2_sha256$310000$cmVtZWRpYXItYWRtaW4tc2FsdA==$/1S8pQaoJ/lj9XjXu3mYCIlG+bMX0Ep3GtAapAsiDM8='
where username = 'admin';

alter table users alter column username set not null;
alter table users alter column password_hash set not null;
alter table users add constraint uk_users_username unique (username);
