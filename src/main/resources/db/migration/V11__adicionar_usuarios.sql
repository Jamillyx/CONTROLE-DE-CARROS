INSERT INTO papel (nome) VALUES ('ADMIN');
INSERT INTO papel (nome) VALUES ('USUARIO');
-- Adicione outros papéis que seu sistema possa ter, se for o caso.

-- Inserção de Usuários Iniciais
-- Usuário 'admin' com a senha 'admin' (criptografada)
INSERT INTO usuario (nome, email, senha, nome_usuario, data_nascimento, ativo)
VALUES ('Gerente', 'admin@controle.com', '12345', 'admin', '2003-06-18', TRUE);

-- Usuário 'usuario' com a senha 'usuario' (criptografada)
INSERT INTO usuario (nome, email, senha, nome_usuario, data_nascimento, ativo)
VALUES ('Usuário Padrão', 'usuario@controlevac.com', '12345', 'usuario', '1990-05-15', TRUE);

-- Associar Papéis aos Usuários
-- Associar o papel 'ADMIN' ao usuário 'admin'
INSERT INTO usuario_papel (codigo_usuario, codigo_papel)
SELECT u.codigo, p.codigo
FROM usuario u, papel p
WHERE u.nome_usuario = 'admin' AND p.nome = 'ADMIN';

-- Associar o papel 'USUARIO' ao usuário 'usuario'
INSERT INTO usuario_papel (codigo_usuario, codigo_papel)
SELECT u.codigo, p.codigo
FROM usuario u, papel p
WHERE u.nome_usuario = 'usuario' AND p.nome = 'USUARIO';