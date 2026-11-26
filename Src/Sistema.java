import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
/*
  CADASTRAR CLIENTES, CADASTRAR PRODUTOS,
  REALIZAR PEDIDOS, GERAR RELATÓRIOS DE VENDAS, VER ESTOQUE
*/
public class Sistema{
    //armazena clientes e funcionários
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    //catálogo
    private Catalogo catalogo = new Catalogo();
    private ArrayList<Pedido> pedidosAbertos = new ArrayList<>();
    private ArrayList<Pedido> pedidosFinalizados = new ArrayList<>();
    private int proximoNumeroPedido = 1;

    private static final int IDENTIFICADOR_CLIENTES = 0x46415543;
    private static final int IDENTIFICADOR_PEDIDOS = 0x46415550;
    private static final int VERSAO_DADOS = 1;
    private static final int LIMITE_REGISTROS = 100000;

    private final Path arquivoClientes = Paths.get("dados", "clientes.dat");
    private final Path arquivoPedidos = Paths.get("dados", "pedidos.dat");

    public Sistema(){
        //funcionário administrador
        Funcionario administrador = new Funcionario("Administrador","00000000000","admin123","F01");
        usuarios.add(administrador);

        //Carrega o catalogo salvo. Os produtos iniciais entram apenas no primeiro uso.
        if (!catalogo.carregar()) {
            catalogo.adicionarProduto(new Produto(1, "Dipirona 500mg", 12.50, 5));
            catalogo.adicionarProduto(new Produto(2, "Paracetamol 750mg", 9.90, 10));
            catalogo.adicionarProduto(new Produto(3, "Ibuprofeno 400mg", 15.90, 6));
            catalogo.adicionarProduto(new Produto(4, "Vitamina C", 19.90, 5));
            catalogo.adicionarProduto(new Produto(5, "Soro Fisiológico", 7.50, 10));
            if (!catalogo.salvar()) {
                throw new IllegalStateException("Nao foi possivel criar o arquivo do catalogo.");
            }
        }

        carregarClientes();
        carregarPedidos();
    }
    
