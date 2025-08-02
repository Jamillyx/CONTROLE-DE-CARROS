-- V2__atualizar_senhas_usuarios.sql

-- Atualiza a senha do usuário 'admin'
UPDATE usuario
SET senha = '{bcrypt}$2a$10$yf/WM45j8a3Fbr9i9JTv6.TUAdnKRxy5gUQVhYlZlHNFiF7vUrbQq'
WHERE nome_usuario = 'admin';

-- Atualiza a senha do usuário 'usuario'
UPDATE usuario
SET senha = '{bcrypt}$2a$10$EgSPWjH1GSrs16Pyfq0z6OilMqenZLHDWjvwdXpRO2G5rsUeA/MVe'
WHERE nome_usuario = 'usuario';