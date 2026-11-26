import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static Scanner teclado = new Scanner(System.in);
    private static Sistema sistema = new Sistema();

    public static void main(String[] args) { //opçoes iniciais
        System.out.println("=== FARMACIA ONLINE ===");
        System.out.println("Sistema de compras");
        int opcao;
        do {
            System.out.println("\n1 - Fazer login");
            System.out.println("2 - Cadastrar cliente");
            System.out.println("3 - Consultar produtos");
            System.out.println("0 - Sair");
            opcao = lerInteiro("Escolha: ");
            try {
                switch (opcao) {
                    case 1: fazerLogin(); break;
                    case 2: cadastrarCliente(); break;
                    case 3: mostrarProdutos(); break;
                    case 0: System.out.println("Programa encerrado."); break;
                    default: System.out.println("Opcao invalida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Aviso: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void cadastrarCliente() {//cadastrar cliente
        String nome = lerTexto("Nome: ");
        String cpf = lerTexto("CPF (11 numeros): ");
        String senha = lerTexto("Senha: ");
        Cliente cliente = sistema.cadastrarCliente(nome, cpf, senha);
        System.out.println("Cliente cadastrado: " + cliente.getNome());
    }

    private static void fazerLogin() {//logar cliente/funcionario
        String cpf = lerTexto("CPF: ");
        String senha = lerTexto("Senha: ");
        Usuario usuario = sistema.fazerLogin(cpf, senha);
        if (usuario == null) {
            System.out.println("CPF ou senha incorretos.");
        } else if (usuario instanceof Funcionario) {
            System.out.println("Bem-vindo, " + usuario.getNome() + " (" + usuario.getTipoUsuario() + ")!");
            menuFuncionario((Funcionario) usuario);
        } else if (usuario instanceof Cliente) {
            System.out.println("Bem-vindo, " + usuario.getNome() + " (" + usuario.getTipoUsuario() + ")!");
            menuCliente((Cliente) usuario);
        }
    }

    private static void menuFuncionario(Funcionario funcionario) {//opçoes que aparecem apenas para o funcionario
        int opcao;
        do {
            System.out.println("\n--- MENU DO FUNCIONARIO ---");
            System.out.println("1 - Ver produtos e estoque");
            System.out.println("2 - Cadastrar produto");
            System.out.println("3 - Alterar preco");
            System.out.println("4 - Repor estoque");
            System.out.println("5 - Ver pedidos");
            System.out.println("0 - Sair da conta");
            opcao = lerInteiro("Escolha: ");
            try {
                switch (opcao) {
                    case 1:
                        mostrarProdutos();
                        break;
                    case 2:
                        int codigo = lerInteiro("Codigo: ");
                        String nome = lerTexto("Nome do produto: ");
                        double preco = lerDecimal("Preco: ");
                        int quantidade = lerInteiro("Estoque inicial: ");
                        boolean cadastrado = sistema.cadastrarProduto(funcionario, codigo, nome, preco, quantidade);
                        System.out.println(cadastrado ? "Produto cadastrado." : "Nao foi possivel cadastrar o produto.");
                        break;
                    case 3:
                        int codigoPreco = lerInteiro("Codigo: ");
                        double novoPreco = lerDecimal("Novo preco: ");
                        boolean alterado = sistema.alterarPreco(funcionario, codigoPreco, novoPreco);
                        System.out.println(alterado ? "Preco alterado." : "Nao foi possivel alterar o preco.");
                        break;
                    case 4:
                        int codigoEstoque = lerInteiro("Codigo: ");
                        int reposicao = lerInteiro("Quantidade a adicionar: ");
                        boolean reposto = sistema.reporEstoque(funcionario, codigoEstoque, reposicao);
                        System.out.println(reposto ? "Estoque atualizado." : "Nao foi possivel repor o estoque.");
                        break;
                    case 5:
                        mostrarPedidos(sistema.listarPedidos(funcionario));
                        break;
                    case 0:
                        System.out.println("Saindo da conta do funcionario.");
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Aviso: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void menuCliente(Cliente cliente) {//opçoes que aparecem para o cliente
        Pedido pedidoAtual = null;
        int opcao;
        do {
            System.out.println("\n--- MENU DO CLIENTE ---");
            System.out.println("1 - Ver produtos");
            System.out.println("2 - Iniciar um pedido");
            System.out.println("3 - Adicionar produto ao pedido");
            System.out.println("4 - Ver pedido atual");
            System.out.println("5 - Finalizar pedido");
            System.out.println("6 - Meus pedidos");
            System.out.println("0 - Sair da conta");
            opcao = lerInteiro("Escolha: ");
            try {
                switch (opcao) {
                    case 1:
                        mostrarProdutos();
                        break;
                    case 2:
                        if (pedidoAtual != null && "ABERTO".equals(pedidoAtual.getSituacao())) {
                            System.out.println("Voce ja tem um pedido aberto nesta sessao.");
                        } else {
                            pedidoAtual = sistema.criarPedido(cliente);
                            System.out.println("Pedido " + pedidoAtual.getNumero() + " iniciado.");
                        }
                        break;
                    case 3:
                        exigirPedido(pedidoAtual);
                        mostrarProdutos();
                        int codigo = lerInteiro("Codigo do produto: ");
                        int quantidade = lerInteiro("Quantidade: ");
                        boolean adicionado = sistema.adicionarItem(pedidoAtual, codigo, quantidade);
                        System.out.println(adicionado ? "Produto adicionado ao pedido." : "Nao foi possivel adicionar o produto.");
                        break;
                    case 4:
                        exigirPedido(pedidoAtual);
                        mostrarDetalhesPedido(pedidoAtual);
                        break;
                    case 5:
                        exigirPedido(pedidoAtual);
                        mostrarDetalhesPedido(pedidoAtual);
                        String forma = lerTexto("Pagamento (PIX ou CARTAO): ");
                        boolean finalizado = sistema.finalizarPedido(pedidoAtual, forma);
                        if (finalizado) {
                            System.out.println("Compra confirmada! Pagamento simulado: "
                                    + pedidoAtual.getFormaPagamento());
                            pedidoAtual = null;
                        } else {
                            System.out.println("Nao foi possivel finalizar o pedido.");
                        }
                        break;
                    case 6:
                        mostrarPedidos(sistema.listarPedidos(cliente));
                        break;
                    case 0:
                        System.out.println("Saindo da conta do cliente.");
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Aviso: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void exigirPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalStateException("Inicie um pedido primeiro (opcao 2).");
        }
    }

    private static void mostrarProdutos() {//imprime na tela os produtos
        System.out.println("\n--- CATALOGO ---");
        ArrayList<Produto> produtos = sistema.listarProdutos();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (Produto produto : produtos) {
           System.out.println("Codigo " + produto.getCodigo() + " | " + produto.getNome() + " | R$ " + dinheiro(produto.getPreco()) + " | Estoque: " + produto.getQuantidade());
        }
    }

    private static void mostrarDetalhesPedido(Pedido pedido) {
        System.out.println("\nPedido numero " + pedido.getNumero() + " | Cliente: " + pedido.getCliente().getNome() + " | Situacao: " + pedido.getSituacao());
        if (pedido.getItens().isEmpty()) {
            System.out.println("Nenhum item adicionado.");
        }
        for (ItemPedido item : pedido.getItens()) {
            System.out.println(item.getProduto().getNome() + " - " + item.getQuantidade() + " x R$ " + dinheiro(item.getPrecoUnitario()) + " = R$ " + dinheiro(item.calcularSubtotal()));
        }
        System.out.println("TOTAL: R$ " + dinheiro(pedido.calcularTotal()));
    }

    private static void mostrarPedidos(ArrayList<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido encontrado.");
        } else {
            for (Pedido pedido : pedidos) {
                mostrarDetalhesPedido(pedido);
            }
        }
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String linha = teclado.nextLine();
            try {
                return Integer.parseInt(linha.trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro valido.");
            }
        }
    }

    private static double lerDecimal(String mensagem) {
    while (true) {
        System.out.print(mensagem);
        String linha = teclado.nextLine().trim().replace(',', '.');

        try {
            double valor = Double.parseDouble(linha);

            if (Double.isFinite(valor)) {
                return valor;
            }

        } catch (NumberFormatException e) {
            // Valor invalido
        }

        System.out.println("Digite um valor numerico valido.");
    }
}

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return teclado.nextLine().trim();
    }

    private static String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

}