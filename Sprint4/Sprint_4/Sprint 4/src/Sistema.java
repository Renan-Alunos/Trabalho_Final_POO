import java.util.ArrayList;
import java.util.Locale;
/*
 * A classe Sistema reúne os cadastros, as compras e o controle do estoque.
 * As telas usam esses métodos, sem repetir as regras de negócio.
 */
public class Sistema{
    // Clientes e funcionários usam a mesma lista de usuários.
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    // Produtos disponíveis na farmácia.
    private Catalogo catalogo = new Catalogo();
    private ArrayList<Pedido> pedidosAbertos = new ArrayList<>();
    private ArrayList<Pedido> pedidosFinalizados = new ArrayList<>();
    private int proximoNumeroPedido = 1;
    private final ArrayList<ObservadorSistema> observadores = new ArrayList<>();

    public void adicionarObservador(ObservadorSistema observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void removerObservador(ObservadorSistema observador) {
        observadores.remove(observador);
    }

    private void avisarObservadores() {
        for (ObservadorSistema observador : new ArrayList<>(observadores)) {
            observador.sistemaAtualizado();
        }
    }

    public Sistema(){
        // Conta inicial para entrar como funcionário.
        Funcionario administrador = new Funcionario("Administrador","00000000000","admin123","F01");
        usuarios.add(administrador);

        // Alguns produtos já aparecem quando o programa abre.
        catalogo.adicionarProduto(new Produto(1, "Dipirona 500mg", 12.50, 5));// (código, nome, preço, quantidade)
        catalogo.adicionarProduto(new Produto(2, "Paracetamol 750mg", 9.90, 10));
        catalogo.adicionarProduto(new Produto(3, "Ibuprofeno 400mg", 15.90, 6));
        catalogo.adicionarProduto(new Produto(4, "Vitamina C", 19.90, 5));
        catalogo.adicionarProduto(new Produto(5, "Soro Fisiológico", 7.50, 10));
    }
    
    // Procura o CPF e confere a senha.
    public Usuario fazerLogin(String cpf, String senha){
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf) && usuario.getSenha().equals(senha)){
                return usuario;
            }
        }
        return null; // Não encontrou um usuário com esses dados.
    }

    public Cliente cadastrarCliente(String nome, String cpf, String senha) {
        if (nome == null || nome.isBlank() || cpf == null || !cpf.matches("\\d{11}") || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Dados invalidos!");
        }
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf)) {
                throw new IllegalArgumentException("CPF ja cadastrado!");
            }
        }
        Cliente cliente = new Cliente(nome, cpf, senha);
        usuarios.add(cliente);
        return cliente;
    }

    public String buscarClientes(String cpf){ // Busca o nome do cliente pelo CPF.
        for(Usuario usuario : usuarios){
            if (usuario instanceof Cliente && usuario.getCpf().equals(cpf)){
                return usuario.getNome();
            }
        }
        return null;
    }

    public ArrayList<Produto> listarProdutos(){
        return catalogo.getProdutos();
    }

    public boolean cadastrarProduto(Funcionario funcionario, int codigo, String nome, double preco, int quantidade){
        // Só um funcionário cadastrado pode alterar produtos.
        if (funcionario == null || !usuarios.contains(funcionario)){
            return false;
        }

        // Confere os dados antes de criar o produto.
        if (codigo <= 0 || nome == null || nome.isBlank() || !Double.isFinite(preco) || preco <= 0 || quantidade < 0){
            return false;
        }

        // Não permite dois produtos com o mesmo código.
        if (catalogo.buscarProduto(codigo) != null){
            return false;
        }

        // Cria o produto com os dados preenchidos.
        Produto produto = new Produto(codigo, nome, preco, quantidade);

        // O funcionário faz a inclusão no catálogo.
        funcionario.adicionarProduto(catalogo, produto);
        avisarObservadores();
        return true;
    }

    public boolean alterarPreco(Funcionario funcionario, int codigo, double novoPreco) {
        // Confere se quem pediu a mudança é funcionário.
        if (funcionario == null || !usuarios.contains(funcionario)) {
            return false;
        }

        // Preço inválido não pode ser salvo.
        if (!Double.isFinite(novoPreco) || novoPreco < 0) {
            return false;
        }

        // Localiza o produto pelo código.
        Produto produto = catalogo.buscarProduto(codigo);

        // Se não encontrar o produto, não altera nada.
        if (produto == null) {
            return false;
        }

        // Atualiza o valor e avisa as telas.
        funcionario.alterarPreco(produto, novoPreco);
        avisarObservadores();
        return true;
    }

    public boolean reporEstoque(Funcionario funcionario, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);
        if (!funcionarioValido(funcionario) || produto == null ||
                quantidade <= 0 || quantidade > Integer.MAX_VALUE - produto.getQuantidade()) {
            return false;
        }

        produto.adicionarEstoque(quantidade);
        avisarObservadores();
        return true;
    }

    public Pedido criarPedido(Cliente cliente) {
        if (cliente == null || !usuarios.contains(cliente)) {
            throw new IllegalArgumentException("Cliente nao cadastrado.");
        }

        Pedido pedido = new Pedido(proximoNumeroPedido, cliente);
        proximoNumeroPedido++;
        pedidosAbertos.add(pedido);
        return pedido;
    }

    public boolean adicionarItem(Pedido pedido, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);
        if (pedido == null || !pedidosAbertos.contains(pedido) ||
                produto == null || quantidade <= 0) {
            return false;
        }

        long total = pedido.quantidadeDoProduto(produto) + quantidade;
        if (total > produto.getQuantidade()) {
            throw new EstoqueInsuficienteException(produto.getNome(), produto.getQuantidade());
        }

        pedido.adicionarItem(new ItemPedido(produto, quantidade));
        return true;
    }

    // O carrinho muda a quantidade sem precisar cancelar o pedido.
    public boolean definirQuantidadeItem(Pedido pedido, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);
        if (pedido == null || !pedidosAbertos.contains(pedido) ||
                produto == null || quantidade < 0) {
            return false;
        }
        if (quantidade > produto.getQuantidade()) {
            throw new EstoqueInsuficienteException(produto.getNome(), produto.getQuantidade());
        }
        pedido.definirQuantidade(produto, quantidade);
        return true;
    }

    public boolean cancelarPedido(Pedido pedido) {
        return pedidosAbertos.remove(pedido);
    }

    public boolean finalizarPedido(Pedido pedido, String formaPagamento) {
        try {
            return finalizarPedido(pedido, new Pagamento(formaPagamento));
        } catch (IllegalArgumentException erro) {
            return false;
        }
    }

    public boolean finalizarPedido(Pedido pedido, Pagamento pagamento) {
        if (pedido == null || pagamento == null ||
                !pedidosAbertos.contains(pedido) || pedido.getItens().isEmpty()) {
            return false;
        }

        // Confere todos os itens primeiro, para não baixar só parte do estoque.
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (catalogo.buscarProduto(produto.getCodigo()) != produto) {
                return false;
            }

            if (pedido.quantidadeDoProduto(produto) > produto.getQuantidade()) {
                throw new EstoqueInsuficienteException(produto.getNome(), produto.getQuantidade());
            }
        }

        if (!pagamento.processarPagamento(pedido.calcularTotal())) {
            return false;
        }

        for (ItemPedido item : pedido.getItens()) {
            item.getProduto().retirarEstoque(item.getQuantidade());
        }

        pedido.finalizar(pagamento.getForma());
        pedidosAbertos.remove(pedido);
        pedidosFinalizados.add(pedido);
        pedido.getCliente().adicionarPedido(pedido);
        avisarObservadores();
        return true;
    }

    public ArrayList<Pedido> listarPedidos() {
        return new ArrayList<>(pedidosFinalizados);
    }

    public ArrayList<Pedido> listarPedidos(Cliente cliente) {
        if (cliente == null || !usuarios.contains(cliente)) {
            return new ArrayList<>();
        }
        return cliente.getPedidos();
    }

    public double calcularTotalVendas() {
        double total = 0;
        for (Pedido pedido : pedidosFinalizados) {
            total += pedido.calcularTotal();
        }
        return total;
    }

    private boolean funcionarioValido(Funcionario funcionario) {
        return funcionario != null && usuarios.contains(funcionario);
    }

    public ArrayList<Pedido> listarPedidos(Funcionario funcionario) {
        if (!funcionarioValido(funcionario)) {
            return new ArrayList<>();
        }
        return listarPedidos();
    }

    public String gerarRelatorioVendas() {
        StringBuilder relatorio = new StringBuilder("RELATORIO DE VENDAS\n");

        for (Pedido pedido : pedidosFinalizados) {
            relatorio.append("Pedido ").append(pedido.getNumero())
                     .append(" - Cliente: ").append(pedido.getCliente().getNome())
                     .append(" - Total: R$ ")
                     .append(String.format(Locale.forLanguageTag("pt-BR"),
                             "%.2f", pedido.calcularTotal()))
                     .append("\n");
        }

        relatorio.append("Pedidos finalizados: ").append(pedidosFinalizados.size());
        relatorio.append("\nValor vendido: R$ ")
                 .append(String.format(Locale.forLanguageTag("pt-BR"),
                         "%.2f", calcularTotalVendas()));
        return relatorio.toString();
    }
}