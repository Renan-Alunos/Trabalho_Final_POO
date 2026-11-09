import java.util.ArrayList;

public class Cliente extends Usuario {

    private ArrayList<Pedido> pedidos;

    public Cliente(String nome, String cpf, String senha) {
        super(nome, cpf, senha);
        pedidos = new ArrayList<>();
    }

    public void adicionarPedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public ArrayList<Pedido> getPedidos() {
        return new ArrayList<>(pedidos);
    }

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }
}
