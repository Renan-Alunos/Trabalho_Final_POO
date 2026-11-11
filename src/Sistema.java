import java.util.ArrayList;
import java.util.Locale;
/*
  CADASTRAR CLIENTES, CADASTRAR PRODUTOS,
  REALIZAR PEDIDOS, GERAR RELATÓRIOS DE VENDAS, VER ESTOQUE
*/
public class Sistema{
    //armazena clientes e funcionários
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    //catálogo
    private Catalogo catalogo = new Catalogo();

    public Sistema(){
        //funcionário administrador
        Funcionario administrador = new Funcionario("Administrador","00000000000","admin123","F01");
        usuarios.add(administrador);

        //produtos iniciais
        catalogo.adicionarProduto(new Produto(1, "Dipirona 500mg", 12.50, 5));//(codigo, nome, preço, quantidade)
        catalogo.adicionarProduto(new Produto(2, "Paracetamol 750mg", 9.90, 10));
        catalogo.adicionarProduto(new Produto(3, "Ibuprofeno 400mg", 15.90, 6));
        catalogo.adicionarProduto(new Produto(4, "Vitamina C", 19.90, 5));
        catalogo.adicionarProduto(new Produto(5, "Soro Fisiológico", 7.50, 10));
    }
    
    //login usuário
    public Usuario fazerLogin(String cpf, String senha){
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf) && usuario.getSenha().equals(senha)){
                return usuario;
            }
        }
        return null; //retorna null se não encontrar o usuário
    }

    public void cadastrarCliente(String nome, String cpf, String senha){
        //verifica se o CPF já está cadastrado
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf)){
                System.out.println("CPF já cadastrado!");
                return;
            }
        }
        //cria um novo cliente
        Cliente cliente = new Cliente(nome, cpf, senha);

        //add o cliente na lista de usuários
        usuarios.add(cliente);

        System.out.println("Cliente cadastrado com sucesso!");
    }

    public String buscarClientes(String cpf){ //buscara usuário a partir de seu cpf
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
        //verifica se o funcionário pertence ao sistema
        if (funcionario == null || !usuarios.contains(funcionario)){
            return false;
        }

        //verifica se os dados são válidos
        if (codigo <= 0 || nome == null || nome.isBlank() || !Double.isFinite(preco) || preco <= 0 || quantidade < 0){
            return false;
        }

        //verifica se o código já existe
        if (catalogo.buscarProduto(codigo) != null){
            return false;
        }

        //cria um produto
        Produto produto = new Produto(codigo, nome, preco, quantidade);

        //só o funcionário adiciona o produto ao catálogo
        funcionario.adicionarProduto(catalogo, produto);

        return true;
    }

    public boolean alterarPreco(Funcionario funcionario, int codigo, double novoPreco) {
        //verifica se o funcionário está cadastrado
        if (funcionario == null || !usuarios.contains(funcionario)) {
            return false;
        }

        //verifica se o preço é válido
        if (!Double.isFinite(novoPreco) || novoPreco < 0) {
            return false;
        }

        //busca produto por código
        Produto produto = catalogo.buscarProduto(codigo);

        //verifica se o produto existe
        if (produto == null) {
            return false;
        }

        //altera preço
        funcionario.alterarPreco(produto, novoPreco);

        return true;
    }

    public boolean reporEstoque(Funcionario funcionario, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);
        if (!funcionarioValido(funcionario) || produto == null ||
                quantidade <= 0 || quantidade > Integer.MAX_VALUE - produto.getQuantidade()) {
            return false;
        }

        produto.adicionarEstoque(quantidade);
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

        long total = quantidade;
        for (ItemPedido item : pedido.getItens()) {
            if (item.getProduto() == produto) {
                total += item.getQuantidade();
            }
        }

        if (total > produto.getQuantidade()) {
            return false;
        }

        pedido.adicionarItem(new ItemPedido(produto, quantidade));
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

        // Confere todo o estoque antes de descontar qualquer produto.
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (catalogo.buscarProduto(produto.getCodigo()) != produto) {
                return false;
            }

            long totalDoProduto = 0;
            for (ItemPedido outro : pedido.getItens()) {
                if (outro.getProduto() == produto) {
                    totalDoProduto += outro.getQuantidade();
                }
            }

            if (totalDoProduto > produto.getQuantidade()) {
                return false;
            }
        }

        if (!pagamento.processarPagamento(pedido.calcularTotal())) {
            return false;
        }

        for (ItemPedido item : pedido.getItens()) {
            item.getProduto().retirarEstoque(item.getQuantidade());
        }

        pedidosAbertos.remove(pedido);
        pedidosFinalizados.add(pedido);
        pedido.getCliente().adicionarPedido(pedido);
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