    //login usuário
    public Usuario fazerLogin(String cpf, String senha){
        if (cpf == null || senha == null) {
            return null;
        }
        cpf = cpf.trim();
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf) && usuario.getSenha().equals(senha)){
                return usuario;
            }
        }
        return null; //retorna null se não encontrar o usuário
    }

    public Cliente cadastrarCliente(String nome, String cpf, String senha) {
        /*
            if (nome == null || nome.isBlank() || cpf == null || !cpf.matches("\\d{11}") || senha == null || senha.isBlank()) {
                throw new IllegalArgumentException("Dados invalidos!");
            }
        */
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome nao pode estar vazio!");
        }
        nome = nome.trim();
        cpf = cpf == null ? null : cpf.trim();
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("O CPF deve conter 11 numeros!");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha nao pode estar vazia!");
        }
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf)) {
                throw new CpfJaCadastradoException();
            }
        }
        Cliente cliente = new Cliente(nome, cpf, senha);
        usuarios.add(cliente);
        if (!salvarClientes()) {
            usuarios.remove(cliente);
            throw new IllegalStateException("Nao foi possivel salvar o cadastro. Tente novamente.");
        }
        return cliente;
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
        if (!catalogo.salvar()) {
            catalogo.removerProduto(codigo);
            return false;
        }

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
        double precoAnterior = produto.getPreco();
        funcionario.alterarPreco(produto, novoPreco);
        if (!catalogo.salvar()) {
            produto.alterarPreco(precoAnterior);
            return false;
        }

        return true;
    }

    public boolean reporEstoque(Funcionario funcionario, int codigo, int quantidade) {
        Produto produto = catalogo.buscarProduto(codigo);
        if (!funcionarioValido(funcionario) || produto == null ||
                quantidade <= 0 || quantidade > Integer.MAX_VALUE - produto.getQuantidade()) {
            return false;
        }

        produto.adicionarEstoque(quantidade);
        if (!catalogo.salvar()) {
            produto.retirarEstoque(quantidade);
            return false;
        }
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

        if (!catalogo.salvar()) {
            for (ItemPedido item : pedido.getItens()) {
                item.getProduto().adicionarEstoque(item.getQuantidade());
            }
            return false;
        }

        // Salva a compra antes de confirmar na memoria.
        if (!salvarPedidos(pedido, pagamento.getForma())) {
            for (ItemPedido item : pedido.getItens()) {
                item.getProduto().adicionarEstoque(item.getQuantidade());
            }
            catalogo.salvar();
            return false;
        }

        pedido.finalizar(pagamento.getForma());
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

    // Os clientes ficam separados do funcionario administrador, que e criado pelo sistema.
    private void carregarClientes() {
        if (!Files.exists(arquivoClientes)) {
            return;
        }

        try {
            ArrayList<Cliente> carregados = lerClientes(arquivoClientes);
            usuarios.addAll(carregados);
        } catch (IOException | IllegalArgumentException erro) {
            Path backup = arquivoClientes.resolveSibling("clientes.dat.bak");
            guardarArquivoDanificado(arquivoClientes);

            if (Files.exists(backup)) {
                try {
                    ArrayList<Cliente> recuperados = lerClientes(backup);
                    usuarios.addAll(recuperados);
                    copiarArquivo(backup, arquivoClientes);
                    System.err.println("O cadastro foi recuperado do backup de clientes.");
                    return;
                } catch (IOException | IllegalArgumentException erroBackup) {
                    erro.addSuppressed(erroBackup);
                }
            }

            throw new IllegalStateException(
                    "Nao foi possivel carregar os clientes salvos. Confira dados/clientes.dat e o backup.", erro);
        }
    }

    private ArrayList<Cliente> lerClientes(Path arquivo) throws IOException {
        ArrayList<Cliente> carregados = new ArrayList<>();
        Set<String> cpfs = new HashSet<>();

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(arquivo)))) {
            if (entrada.readInt() != IDENTIFICADOR_CLIENTES || entrada.readInt() != VERSAO_DADOS) {
                throw new IOException("Formato do arquivo de clientes desconhecido.");
            }

            int quantidade = entrada.readInt();
            if (quantidade < 0 || quantidade > LIMITE_REGISTROS) {
                throw new IOException("Quantidade de clientes invalida.");
            }

            for (int i = 0; i < quantidade; i++) {
                String nome = entrada.readUTF();
                String cpf = entrada.readUTF();
                String senha = entrada.readUTF();

                if (nome.isBlank() || !cpf.matches("\\d{11}") || senha.isBlank()
                        || cpf.equals("00000000000") || !cpfs.add(cpf)) {
                    throw new IOException("Cadastro invalido no arquivo de clientes.");
                }
                carregados.add(new Cliente(nome, cpf, senha));
            }

            if (entrada.read() != -1) {
                throw new IOException("Existem dados extras no arquivo de clientes.");
            }
        }
        return carregados;
    }

    private boolean salvarClientes() {
        Path temporario = arquivoClientes.resolveSibling("clientes.tmp");
        try {
            Files.createDirectories(arquivoClientes.getParent());

            try (DataOutputStream saida = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporario)))) {
                saida.writeInt(IDENTIFICADOR_CLIENTES);
                saida.writeInt(VERSAO_DADOS);

                int quantidade = 0;
                for (Usuario usuario : usuarios) {
                    if (usuario instanceof Cliente) quantidade++;
                }
                saida.writeInt(quantidade);

                for (Usuario usuario : usuarios) {
                    if (usuario instanceof Cliente) {
                        saida.writeUTF(usuario.getNome());
                        saida.writeUTF(usuario.getCpf());
                        saida.writeUTF(usuario.getSenha());
                    }
                }
            }

            manterBackup(arquivoClientes);
            moverArquivo(temporario, arquivoClientes);
            return true;
        } catch (IOException erro) {
            apagarTemporario(temporario);
            System.err.println("Erro ao salvar os clientes: " + erro.getMessage());
            return false;
        }
    }

    // Recarrega o historico depois que os clientes e o catalogo ja foram carregados.
    private void carregarPedidos() {
        if (!Files.exists(arquivoPedidos)) {
            return;
        }

        try {
            ArrayList<Pedido> carregados = lerPedidos(arquivoPedidos);
            pedidosFinalizados.addAll(carregados);
            for (Pedido pedido : carregados) {
                pedido.getCliente().adicionarPedido(pedido);
                if (pedido.getNumero() >= proximoNumeroPedido) {
                    if (pedido.getNumero() == Integer.MAX_VALUE) {
                        throw new IllegalStateException("O numero maximo de pedidos foi atingido.");
                    }
                    proximoNumeroPedido = pedido.getNumero() + 1;
                }
            }
        } catch (IOException | IllegalArgumentException erro) {
            Path backup = arquivoPedidos.resolveSibling("pedidos.dat.bak");
            guardarArquivoDanificado(arquivoPedidos);

            if (Files.exists(backup)) {
                try {
                    ArrayList<Pedido> recuperados = lerPedidos(backup);
                    pedidosFinalizados.addAll(recuperados);
                    for (Pedido pedido : recuperados) {
                        pedido.getCliente().adicionarPedido(pedido);
                        if (pedido.getNumero() >= proximoNumeroPedido) {
                            proximoNumeroPedido = pedido.getNumero() + 1;
                        }
                    }
                    copiarArquivo(backup, arquivoPedidos);
                    System.err.println("O historico foi recuperado do backup de pedidos.");
                    return;
                } catch (IOException | IllegalArgumentException erroBackup) {
                    erro.addSuppressed(erroBackup);
                }
            }

            throw new IllegalStateException(
                    "Nao foi possivel carregar o historico salvo. Confira dados/pedidos.dat e o backup.", erro);
        }
    }

    private ArrayList<Pedido> lerPedidos(Path arquivo) throws IOException {
        ArrayList<Pedido> carregados = new ArrayList<>();
        Set<Integer> numeros = new HashSet<>();

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(arquivo)))) {
            if (entrada.readInt() != IDENTIFICADOR_PEDIDOS || entrada.readInt() != VERSAO_DADOS) {
                throw new IOException("Formato do arquivo de pedidos desconhecido.");
            }

            int quantidade = entrada.readInt();
            if (quantidade < 0 || quantidade > LIMITE_REGISTROS) {
                throw new IOException("Quantidade de pedidos invalida.");
            }

            for (int i = 0; i < quantidade; i++) {
                int numero = entrada.readInt();
                String cpfCliente = entrada.readUTF();
                String forma = entrada.readUTF();
                int quantidadeItens = entrada.readInt();
                Cliente cliente = buscarClientePorCpf(cpfCliente);

                if (numero <= 0 || !numeros.add(numero) || cliente == null
                        || quantidadeItens <= 0 || quantidadeItens > LIMITE_REGISTROS) {
                    throw new IOException("Pedido invalido no arquivo de historico.");
                }

                Pedido pedido = new Pedido(numero, cliente);
                for (int j = 0; j < quantidadeItens; j++) {
                    int codigo = entrada.readInt();
                    String nomeProduto = entrada.readUTF();
                    int quantidadeProduto = entrada.readInt();
                    double precoUnitario = entrada.readDouble();
                    Produto produtoHistorico = new Produto(codigo, nomeProduto, precoUnitario, 0);
                    pedido.adicionarItem(new ItemPedido(produtoHistorico, quantidadeProduto, precoUnitario));
                }

                pedido.finalizar(forma);
                carregados.add(pedido);
            }

            if (entrada.read() != -1) {
                throw new IOException("Existem dados extras no arquivo de pedidos.");
            }
        }
        return carregados;
    }

    private Cliente buscarClientePorCpf(String cpf) {
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Cliente && usuario.getCpf().equals(cpf)) {
                return (Cliente) usuario;
            }
        }
        return null;
    }

    // O pedido novo ainda esta aberto em memoria; aqui ele ja e gravado como finalizado.
    private boolean salvarPedidos(Pedido pedidoNovo, String formaPagamento) {
        Path temporario = arquivoPedidos.resolveSibling("pedidos.tmp");
        try {
            Files.createDirectories(arquivoPedidos.getParent());

            try (DataOutputStream saida = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporario)))) {
                saida.writeInt(IDENTIFICADOR_PEDIDOS);
                saida.writeInt(VERSAO_DADOS);
                saida.writeInt(pedidosFinalizados.size() + 1);

                for (Pedido pedido : pedidosFinalizados) {
                    escreverPedido(saida, pedido, pedido.getFormaPagamento());
                }
                escreverPedido(saida, pedidoNovo, formaPagamento);
            }

            manterBackup(arquivoPedidos);
            moverArquivo(temporario, arquivoPedidos);
            return true;
        } catch (IOException erro) {
            apagarTemporario(temporario);
            System.err.println("Erro ao salvar o historico: " + erro.getMessage());
            return false;
        }
    }

    private void escreverPedido(DataOutputStream saida, Pedido pedido, String formaPagamento)
            throws IOException {
        saida.writeInt(pedido.getNumero());
        saida.writeUTF(pedido.getCliente().getCpf());
        saida.writeUTF(formaPagamento);
        saida.writeInt(pedido.getItens().size());

        for (ItemPedido item : pedido.getItens()) {
            saida.writeInt(item.getProduto().getCodigo());
            saida.writeUTF(item.getProduto().getNome());
            saida.writeInt(item.getQuantidade());
            saida.writeDouble(item.getPrecoUnitario());
        }
    }

    private void manterBackup(Path arquivo) {
        if (Files.exists(arquivo)) {
            Path backup = arquivo.resolveSibling(arquivo.getFileName().toString() + ".bak");
            try {
                Files.copy(arquivo, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException erro) {
                System.err.println("Nao foi possivel atualizar o backup de " + arquivo.getFileName() + ": " + erro.getMessage());
            }
        }
    }

    private void guardarArquivoDanificado(Path arquivo) {
        Path copia = arquivo.resolveSibling(arquivo.getFileName().toString() + ".corrompido");
        try {
            Files.copy(arquivo, copia, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException erro) {
            System.err.println("Nao foi possivel guardar uma copia do arquivo danificado: " + erro.getMessage());
        }
    }

    private void copiarArquivo(Path origem, Path destino) throws IOException {
        Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    private void moverArquivo(Path origem, Path destino) throws IOException {
        try {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException erro) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void apagarTemporario(Path temporario) {
        try {
            Files.deleteIfExists(temporario);
        } catch (IOException ignorado) {
            // O erro principal ja foi informado.
        }
    }

}