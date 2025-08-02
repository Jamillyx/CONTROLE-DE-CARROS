CREATE TABLE public.motorista
(
    codigo serial NOT NULL,
    nome text,
    cpf text,
    cnh text,
    data_nascimento date,
    status text DEFAULT 'ATIVO',
    PRIMARY KEY (codigo)
);
