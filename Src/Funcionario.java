public class Funcionario extends Usuario {

    private String matricula;

    public Funcionario(String nome, String cpf, String senha, String matricula) {
        super(nome, cpf, senha);
        this.matricula = matricula;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String getTipoUsuario() {
        return "Funcionario";
    }

    public void alterarPreco(Produto produto, double novoPreco) {
        produto.alterarPreco(novoPreco);
    }

    public void adicionarProduto(Catalogo catalogo, Produto produto) {
        catalogo.adicionarProduto(produto);
    }
}
