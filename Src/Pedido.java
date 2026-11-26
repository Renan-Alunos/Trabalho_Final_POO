import java.util.ArrayList;

public class Pedido {

    private int numero;
    private Cliente cliente;
    private ArrayList<ItemPedido> itens;
    private String situacao = "ABERTO";
    private String formaPagamento;

    public Pedido(int numero, Cliente cliente) {
        if (numero <= 0 || cliente == null) {
            throw new IllegalArgumentException("Dados do pedido invalidos.");
        }
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
        if (!"ABERTO".equals(situacao)) {
            throw new IllegalStateException("Nao e possivel alterar um pedido finalizado.");
        }
        if (item == null) {
            throw new IllegalArgumentException("O item do pedido nao pode ser nulo.");
        }
        itens.add(item);
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
        if (formaPagamento == null || formaPagamento.isBlank()) {
            throw new IllegalArgumentException("Informe a forma de pagamento.");
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
