public class ItemPedido {

    private Produto produto;
    private int quantidade;
    private double precoUnitario;

    public ItemPedido(Produto produto, int quantidade) {
        this(produto, quantidade, produto == null ? 0 : produto.getPreco());
    }

    public ItemPedido(Produto produto, int quantidade, double precoUnitario) {
        if (produto == null || quantidade <= 0 || !Double.isFinite(precoUnitario) || precoUnitario < 0) {
            throw new IllegalArgumentException("Item invalido!");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
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