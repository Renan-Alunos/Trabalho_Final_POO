import java.time.LocalDateTime;
public class Usuario {

    private String nome;

    public Usuario(String nome) {
        this.nome = nome;
        this.cpf = cpf;;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public int getSenha() {
        return senha;
    }

    public String getCpf() {
        return cpf;
    }
}