import java.util.ArrayList;

public class Pedido {

    private int numero;
    private Cliente cliente;
    private ArrayList<ItemPedido> itens;
    private String situacao = "ABERTO";
    private String formaPagamento;

    public Pedido(int numero, Cliente cliente) {
        this.numero = numero;
        this.cliente = cliente;
        itens = new ArrayList<>();
    }

    public int getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
    }

    // Assim dá para mudar a quantidade sem criar outro pedido toda vez.
    public void definirQuantidade(Produto produto, int quantidade) {
        if (!"ABERTO".equals(situacao)) {
            throw new IllegalStateException("Pedido nao esta aberto!");
        }
        if (produto == null || quantidade < 0) {
            throw new IllegalArgumentException("Produto ou quantidade invalida.");
        }
        itens.removeIf(item -> item.getProduto() == produto);
        if (quantidade > 0) {
            itens.add(new ItemPedido(produto, quantidade));
        }
    }

    public long quantidadeDoProduto(Produto produto) {
        long quantidadeTotal = 0;
        for (ItemPedido item : itens) {
            if (item.getProduto() == produto) {
                quantidadeTotal += item.getQuantidade();
            }
        }
        return quantidadeTotal;
    }

    public ArrayList<ItemPedido> getItens() {
        return new ArrayList<>(itens);
    }

    public String getSituacao() {
        return situacao;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void finalizar(String formaPagamento) {
        if (!"ABERTO".equals(situacao)) {
            throw new IllegalStateException("Pedido nao esta aberto!");
        }
        this.formaPagamento = formaPagamento;
        this.situacao = "FINALIZADO";
    }

    public double calcularTotal() {
        double total = 0;

        for (ItemPedido item : itens) {
            total += item.calcularSubtotal();
        }

        return total;
    }
}