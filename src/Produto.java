public class Produto {

    private int codigo;
    private String nome;
    private double preco;
    private int quantidade;

    public Produto(int codigo, String nome, double preco, int quantidade) {
        this.codigo = codigo;
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void alterarPreco(double novoPreco) {
        if (novoPreco >= 0) {
            preco = novoPreco;
        }
    }

    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0 || quantidade > Integer.MAX_VALUE - this.quantidade) {
            throw new IllegalArgumentException("Quantidade invalida!");
        }
        this.quantidade += quantidade;
    }

    public boolean retirarEstoque(int quantidade) {
        if (quantidade > 0 && quantidade <= this.quantidade) {
            this.quantidade -= quantidade;
            return true;
        }
        return false;
    }
}