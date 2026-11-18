import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

// Tela do cliente. Usa a logica existente no Sistema.java.
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

    public TelaCliente(Sistema sistema, Cliente clienteAtual, Runnable onSair) {
        super(new BorderLayout());
        this.sistema = sistema;
        this.clienteAtual = clienteAtual;
        this.onSair = onSair;
        montarTela();
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

    private void alterarQuantidade(Produto produto, int diferenca) {
        int atual = carrinho.getOrDefault(produto.getCodigo(), 0);
        int novo = atual + diferenca;
        if (novo < 0) return;
        if (novo > produto.getQuantidade()) {
            JOptionPane.showMessageDialog(this, "Quantidade indisponivel para compra.");
            return;
        }
        if (novo == 0) carrinho.remove(produto.getCodigo());
        else carrinho.put(produto.getCodigo(), novo);

        // O Pedido nao possui metodo para remover itens. Por isso a tela
        // cancela o pedido aberto e cria outro com as quantidades atualizadas.
        if (pedidoAtual != null) {
            sistema.cancelarPedido(pedidoAtual);
            pedidoAtual = null;
        }
        if (!carrinho.isEmpty()) {
            pedidoAtual = sistema.criarPedido(clienteAtual);
            for (Produto p : sistema.listarProdutos()) {
                int qtd = carrinho.getOrDefault(p.getCodigo(), 0);
                if (qtd > 0 && !sistema.adicionarItem(pedidoAtual, p.getCodigo(), qtd)) {
                    JOptionPane.showMessageDialog(this, "Nao foi possivel atualizar o pedido.");
                    sistema.cancelarPedido(pedidoAtual);
                    pedidoAtual = null;
                    carrinho.clear();
                    break;
                }
            }
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
        boolean ok = sistema.finalizarPedido(pedidoAtual, forma);
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
        if (pedidoAtual != null) sistema.cancelarPedido(pedidoAtual);
        pedidoAtual = null;
        carrinho.clear();
        onSair.run();
    }

}
