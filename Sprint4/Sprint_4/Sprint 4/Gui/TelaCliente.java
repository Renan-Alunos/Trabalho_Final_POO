import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Mostra os produtos e o carrinho, mas deixa as regras de compra no Sistema.
public class TelaCliente extends JPanel {
    private final Sistema sistema;
    private final Cliente clienteAtual;
    private final Runnable onSair;
    private Pedido pedidoAtual;
    private final Map<Integer, Integer> carrinho = new LinkedHashMap<>();
    private JPanel painelProdutos;
    private JTextArea resumoCarrinho;
    private JLabel valorTotal;
    private JComboBox<String> pagamento;
    private final Color azul = new Color(29, 74, 181);
    private final ObservadorSistema observador = () ->
            SwingUtilities.invokeLater(this::atualizarTelaCliente);

    public TelaCliente(Sistema sistema, Cliente clienteAtual, Runnable onSair) {
        super(new BorderLayout());
        this.sistema = sistema;
        this.clienteAtual = clienteAtual;
        this.onSair = onSair;
        montarTela();
        // Atualiza a tela quando o estoque ou os pedidos mudam.
        sistema.adicionarObservador(observador);
    }

    private JLabel titulo(String texto) {
        JLabel label = new JLabel(texto, JLabel.CENTER);
        label.setFont(new Font("Times New Roman", Font.BOLD, 29));
        label.setForeground(azul);
        return label;
    }

    private JButton botao(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(azul);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

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

        painelProdutos = new JPanel();
        painelProdutos.setLayout(new BoxLayout(painelProdutos, BoxLayout.Y_AXIS));
        JScrollPane rolagem = new JScrollPane(painelProdutos);
        rolagem.setBorder(BorderFactory.createTitledBorder("Produtos disponiveis"));
        rolagem.getVerticalScrollBar().setUnitIncrement(12);
        principal.add(rolagem, BorderLayout.CENTER);

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

    private void atualizarTelaCliente() {
        painelProdutos.removeAll();
        for (Produto produto : sistema.listarProdutos()) {
            JPanel linha = new JPanel(new BorderLayout(10, 4));
            linha.setBorder(new EmptyBorder(9, 10, 9, 10));
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
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

    private String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }

    private void atualizarResumo() {
        StringBuilder texto = new StringBuilder();
        // Pego o total do pedido para não mostrar um valor diferente no carrinho.
        if (pedidoAtual != null) {
            for (ItemPedido item : pedidoAtual.getItens()) {
                texto.append(item.getQuantidade()).append(" x ")
                     .append(item.getProduto().getNome()).append("\n");
                texto.append("    ").append(dinheiro(item.calcularSubtotal()))
                     .append("\n\n");
            }
        }
        resumoCarrinho.setText(texto.length() == 0 ? "Carrinho vazio." : texto.toString());
        valorTotal.setText("Total: " + dinheiro(pedidoAtual == null ? 0 : pedidoAtual.calcularTotal()));
    }

    private void alterarQuantidade(Produto produto, int diferenca) {
        int atual = carrinho.getOrDefault(produto.getCodigo(), 0);
        int novo = atual + diferenca;
        if (novo < 0) return;
        // A tela pede a mudança, e o Sistema confere se a quantidade é válida.
        try {
            if (pedidoAtual == null && novo > 0) {
                pedidoAtual = sistema.criarPedido(clienteAtual);
            }
            if (pedidoAtual != null && !sistema.definirQuantidadeItem(
                    pedidoAtual, produto.getCodigo(), novo)) {
                throw new IllegalStateException("Nao foi possivel atualizar o pedido.");
            }
            if (novo == 0) carrinho.remove(produto.getCodigo());
            else carrinho.put(produto.getCodigo(), novo);
            if (carrinho.isEmpty() && pedidoAtual != null) {
                sistema.cancelarPedido(pedidoAtual);
                pedidoAtual = null;
            }
        } catch (EstoqueInsuficienteException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        atualizarTelaCliente();
    }

    private void finalizarPedido() {
        if (pedidoAtual == null || carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione produtos ao carrinho primeiro.");
            return;
        }
        String forma = pagamento.getSelectedItem().equals("PIX") ? "PIX" : "CARTAO";
        double total = pedidoAtual.calcularTotal();
        int numero = pedidoAtual.getNumero();
        boolean ok;
        try {
            ok = sistema.finalizarPedido(pedidoAtual, forma);
        } catch (EstoqueInsuficienteException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }
        if (ok) {
            JOptionPane.showMessageDialog(this, "Pedido " + numero + " finalizado!\nTotal: " + dinheiro(total)
                    + "\nPagamento: " + forma + " (simulado)");
            pedidoAtual = null;
            carrinho.clear();
            atualizarTelaCliente();
        } else {
            JOptionPane.showMessageDialog(this, "Nao foi possivel finalizar o pedido.");
        }
    }

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

    private void sairDaConta() {
        sistema.removerObservador(observador);
        if (pedidoAtual != null) sistema.cancelarPedido(pedidoAtual);
        pedidoAtual = null;
        carrinho.clear();
        onSair.run();
    }

}
