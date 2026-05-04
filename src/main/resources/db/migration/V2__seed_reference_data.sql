insert into users (name, cpf, cnpj, role, verified, created_at)
values
('Doador Demo', '11122233344', null, 'DONOR', true, current_timestamp),
('ONG Demo', null, '12345678000199', 'INSTITUTION', true, current_timestamp),
('Administrador Demo', '99988877766', null, 'ADMIN', true, current_timestamp);

insert into institutions (
    legal_name,
    cnpj,
    pharmacist_name,
    pharmacist_registry,
    status,
    latitude,
    longitude,
    created_at
) values (
    'Instituto Saude Solidaria',
    '12345678000199',
    'Dra. Ana Farmaceutica',
    'CRF-SP 12345',
    'ACTIVE',
    -23.550520,
    -46.633308,
    current_timestamp
);

insert into medication_blacklist (
    blocked_type_code,
    active_ingredient,
    commercial_name,
    reason,
    active,
    created_at
) values
(4, null, null, 'Medicamento de controle especial/tarja preta bloqueado por regra sanitaria.', true, current_timestamp),
(null, 'clonazepam', null, 'Substancia sujeita a controle especial.', true, current_timestamp),
(null, 'morfina', null, 'Substancia sujeita a controle especial.', true, current_timestamp),
(null, 'metilfenidato', null, 'Substancia sujeita a controle especial.', true, current_timestamp);
