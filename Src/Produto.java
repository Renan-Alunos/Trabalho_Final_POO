public class Produto {

    private int codigo;
    private String nome;
    private double preco;
    private int quantidade;

    public Produto(int codigo, String nome, double preco, int quantidade) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O codigo do produto deve ser maior que zero.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto nao pode estar vazio.");
        }
        if (!Double.isFinite(preco) || preco < 0) {
            throw new IllegalArgumentException("O preco do produto nao pode ser negativo.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("O estoque nao pode ser negativo.");
        }

        this.codigo = codigo;
        this.nome = nome.trim();
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
        if (!Double.isFinite(novoPreco) || novoPreco < 0) {
            throw new IllegalArgumentException("Informe um preco valido.");
        }
        preco = novoPreco;
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
