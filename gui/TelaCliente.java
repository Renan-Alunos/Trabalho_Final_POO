import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Aqui fica a tela que o cliente usa depois de entrar na conta.
// As compras e os pedidos continuam sendo tratados pela classe Sistema.
public class TelaCliente extends JPanel {
    // Guardo o sistema e o cliente que fez login.
    private final Sistema sistema;
    private final Cliente clienteAtual;
    private final Runnable onSair;
    // O carrinho guarda o codigo do produto e a quantidade escolhida.
    private final Map<Integer, Integer> carrinho = new LinkedHashMap<>();
    private JPanel painelProdutos;
    private JTextArea resumoCarrinho;
    private JLabel valorTotal;
    private JComboBox<String> pagamento;
    private final Color azul = new Color(29, 74, 181);

    // Recebo as informacoes do login e monto a tela do cliente.
    public TelaCliente(Sistema sistema, Cliente clienteAtual, Runnable onSair) {
        super(new BorderLayout());
        this.sistema = sistema;
        this.clienteAtual = clienteAtual;
        this.onSair = onSair;
        montarTela();
    }

    // Uso este metodo pra manter o mesmo estilo nos titulos.
    private JLabel titulo(String texto) {
        JLabel label = new JLabel(texto, JLabel.CENTER);
        label.setFont(new Font("Times New Roman", Font.BOLD, 29));
        label.setForeground(azul);
        return label;
    }

    // Deixo os botoes principais com a mesma cor.
    private JButton botao(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(azul);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    // Aqui monto a tela: botoes em cima, produtos no meio e carrinho ao lado.
    private void montarTela() {
        JPanel principal = new JPanel(new BorderLayout(12, 12));
        principal.setBorder(new EmptyBorder(12, 14, 12, 14));
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(titulo("FARMA AURA"), BorderLayout.CENTER);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton historico = botao("Historico de compras");
        historico.addActionListener(e -> mostrarHistorico());
        JButton sair = new JButton("Sair da conta");
        sair.addActionListener(e -> sairDaConta());
        acoes.add(historico);
        acoes.add(sair);
        topo.add(acoes, BorderLayout.EAST);
        principal.add(topo, BorderLayout.NORTH);

        // A lista fica com rolagem pra caber todos os medicamentos.
        painelProdutos = new JPanel();
        painelProdutos.setLayout(new BoxLayout(painelProdutos, BoxLayout.Y_AXIS));
        JScrollPane rolagem = new JScrollPane(painelProdutos);
        rolagem.setBorder(BorderFactory.createTitledBorder("Produtos disponiveis"));
        rolagem.getVerticalScrollBar().setUnitIncrement(12);
        principal.add(rolagem, BorderLayout.CENTER);

        // O carrinho fica na lateral, separado da lista de produtos.
        JPanel lateral = new JPanel(new BorderLayout(8, 8));
        lateral.setPreferredSize(new Dimension(245, 0));
        lateral.setBorder(BorderFactory.createTitledBorder("Carrinho de compras"));
        JLabel carrinhoTitulo = new JLabel("\uD83D\uDED2 Meu carrinho", JLabel.CENTER);
        carrinhoTitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        lateral.add(carrinhoTitulo, BorderLayout.NORTH);
        resumoCarrinho = new JTextArea();
        resumoCarrinho.setEditable(false);
        resumoCarrinho.setLineWrap(true);
        resumoCarrinho.setWrapStyleWord(true);
        resumoCarrinho.setBackground(lateral.getBackground());
        resumoCarrinho.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lateral.add(new JScrollPane(resumoCarrinho), BorderLayout.CENTER);

        // No final do carrinho mostro o total e a forma de pagamento.
        JPanel finalizar = new JPanel(new GridLayout(5, 1, 5, 6));
        valorTotal = new JLabel("Total: R$ 0,00", JLabel.CENTER);
        valorTotal.setFont(new Font("SansSerif", Font.BOLD, 17));
        finalizar.add(valorTotal);
        finalizar.add(new JLabel("Forma de pagamento:"));
        pagamento = new JComboBox<>(new String[] { "PIX", "Cartao" });
        finalizar.add(pagamento);
        JButton comprar = botao("Finalizar pedido");
        comprar.addActionListener(e -> finalizarPedido());
        finalizar.add(comprar);
        finalizar.add(new JLabel("Pagamento simulado", JLabel.CENTER));
        lateral.add(finalizar, BorderLayout.SOUTH);
        principal.add(lateral, BorderLayout.EAST);
        atualizarTelaCliente();
        add(principal, BorderLayout.CENTER);
    }

    // Refaco a lista quando alguem aperta + ou - para mostrar a quantidade certa.
    private void atualizarTelaCliente() {
        painelProdutos.removeAll();
        for (Produto produto : sistema.listarProdutos()) {
            JPanel linha = new JPanel(new BorderLayout(10, 4));
            linha.setBorder(new EmptyBorder(9, 10, 9, 10));
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
            // O cliente ve apenas nome e preco. O estoque nao aparece aqui.
            JPanel descricao = new JPanel(new GridLayout(2, 1));
            descricao.add(new JLabel(produto.getNome()));
            descricao.add(new JLabel(dinheiro(produto.getPreco())));
            linha.add(descricao, BorderLayout.CENTER);

            JPanel quantidades = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton menos = new JButton("−");
            menos.addActionListener(e -> alterarQuantidade(produto, -1));
            quantidades.add(menos);
            quantidades.add(new JLabel(" " + carrinho.getOrDefault(produto.getCodigo(), 0) + " "));
            JButton mais = new JButton("+");
            mais.addActionListener(e -> alterarQuantidade(produto, 1));
            quantidades.add(mais);
            linha.add(quantidades, BorderLayout.EAST);
            painelProdutos.add(linha);
            painelProdutos.add(new JSeparator());
        }
        painelProdutos.revalidate();
        painelProdutos.repaint();
        atualizarResumo();
    }

    // Formato os valores em reais para mostrar na tela.
    private String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }

