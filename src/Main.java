
import java.util.Scanner;

public class Main {

    private static Scanner teclado = new Scanner(System.in);
    private static Sistema sistema = new Sistema();

    public static void main(String[] args) {
        int opcao;

        do {
            System.out.println("\n=== FARMACIA ONLINE ===");
            System.out.println("1 - Cadastrar cliente");
            System.out.println("2 - Fazer login");
            System.out.println("0 - Sair");

            opcao = lerInteiro("Opcao: ");

            switch (opcao) {
                case 1:
                    cadastrarCliente();
                    break;
                case 2:
                    fazerLogin();
                    break;
                case 0:
                    System.out.println("Ate logo!");
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);

        teclado.close();
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return teclado.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {
        try {
            return Integer.parseInt(lerTexto(mensagem));
        } catch (NumberFormatException erro) {
            return -1;
        }
    }

    private static double lerPreco(String mensagem) {
        try {
            return Double.parseDouble(lerTexto(mensagem).replace(',', '.'));
        } catch (NumberFormatException erro) {
            return -1;
        }
    }

    private static void cadastrarCliente() {
        String nome = lerTexto("Nome: ");
        String cpf = lerTexto("CPF (11 numeros): ");
        String senha = lerTexto("Senha: ");

        try {
            sistema.cadastrarCliente(nome, cpf, senha);
            System.out.println("Cliente cadastrado com sucesso!");
        } catch (IllegalArgumentException erro) {
            System.out.println(erro.getMessage());
        }
    }

    private static void fazerLogin() {
        String cpf = lerTexto("CPF: ");
        String senha = lerTexto("Senha: ");

        Usuario usuario = sistema.fazerLogin(cpf, senha);

        if (usuario == null) {
            System.out.println("CPF ou senha incorretos.");
        } else if (usuario instanceof Cliente) {
            System.out.println("Login realizado: " + usuario.getTipoUsuario());
            menuCliente((Cliente) usuario);
        } else if (usuario instanceof Funcionario) {
            System.out.println("Login realizado: " + usuario.getTipoUsuario());
            menuFuncionario((Funcionario) usuario);
        }
    }

    private static void listarProdutos() {
        System.out.println("\n--- PRODUTOS ---");

        for (Produto produto : sistema.listarProdutos()) {
            String aviso = produto.getQuantidade() == 0 ? "ESGOTADO" :
                           produto.estoqueBaixo() ? "ESTOQUE BAIXO" : "";

            System.out.printf("%d - %s | R$ %.2f | Estoque: %d %s%n",
                    produto.getCodigo(), produto.getNome(), produto.getPreco(),
                    produto.getQuantidade(), aviso);
        }
    }

    private static void mostrarPedido(Pedido pedido) {
        if (pedido == null) {
            System.out.println("Nenhum pedido iniciado.");
            return;
        }

        System.out.println("\nPedido numero " + pedido.getNumero());

        for (ItemPedido item : pedido.getItens()) {
            System.out.printf("%s | %d un. | R$ %.2f%n",
                    item.getProduto().getNome(), item.getQuantidade(),
                    item.calcularSubtotal());
        }

        System.out.printf("TOTAL: R$ %.2f%n", pedido.calcularTotal());
    }

    private static void menuCliente(Cliente cliente) {
        Pedido pedidoAtual = null;
        int opcao;

        do {
            System.out.println("\n--- CLIENTE: " + cliente.getNome() + " ---");
            System.out.println("1 - Ver produtos");
            System.out.println("2 - Iniciar pedido");
            System.out.println("3 - Adicionar produto ao pedido");
            System.out.println("4 - Ver pedido atual");
            System.out.println("5 - Finalizar pedido");
            System.out.println("6 - Historico de pedidos");
            System.out.println("7 - Cancelar pedido atual");
            System.out.println("0 - Sair da conta");

            opcao = lerInteiro("Opcao: ");

            switch (opcao) {
                case 1:
                    listarProdutos();
                    break;

                case 2:
                    if (pedidoAtual != null) {
                        System.out.println("Finalize ou cancele o pedido atual primeiro.");
                    } else {
                        pedidoAtual = sistema.criarPedido(cliente);
                        System.out.println("Pedido iniciado: " + pedidoAtual.getNumero());
                    }
                    break;

                case 3:
                    if (pedidoAtual == null) {
                        System.out.println("Inicie um pedido primeiro.");
                        break;
                    }

                    listarProdutos();

                    int codigo = lerInteiro("Codigo do produto: ");
                    int quantidade = lerInteiro("Quantidade: ");
                    Produto produto = sistema.buscarProduto(codigo);

                    try {
                        pedidoAtual.adicionarItem(produto, quantidade);
                        System.out.println("Produto adicionado!");
                    } catch (IllegalArgumentException erro) {
                        System.out.println(erro.getMessage());
                    }
                    break;

                case 4:
                    mostrarPedido(pedidoAtual);
                    break;

                case 5:
                    if (pedidoAtual == null) {
                        System.out.println("Inicie um pedido primeiro.");
                        break;
                    }

                    mostrarPedido(pedidoAtual);

                    System.out.println("1 - PIX | 2 - CARTAO");
                    int forma = lerInteiro("Pagamento: ");

                    if (forma != 1 && forma != 2) {
                        System.out.println("Forma de pagamento invalida.");
                        break;
                    }

                    Pagamento pagamento = new Pagamento(forma == 1 ? "PIX" : "CARTAO");

                    if (sistema.finalizarPedido(pedidoAtual, pagamento)) {
                        System.out.println("Compra confirmada! Pagamento simulado: "
                                + pagamento.getForma());
                        pedidoAtual = null;
                    } else {
                        System.out.println("Nao foi possivel finalizar. Confira os itens e o estoque.");
                    }
                    break;

                case 6:
                    if (cliente.getPedidos().isEmpty()) {
                        System.out.println("Nenhum pedido concluido.");
                    }

                    for (Pedido p : cliente.getPedidos()) {
                        mostrarPedido(p);
                        System.out.println("Pagamento: " + p.getFormaPagamento());
                    }
                    break;

                case 7:
                    pedidoAtual = null;
                    System.out.println("Pedido cancelado.");
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }

    private static void menuFuncionario(Funcionario funcionario) {
        int opcao;

        do {
            System.out.println("\n--- FUNCIONARIO: " + funcionario.getNome() + " ---");
            System.out.println("1 - Ver produtos e estoque");
            System.out.println("2 - Cadastrar produto");
            System.out.println("3 - Alterar preco");
            System.out.println("4 - Repor estoque");
            System.out.println("5 - Ver pedidos realizados");
            System.out.println("0 - Sair da conta");

            opcao = lerInteiro("Opcao: ");

            switch (opcao) {
                case 1:
                    listarProdutos();
                    break;

                case 2:
                    int codigo = lerInteiro("Codigo: ");
                    String nome = lerTexto("Nome: ");
                    double preco = lerPreco("Preco: ");
                    int quantidade = lerInteiro("Quantidade inicial: ");

                    System.out.println(sistema.cadastrarProduto(funcionario, codigo,
                            nome, preco, quantidade)
                            ? "Produto cadastrado!" : "Nao foi possivel cadastrar.");
                    break;

                case 3:
                    codigo = lerInteiro("Codigo do produto: ");
                    preco = lerPreco("Novo preco: ");

                    System.out.println(sistema.alterarPreco(funcionario, codigo, preco)
                            ? "Preco alterado!" : "Nao foi possivel alterar.");
                    break;

                case 4:
                    codigo = lerInteiro("Codigo do produto: ");
                    quantidade = lerInteiro("Quantidade para repor: ");

                    System.out.println(sistema.reporEstoque(funcionario, codigo, quantidade)
                            ? "Estoque atualizado!" : "Nao foi possivel repor.");
                    break;

                case 5:
                    if (sistema.listarPedidos().isEmpty()) {
                        System.out.println("Nenhum pedido realizado.");
                    }

                    for (Pedido p : sistema.listarPedidos()) {
                        System.out.println("Cliente: " + p.getCliente().getNome());
                        mostrarPedido(p);
                    }
                    break;

                case 0:
                    break;

                default:
                    System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }
}
