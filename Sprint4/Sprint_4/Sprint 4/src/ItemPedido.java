public class ItemPedido {

    private Produto produto;
    private int quantidade;
    private double precoUnitario;

    public ItemPedido(Produto produto, int quantidade) {
        if (produto == null || quantidade <= 0) {
            throw new IllegalArgumentException("Item invalido!");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double calcularSubtotal() {
        return precoUnitario * quantidade;
    }
}