    // Somo os produtos escolhidos e atualizo o valor do carrinho.
    private void atualizarResumo() {
        StringBuilder texto = new StringBuilder();
        double total = 0;
        for (Produto p : sistema.listarProdutos()) {
            int qtd = carrinho.getOrDefault(p.getCodigo(), 0);
            if (qtd > 0) {
                texto.append(qtd).append(" x ").append(p.getNome()).append("\n");
                texto.append("    ").append(dinheiro(qtd * p.getPreco())).append("\n\n");
                total += qtd * p.getPreco();
            }
        }
        resumoCarrinho.setText(texto.length() == 0 ? "Carrinho vazio." : texto.toString());
        valorTotal.setText("Total: " + dinheiro(total));
    }

    // O + adiciona uma unidade e o - retira uma.
    private void alterarQuantidade(Produto produto, int diferenca) {
        int atual = carrinho.getOrDefault(produto.getCodigo(), 0);
        int novo = atual + diferenca;
        if (novo < 0) return;
        // Mesmo sem mostrar o estoque, nao deixo passar do disponivel.
        if (novo > produto.getQuantidade()) {
            JOptionPane.showMessageDialog(this, "Quantidade indisponivel para compra.");
            return;
        }
        if (novo == 0) carrinho.remove(produto.getCodigo());
        else carrinho.put(produto.getCodigo(), novo);

        // O carrinho e so uma lista na tela. O Pedido so e criado ao finalizar.
        atualizarTelaCliente();
    }

    // Confiro se tem itens, crio o pedido so agora e finalizo usando o Sistema.
    private void finalizarPedido() {
        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione produtos ao carrinho primeiro.");
            return;
        }
        // PIX e cartao sao opcoes de pagamento simulado.
        String forma = pagamento.getSelectedItem().equals("PIX") ? "PIX" : "CARTAO";

        // O pedido nasce aqui, por isso o numero fica em sequencia (1, 2, 3...).
        Pedido pedido = sistema.criarPedido(clienteAtual);
        for (Map.Entry<Integer, Integer> item : carrinho.entrySet()) {
            if (!sistema.adicionarItem(pedido, item.getKey(), item.getValue())) {
                sistema.cancelarPedido(pedido);
                JOptionPane.showMessageDialog(this, "Nao foi possivel adicionar os itens ao pedido.");
                return;
            }
        }

        double total = pedido.calcularTotal();
        int numero = pedido.getNumero();
        if (sistema.finalizarPedido(pedido, forma)) {
            JOptionPane.showMessageDialog(this, "Pedido " + numero + " finalizado!\nTotal: " + dinheiro(total)
                    + "\nPagamento: " + forma + " (simulado)");
            carrinho.clear();
            atualizarTelaCliente();
        } else {
            sistema.cancelarPedido(pedido);
            JOptionPane.showMessageDialog(this, "Nao foi possivel finalizar o pedido.");
        }
    }

    // Busco os pedidos desse cliente e mostro os detalhes numa janela.
    private void mostrarHistorico() {
        ArrayList<Pedido> pedidos = sistema.listarPedidos(clienteAtual);
        if (pedidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Voce ainda nao realizou compras.");
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (Pedido p : pedidos) {
            texto.append("Pedido ").append(p.getNumero()).append(" - ").append(p.getSituacao()).append("\n");
            for (ItemPedido item : p.getItens()) {
                texto.append("  ").append(item.getQuantidade()).append(" x ")
                     .append(item.getProduto().getNome()).append("\n");
            }
            texto.append("Total: ").append(dinheiro(p.calcularTotal())).append("\n")
                 .append("Pagamento: ").append(p.getFormaPagamento()).append("\n\n");
        }
        JTextArea area = new JTextArea(texto.toString(), 16, 40);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Historico de compras", JOptionPane.INFORMATION_MESSAGE);
    }

    // Ao sair, limpo o carrinho e volto para a tela de login.
    private void sairDaConta() {
        carrinho.clear();
        onSair.run();
    }

}
