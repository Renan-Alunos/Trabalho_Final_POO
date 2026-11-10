public class Usuario {

    private String nome;
    private String cpf;
    private String senha;

    public Usuario(String nome, String senha, String cpf) {
        this.nome = nome;
        this.senha = senha;
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTipoUsuario() {
        return "Usuario";
    }
}