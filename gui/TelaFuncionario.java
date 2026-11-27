import javax.swing.BorderFactory;
import javax.swing.JButton;//Cria botão;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;//Campo de texto;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;//Margens do painel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Locale;

public class TelaFuncionario extends JFrame {

    private final Sistema sistema;
    private final Funcionario funcionario;
    private final TelaFarmacia telaLogin;

    private final JTabbedPane abas = new JTabbedPane();

    //Observer: o Sistema avisa a tela quando algo muda (produto, preco, estoque, venda);
    private final ObservadorSistema observador = (evento, dados) -> reagir(evento);

    //Primeira Aba - Mostra produtos e o estoque;
    private final DefaultTableModel modeloProdutos = new DefaultTableModel(
            new String[]{"Codigo", "Produto", "Preco", "Estoque"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };

    //Segunda Aba - Cadastra o produto;
    private final JTextField campoCadCodigo = new JTextField();
    private final JTextField campoCadNome = new JTextField();
    private final JTextField campoCadPreco = new JTextField();
    private final JTextField campoCadEstoque = new JTextField();

    //Terceira Aba - Serve para modificar os valores dos produtos;
    private final JTextField campoPrecoCodigo = new JTextField();
    private final JTextField campoPrecoNovo = new JTextField();

    //Quarta Aba - Repoe o estoque dos produtos;
    private final JTextField campoEstoqueCodigo = new JTextField();
    private final JTextField campoEstoqueQtd = new JTextField();

    //Quinta Aba e a Setxa, mostram os pedidos e as vendas;
    private final JTextArea areaPedidos = new JTextArea();
    private final JTextArea areaRelatorio = new JTextArea();

    public TelaFuncionario(Sistema sistema, Funcionario funcionario, TelaFarmacia telaLogin) {
        this.sistema = sistema;
        this.funcionario = funcionario;
        this.telaLogin = telaLogin;

        setTitle("Farma Aura - Menu do Funcionario");
        setSize(700, 550);
        //Fechar no X faz o mesmo que "Sair da conta": volta para o login;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                mostrarAlertasEstoque();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                telaLogin.voltarAoLogin();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                //Deixa de ser avisada (vale tanto para o X quanto para "Sair da conta");
                sistema.removerObservador(observador);
            }
        });
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(criarTopo(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        //Layout do aplicativo e das Abas;

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
        atualizarTudo();
        //Faz uma atualização a cada mudança de Aba;
        sistema.adicionarObservador(observador);
    }

    //TELA;
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

    //Funções do Sistema;
    //Valida na tela antes de chamar o Sistema, para dar mensagens especificas;
    private void cadastrarProduto() {
        try {
            int codigo = lerInteiro(campoCadCodigo, "Codigo");
            String nome = campoCadNome.getText().trim();
            double preco = lerDecimal(campoCadPreco, "Preco");
            int quantidade = lerInteiro(campoCadEstoque, "Estoque inicial");

            if (codigo <= 0) {
                mensagem("O codigo deve ser maior que zero.");
            } else if (nome.isEmpty()) {
                mensagem("Informe o nome do produto.");
            } else if (preco <= 0) {
                mensagem("O preco deve ser maior que zero.");
            } else if (quantidade < 0) {
                mensagem("O estoque inicial nao pode ser negativo.");
            } else if (buscarProduto(codigo) != null) {
                mensagem("Ja existe um produto com o codigo " + codigo + ".");
            } else if (sistema.cadastrarProduto(funcionario, codigo, nome, preco, quantidade)) {
                mensagem("Produto cadastrado.");
                campoCadCodigo.setText("");
                campoCadNome.setText("");
                campoCadPreco.setText("");
                campoCadEstoque.setText("");
            } else {
                mensagem("Nao foi possivel cadastrar o produto.");
            }
        } catch (IllegalArgumentException e) {
            mensagem(e.getMessage());
        }
    }

    private void alterarPreco() {
        try {
            int codigo = lerInteiro(campoPrecoCodigo, "Codigo do produto");
            double novoPreco = lerDecimal(campoPrecoNovo, "Novo preco");

            if (buscarProduto(codigo) == null) {
                mensagem("Nao existe produto com o codigo " + codigo + ".");
            } else if (novoPreco <= 0) {
                mensagem("O preco deve ser maior que zero.");
            } else if (sistema.alterarPreco(funcionario, codigo, novoPreco)) {
                mensagem("Preco alterado.");
                campoPrecoCodigo.setText("");
                campoPrecoNovo.setText("");
            } else {
                mensagem("Nao foi possivel alterar o preco.");
            }
        } catch (IllegalArgumentException e) {
            mensagem(e.getMessage());
        }
    }

    private void reporEstoque() {
        try {
            int codigo = lerInteiro(campoEstoqueCodigo, "Codigo do produto");
            int quantidade = lerInteiro(campoEstoqueQtd, "Quantidade a adicionar");
            Produto produto = buscarProduto(codigo);

            if (produto == null) {
                mensagem("Nao existe produto com o codigo " + codigo + ".");
            } else if (quantidade <= 0) {
                mensagem("A quantidade deve ser maior que zero.");
            } else if (quantidade > Integer.MAX_VALUE - produto.getQuantidade()) {
                mensagem("Quantidade grande demais: o estoque ultrapassaria o limite permitido.");
            } else if (sistema.reporEstoque(funcionario, codigo, quantidade)) {
                mensagem("Estoque atualizado.");
                campoEstoqueCodigo.setText("");
                campoEstoqueQtd.setText("");
            } else {
                mensagem("Nao foi possivel repor o estoque.");
            }
        } catch (IllegalArgumentException e) {
            mensagem(e.getMessage());
        }
    }

    private void sairDaConta() {
        dispose();
        telaLogin.voltarAoLogin();
    }

    //Observer;
    //Chamado pelo Sistema quando algo muda. A tela se atualiza sozinha;
    private void reagir(EventoSistema evento) {
        if (evento == EventoSistema.ESTOQUE_BAIXO) {
            //nao abre janela no meio da operacao do Sistema: deixa para depois;
            SwingUtilities.invokeLater(() -> {
                atualizarTudo();
                mostrarAlertasEstoque();
            });
        } else if (SwingUtilities.isEventDispatchThread()) {
            atualizarTudo();
        } else {
            SwingUtilities.invokeLater(this::atualizarTudo);
        }
    }

    //Mostra os avisos de estoque baixo que ainda nao foram vistos;
    private void mostrarAlertasEstoque() {
        ArrayList<String> avisos = sistema.retirarAlertasEstoque();
        if (avisos.isEmpty()) {
            return;
        }
        StringBuilder texto = new StringBuilder("Atencao ao estoque (limite: "
                + Sistema.LIMITE_ESTOQUE_BAIXO + " unidades):\n\n");
        for (String aviso : avisos) {
            texto.append("- ").append(aviso).append("\n");
        }
        JOptionPane.showMessageDialog(this, texto.toString(), "Alerta de estoque", JOptionPane.WARNING_MESSAGE);
    }

    //Atulização dos Dados;
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

    //Procura o produto pelo codigo (null se nao existir);
    private Produto buscarProduto(int codigo) {
        for (Produto produto : sistema.listarProdutos()) {
            if (produto.getCodigo() == codigo) {
                return produto;
            }
        }
        return null;
    }

    private int lerInteiro(JTextField campo, String nomeCampo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nomeCampo + " deve ser um numero inteiro valido.");
        }
    }

    //Faz vírgula ou ponto funcionar para os números e arredonda para centavos (9,999 -> 10,00);
    private double lerDecimal(JTextField campo, String nomeCampo) {
        try {
            String texto = campo.getText().trim().replace(',', '.');
            double valor = new BigDecimal(texto).setScale(2, RoundingMode.HALF_UP).doubleValue();
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nomeCampo
                    + " deve ser um numero valido (ex.: 12,50), sem separador de milhar.");
        }
    }

    private String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private void mensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }
}
