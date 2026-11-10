import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Sistema {

    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private ArrayList<Pedido> pedidos = new ArrayList<>();
    private Catalogo catalogo = new Catalogo();
    private int proximoPedido = 1;

    public Sistema() {
        usuarios.add(new Funcionario("Administrador", "00000000000", "admin123", "F01"));

        catalogo.adicionarProduto(new Produto(1, "Dipirona 500mg", 12.50, 5));
        catalogo.adicionarProduto(new Produto(2, "Paracetamol 750mg", 9.90, 10));
        catalogo.adicionarProduto(new Produto(3, "Ibuprofeno 400mg", 15.90, 6));
        catalogo.adicionarProduto(new Produto(4, "Vitamina C", 19.90, 5));
        catalogo.adicionarProduto(new Produto(5, "Soro Fisiologico", 7.50, 10));
    }

    public Usuario fazerLogin(String cpf, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf) && usuario.conferirSenha(senha)) {
                return usuario;
            }
        }
        return null;
    }

    public void cadastrarCliente(String nome, String cpf, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf)) {
                throw new IllegalArgumentException("CPF ja cadastrado.");
            }
        }

        usuarios.add(new Cliente(nome, cpf, senha));
    }

    public ArrayList<Produto> listarProdutos() {
        return catalogo.getProdutos();
    }

    public Produto buscarProduto(int codigo) {
        return catalogo.buscarProduto(codigo);
    }

    public ArrayList<Pedido> listarPedidos() {
        return new ArrayList<>(pedidos);
    }

    private boolean funcionarioValido(Funcionario funcionario) {
        return funcionario != null && usuarios.contains(funcionario);
    }

    public boolean cadastrarProduto(Funcionario funcionario, int codigo,
                                    String nome, double preco, int quantidade) {
        if (!funcionarioValido(funcionario) || catalogo.buscarProduto(codigo) != null) {
            return false;
        }

        try {
            funcionario.adicionarProduto(catalogo, new Produto(codigo, nome, preco, quantidade));
            return true;
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    public boolean alterarPreco(Funcionario funcionario, int codigo, double preco) {
        Produto produto = catalogo.buscarProduto(codigo);

        if (!funcionarioValido(funcionario) || produto == null) {
            return false;
        }

        try {
            funcionario.alterarPreco(produto, preco);
            return true;
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    public boolean reporEstoque(Funcionario funcionario, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);

        if (!funcionarioValido(funcionario) || produto == null) {
            return false;
        }

        try {
            funcionario.reporEstoque(produto, quantidade);
            return true;
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null || !usuarios.contains(cliente)) {
            throw new IllegalArgumentException("Cliente nao cadastrado.");
        }

        return new Pedido(proximoPedido++, cliente);
    }

    public boolean finalizarPedido(Pedido pedido, Pagamento pagamento) {
        if (pedido == null || pagamento == null || pedido.isFinalizado() ||
            !usuarios.contains(pedido.getCliente()) || pedido.getItens().isEmpty()) {
            return false;
        }

        Map<Produto, Integer> quantidades = new HashMap<>();

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();

            if (catalogo.buscarProduto(produto.getCodigo()) != produto) {
                return false;
            }

            int total = quantidades.getOrDefault(produto, 0) + item.getQuantidade();

            if (total > produto.getQuantidade()) {
                return false;
            }

            quantidades.put(produto, total);
        }

        if (!pagamento.processarPagamento(pedido.calcularTotal())) {
            return false;
        }

        for (Map.Entry<Produto, Integer> entrada : quantidades.entrySet()) {
            entrada.getKey().retirarEstoque(entrada.getValue());
        }

        pedido.confirmarPagamento(pagamento.getForma());
        pedido.getCliente().adicionarPedido(pedido);
        pedidos.add(pedido);

        return true;
    }
}
