package web.controlevacinacao; // Ou o pacote onde você criar a classe

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Gerar senha para 'admin'
        String rawPasswordAdmin = "12345"; // A senha em texto puro que você quer usar
        String encodedPasswordAdmin = encoder.encode(rawPasswordAdmin);
        System.out.println("Senha para admin ('" + rawPasswordAdmin + "') para SQL: {bcrypt}" + encodedPasswordAdmin);

        // Gerar senha para 'usuario'
        String rawPasswordUsuario = "12345"; // A senha em texto puro que você quer usar
        String encodedPasswordUsuario = encoder.encode(rawPasswordUsuario);
        System.out.println("Senha para usuario ('" + rawPasswordUsuario + "') para SQL: {bcrypt}" + encodedPasswordUsuario);
    }
}