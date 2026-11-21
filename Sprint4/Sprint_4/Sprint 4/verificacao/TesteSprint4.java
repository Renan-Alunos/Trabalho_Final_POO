import java.util.concurrent.atomic.AtomicInteger;

public class TesteSprint4 {
    private static int verificacoes = 0;

    private static void conferir(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void esperarEstoqueInsuficiente(Runnable acao) {
        try {
            acao.run();
            throw new AssertionError("Era esperada EstoqueInsuficienteException.");
        } catch (EstoqueInsuficienteException erro) {
            conferir(erro.getMessage().contains("Estoque insuficiente"),
                    "Excecao personalizada com mensagem para o usuario");
        }
    }

    public static void main(String[] args) {
        Sistema sistema = new Sistema();
        conferir(sistema.listarProdutos().size() == 5, "Catalogo inicial");
        Funcionario gerente = (Funcionario) sistema.fazerLogin("00000000000", "admin123");
        conferir(gerente != null, "Login do funcionario");
        conferir(sistema.fazerLogin("00000000000", "senha-errada") == null,
                "Senha incorreta nao autentica");

        Cliente ana = sistema.cadastrarCliente("Ana", "11122233344", "senha123");
        Cliente joao = sistema.cadastrarCliente("Joao", "22233344455", "senha456");
        conferir(sistema.fazerLogin("11122233344", "senha123") == ana,
                "Login do cliente");
        try {
            sistema.cadastrarCliente("Outro", "11122233344", "senha");
            throw new AssertionError("CPF repetido deveria falhar");
        } catch (IllegalArgumentException e) {
            conferir(e.getMessage().contains("CPF"), "CPF duplicado com mensagem clara");
        }
        try {
            sistema.cadastrarCliente("Nome", "123", "senha");
            throw new AssertionError("CPF curto deveria falhar");
        } catch (IllegalArgumentException e) {
            conferir(true, "Validacao do formato do CPF");
        }

        AtomicInteger notificacoes = new AtomicInteger();
        ObservadorSistema observador = notificacoes::incrementAndGet;
        sistema.adicionarObservador(observador);
        conferir(sistema.cadastrarProduto(gerente, 9, "Creme", 8.0, 2),
                "Funcionario cadastra produto");
        conferir(!sistema.cadastrarProduto(gerente, 9, "Repetido", 8.0, 2),
                "Produto duplicado nao e cadastrado");
        conferir(sistema.alterarPreco(gerente, 9, 9.5), "Preco alterado");
        conferir(sistema.reporEstoque(gerente, 9, 3), "Estoque reposto");
        conferir(notificacoes.get() == 3, "Observer notifica alteracoes reais");
        conferir(!sistema.reporEstoque(gerente, 9, -1), "Reposicao negativa rejeitada");
        Produto creme = sistema.listarProdutos().stream()
                .filter(p -> p.getCodigo() == 9).findFirst().orElseThrow();
        conferir(creme.getQuantidade() == 5 && creme.getPreco() == 9.5,
                "Dados finais do produto correto");
        try {
            creme.alterarPreco(Double.NaN);
            throw new AssertionError("Preco NaN deveria falhar");
        } catch (IllegalArgumentException e) {
            conferir(creme.getPreco() == 9.5, "Preco invalido nao altera produto");
        }

        // Confere as duas formas de pagamento e a criação dos processadores.
        conferir(new CriadorPix().criar() instanceof PagamentoPix, "Factory Method PIX");
        conferir(new CriadorCartao().criar() instanceof PagamentoCartao,
                "Factory Method CARTAO");
        conferir("PIX".equals(new Pagamento(" pix ").getForma()), "Strategy PIX");
        conferir("CARTAO".equals(new Pagamento("cartao").getForma()),
                "Strategy CARTAO");
        conferir(!new Pagamento("PIX").processarPagamento(0),
                "Pagamento simulado rejeita total zero");

        Pedido primeiro = sistema.criarPedido(ana);
        conferir(sistema.adicionarItem(primeiro, 9, 2), "Adicao de item no pedido");
        conferir(sistema.definirQuantidadeItem(primeiro, 9, 3),
                "Atualizacao da quantidade sem recriar pedido");
        conferir(primeiro.getNumero() == 1, "Numero do pedido preservado");
        conferir(primeiro.quantidadeDoProduto(creme) == 3, "Quantidade atualizada");
        conferir(Math.abs(primeiro.calcularTotal() - 28.5) < 0.0001,
                "Total calculado");
        conferir(!sistema.finalizarPedido(primeiro, "boleto"),
                "Pagamento invalido nao encerra o pedido");
        conferir("ABERTO".equals(primeiro.getSituacao()), "Pedido continua aberto");
        conferir(sistema.finalizarPedido(primeiro, "PIX"), "Pedido finalizado por PIX");
        conferir(creme.getQuantidade() == 2, "Estoque descontado apenas no final");
        conferir(notificacoes.get() == 4, "Observer notifica a venda");
        conferir(sistema.listarPedidos(ana).size() == 1, "Historico do cliente");
        conferir(sistema.gerarRelatorioVendas().contains("28,50"),
                "Relatorio de vendas atualizado");
        conferir(!sistema.finalizarPedido(primeiro, "PIX"),
                "Nao finaliza o mesmo pedido duas vezes");

        Pedido segundo = sistema.criarPedido(joao);
        esperarEstoqueInsuficiente(() -> sistema.adicionarItem(segundo, 9, 3));
        conferir(segundo.getItens().isEmpty(), "Estoque invalido nao adiciona item");
        conferir(sistema.adicionarItem(segundo, 9, 1), "Sistema permanece funcional");
        esperarEstoqueInsuficiente(() -> sistema.definirQuantidadeItem(segundo, 9, 30));
        conferir(segundo.quantidadeDoProduto(creme) == 1, "Falha nao corrompe pedido");
        conferir(sistema.cancelarPedido(segundo), "Cancelamento de pedido");

        // Os dois clientes podem montar carrinhos com o mesmo produto.
        // Na finalização, o segundo precisa respeitar o estoque que sobrou.
        Sistema outraFarmacia = new Sistema();
        Cliente c1 = outraFarmacia.cadastrarCliente("C1", "33344455566", "123");
        Cliente c2 = outraFarmacia.cadastrarCliente("C2", "44455566677", "123");
        Pedido p1 = outraFarmacia.criarPedido(c1);
        Pedido p2 = outraFarmacia.criarPedido(c2);
        conferir(outraFarmacia.adicionarItem(p1, 1, 4), "Carrinho 1");
        conferir(outraFarmacia.adicionarItem(p2, 1, 4), "Carrinho 2");
        conferir(outraFarmacia.finalizarPedido(p1, "CARTAO"), "Primeira compra");
        esperarEstoqueInsuficiente(() -> outraFarmacia.finalizarPedido(p2, "PIX"));
        conferir("ABERTO".equals(p2.getSituacao()), "Falha nao encerra pedido 2");
        conferir(outraFarmacia.listarProdutos().get(0).getQuantidade() == 1,
                "Estoque permanece correto apos erro");
        conferir(outraFarmacia.definirQuantidadeItem(p2, 1, 1),
                "Cliente ajusta quantidade apos erro");
        conferir(outraFarmacia.finalizarPedido(p2, "PIX"),
                "Cliente consegue finalizar apos corrigir");
        conferir(outraFarmacia.listarProdutos().get(0).getQuantidade() == 0,
                "Estoque zerado sem valores negativos");
        sistema.removerObservador(observador);
        conferir(sistema.reporEstoque(gerente, 9, 1), "Reposicao apos remover observador");
        conferir(notificacoes.get() == 4, "Observer cancelado nao e notificado");

        System.out.println("Sprint 4: " + verificacoes + " verificacoes passaram.");
    }
}
