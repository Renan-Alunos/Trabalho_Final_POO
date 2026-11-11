import java.util.ArrayList;

public class Pedido {

    private int numero;
    private Cliente cliente;
    private ArrayList<ItemPedido> itens;

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

    public ArrayList<ItemPedido> getItens() {
        return new ArrayList<>(itens);
    }

    public double calcularTotal() {
        double total = 0;

        for (ItemPedido item : itens) {
            total += item.calcularSubtotal();
        }

        return total;
    }
}