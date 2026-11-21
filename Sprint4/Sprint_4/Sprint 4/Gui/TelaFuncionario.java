import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Locale;

public class TelaFuncionario extends JFrame {

    private final Sistema sistema;
    private final Funcionario funcionario;
    private final TelaFarmacia telaLogin;

    private final JTabbedPane abas = new JTabbedPane();

    // Tabela dos produtos cadastrados, só para consulta.
    private final DefaultTableModel modeloProdutos = new DefaultTableModel(
            new String[]{"Codigo", "Produto", "Preco", "Estoque"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };

    // Campos usados no cadastro de produtos.
    private final JTextField campoCadCodigo = new JTextField();
    private final JTextField campoCadNome = new JTextField();
    private final JTextField campoCadPreco = new JTextField();
    private final JTextField campoCadEstoque = new JTextField();

    // Campos para atualizar o preço de um produto.
    private final JTextField campoPrecoCodigo = new JTextField();
    private final JTextField campoPrecoNovo = new JTextField();

    // Campos para adicionar unidades ao estoque.
    private final JTextField campoEstoqueCodigo = new JTextField();
    private final JTextField campoEstoqueQtd = new JTextField();

    // Áreas em que aparecem os pedidos e o relatório de vendas.
    private final JTextArea areaPedidos = new JTextArea();
    private final JTextArea areaRelatorio = new JTextArea();
    private final ObservadorSistema observador = () ->
            javax.swing.SwingUtilities.invokeLater(this::atualizarTudo);

    public TelaFuncionario(Sistema sistema, Funcionario funcionario, TelaFarmacia telaLogin) {
        this.sistema = sistema;
        this.funcionario = funcionario;
        this.telaLogin = telaLogin;

        setTitle("Farma Aura - Menu do Funcionario");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(criarTopo(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        // Cada função do funcionário fica em uma aba.

        abas.addTab("Produtos e estoque", criarAbaProdutos());
        abas.addTab("Cadastrar produto", criarFormulario("Cadastrar produto",
                new String[]{"Codigo:", "Nome do produto:", "Preco:", "Estoque inicial:"},
                new JTextField[]{campoCadCodigo, campoCadNome, campoCadPreco, campoCadEstoque},
                "Cadastrar", this::cadastrarProduto));
        abas.addTab("Alterar preco", criarFormulario("Alterar preco",
                new String[]{"Codigo do produto:", "Novo preco:"},
                new JTextField[]{campoPrecoCodigo, campoPrecoNovo},
                "Alterar", this::alterarPreco));
        abas.addTab("Repor estoque", criarFormulario("Repor estoque",
                new String[]{"Codigo do produto:", "Quantidade a adicionar:"},
                new JTextField[]{campoEstoqueCodigo, campoEstoqueQtd},
                "Repor", this::reporEstoque));
        abas.addTab("Pedidos", criarAbaTexto(areaPedidos));
        abas.addTab("Relatorio de vendas", criarAbaTexto(areaRelatorio));

        abas.addChangeListener(e -> atualizarTudo());
        sistema.adicionarObservador(observador);
        atualizarTudo();
        // Atualiza os dados ao trocar de aba ou quando o Sistema muda.
    }

    // Parte superior com o nome da farmácia e o usuário logado.
    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titulo = new JLabel("FARMA AURA", JLabel.LEFT);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 24));
        titulo.setForeground(Color.BLUE);

        JLabel boasVindas = new JLabel("Bem-vindo, " + funcionario.getNome()
                + " (" + funcionario.getTipoUsuario() + " - matricula "
                + funcionario.getMatricula() + ")", JLabel.RIGHT);

        topo.add(titulo, BorderLayout.WEST);
        topo.add(boasVindas, BorderLayout.EAST);
        return topo;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new GridLayout(1, 1));
        rodape.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton botaoSair = new JButton("Sair da conta");
        botaoSair.setBackground(Color.WHITE);
        botaoSair.setForeground(Color.BLUE);
        botaoSair.addActionListener(e -> sairDaConta());
        rodape.add(botaoSair);
        return rodape;
    }

    private JPanel criarAbaProdutos() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTable tabela = new JTable(modeloProdutos);
        tabela.setRowHeight(24);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarAbaTexto(JTextArea area) {
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        painel.add(new JScrollPane(area), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarFormulario(String titulo, String[] rotulos, JTextField[] campos,
                                   String textoBotao, Runnable acao) {
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        JLabel tituloForm = new JLabel(titulo, JLabel.CENTER);
        tituloForm.setFont(new Font("Times New Roman", Font.BOLD, 20));
        tituloForm.setForeground(Color.BLUE);
        form.add(tituloForm);

        for (int i = 0; i < rotulos.length; i++) {
            form.add(new JLabel(rotulos[i]));
            form.add(campos[i]);
        }

        JButton botao = botaoAzul(textoBotao);
        botao.addActionListener(e -> acao.run());
        form.add(botao);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private JButton botaoAzul(String texto) {
        JButton botao = new JButton(texto);
        botao.setBackground(Color.BLUE);
        botao.setForeground(Color.WHITE);
        return botao;
    }

    // Os botões chamam o Sistema, que faz as validações.
    private void cadastrarProduto() {
        try {
            int codigo = Integer.parseInt(campoCadCodigo.getText().trim());
            String nome = campoCadNome.getText().trim();
            double preco = lerDecimal(campoCadPreco.getText());
            int quantidade = Integer.parseInt(campoCadEstoque.getText().trim());

            if (sistema.cadastrarProduto(funcionario, codigo, nome, preco, quantidade)) {
                mensagem("Produto cadastrado.");
                campoCadCodigo.setText("");
                campoCadNome.setText("");
                campoCadPreco.setText("");
                campoCadEstoque.setText("");
                atualizarTudo();
            } else {
                mensagem("Nao foi possivel cadastrar o produto.\n"
                        + "Verifique se o codigo ja existe ou se os dados sao validos.");
            }
        } catch (NumberFormatException e) {
            mensagem("Preencha codigo, preco e estoque com numeros validos.");
        }
    }

    private void alterarPreco() {
        try {
            int codigo = Integer.parseInt(campoPrecoCodigo.getText().trim());
            double novoPreco = lerDecimal(campoPrecoNovo.getText());

            if (sistema.alterarPreco(funcionario, codigo, novoPreco)) {
                mensagem("Preco alterado.");
                campoPrecoCodigo.setText("");
                campoPrecoNovo.setText("");
                atualizarTudo();
            } else {
                mensagem("Nao foi possivel alterar o preco.\n"
                        + "Verifique o codigo do produto e o valor informado.");
            }
        } catch (NumberFormatException e) {
            mensagem("Preencha codigo e preco com numeros validos.");
        }
    }

    private void reporEstoque() {
        try {
            int codigo = Integer.parseInt(campoEstoqueCodigo.getText().trim());
            int quantidade = Integer.parseInt(campoEstoqueQtd.getText().trim());

            if (sistema.reporEstoque(funcionario, codigo, quantidade)) {
                mensagem("Estoque atualizado.");
                campoEstoqueCodigo.setText("");
                campoEstoqueQtd.setText("");
                atualizarTudo();
            } else {
                mensagem("Nao foi possivel repor o estoque.\n"
                        + "Verifique o codigo do produto e a quantidade (maior que zero).");
            }
        } catch (NumberFormatException e) {
            mensagem("Preencha codigo e quantidade com numeros inteiros validos.");
        }
    }

    private void sairDaConta() {
        sistema.removerObservador(observador);
        dispose();
        telaLogin.voltarAoLogin();
    }

    // Recarrega a tabela e os relatórios depois das alterações.
    private void atualizarTudo() {
        atualizarProdutos();
        atualizarPedidos();
        areaRelatorio.setText(sistema.gerarRelatorioVendas());
        areaRelatorio.setCaretPosition(0);
    }

    private void atualizarProdutos() {
        modeloProdutos.setRowCount(0);
        for (Produto produto : sistema.listarProdutos()) {
            modeloProdutos.addRow(new Object[]{
                    produto.getCodigo(),
                    produto.getNome(),
                    "R$ " + dinheiro(produto.getPreco()),
                    produto.getQuantidade()
            });
        }
    }

    private void atualizarPedidos() {
        ArrayList<Pedido> pedidos = sistema.listarPedidos(funcionario);
        StringBuilder texto = new StringBuilder();

        if (pedidos.isEmpty()) {
            texto.append("Nenhum pedido encontrado.");
        }
        for (Pedido pedido : pedidos) {
            texto.append("Pedido numero ").append(pedido.getNumero())
                 .append(" | Cliente: ").append(pedido.getCliente().getNome())
                 .append(" | Situacao: ").append(pedido.getSituacao())
                 .append(" | Pagamento: ").append(pedido.getFormaPagamento())
                 .append("\n");
            for (ItemPedido item : pedido.getItens()) {
                texto.append("  ").append(item.getProduto().getNome())
                     .append(" - ").append(item.getQuantidade())
                     .append(" x R$ ").append(dinheiro(item.getPrecoUnitario()))
                     .append(" = R$ ").append(dinheiro(item.calcularSubtotal()))
                     .append("\n");
            }
            texto.append("  TOTAL: R$ ").append(dinheiro(pedido.calcularTotal()))
                 .append("\n\n");
        }

        areaPedidos.setText(texto.toString());
        areaPedidos.setCaretPosition(0);
    }

    // Aceita valores digitados com vírgula ou ponto.
    private double lerDecimal(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    private String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private void mensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }
}