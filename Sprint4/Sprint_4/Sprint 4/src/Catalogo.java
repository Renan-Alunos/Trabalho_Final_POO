import java.util.ArrayList;

public class Catalogo {

    private ArrayList<Produto> produtos;

    public Catalogo() {
        produtos = new ArrayList<>();
    }

    public void adicionarProduto(Produto produto) {
        produtos.add(produto);
    }

    public Produto buscarProduto(int codigo) {
        for (Produto p : produtos) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }
}
