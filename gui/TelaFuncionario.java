import javax.swing.BorderFactory;
import javax.swing.JButton;//Cria botão;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;//Campo de texto;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;//Divide o painel em norte, centro e sul;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Locale;

// Menu do funcionário. Igual à TelaCliente, é um painel que o TelaFarmacia coloca na janela.
// Esta tela não tem regra de negócio: ela só chama os métodos do Sistema.
public class TelaFuncionario extends JPanel {

    private final Sistema sistema;
    private final Funcionario funcionario;
    private final Runnable onSair;//o que fazer ao sair da conta (voltar ao login)

    private final JTabbedPane abas = new JTabbedPane();

    //Primeira Aba - Mostra produtos e o estoque;
    private final DefaultTableModel modeloProdutos = new DefaultTableModel(
            new String[]{"Código", "Produto", "Preço", "Estoque"}, 0) {
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

    //Quarta Aba - Repõe o estoque dos produtos;
    private final JTextField campoEstoqueCodigo = new JTextField();
    private final JTextField campoEstoqueQtd = new JTextField();

    //Quinta e Sexta Aba - Mostram os pedidos e as vendas;
    private final JTextArea areaPedidos = new JTextArea();
    private final JTextArea areaRelatorio = new JTextArea();

    public TelaFuncionario(Sistema sistema, Funcionario funcionario, Runnable onSair) {
        super(new BorderLayout());
        this.sistema = sistema;
        this.funcionario = funcionario;
        this.onSair = onSair;

        add(criarTopo(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
        //Layout do aplicativo e das Abas;

        abas.addTab("Produtos e estoque", criarAbaProdutos());
        abas.addTab("Cadastrar produto", criarFormulario("Cadastrar produto",
                new String[]{"Código:", "Nome do produto:", "Preço:", "Estoque inicial:"},
                new JTextField[]{campoCadCodigo, campoCadNome, campoCadPreco, campoCadEstoque},
                "Cadastrar", this::cadastrarProduto));
        abas.addTab("Alterar preço", criarFormulario("Alterar preço",
                new String[]{"Código do produto:", "Novo preço:"},
                new JTextField[]{campoPrecoCodigo, campoPrecoNovo},
                "Alterar", this::alterarPreco));
        abas.addTab("Repor estoque", criarFormulario("Repor estoque",
                new String[]{"Código do produto:", "Quantidade a adicionar:"},
                new JTextField[]{campoEstoqueCodigo, campoEstoqueQtd},
                "Repor", this::reporEstoque));
        abas.addTab("Pedidos", criarAbaTexto(areaPedidos));
        abas.addTab("Relatório de vendas", criarAbaTexto(areaRelatorio));

        abas.addChangeListener(e -> atualizarTudo());
        atualizarTudo();
        //Faz uma atualização a cada mudança de Aba;
    }

    //TELA;
    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titulo = new JLabel("FARMA AURA", JLabel.LEFT);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 24));
        titulo.setForeground(Color.BLUE);

        JLabel boasVindas = new JLabel("Bem-vindo, " + funcionario.getNome()
                + " (" + funcionario.getTipoUsuario() + " - matrícula "
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

        //Apertar Enter no último campo faz o mesmo que clicar no botão;
        campos[campos.length - 1].addActionListener(e -> acao.run());

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
    private void cadastrarProduto() {
        try {
            int codigo = lerInteiro(campoCadCodigo);
            String nome = campoCadNome.getText().trim();
            double preco = lerDecimal(campoCadPreco);
            int quantidade = lerInteiro(campoCadEstoque);

            mostrarResultado(sistema.cadastrarProduto(funcionario, codigo, nome, preco, quantidade),
                    "Produto cadastrado.",
                    "Não foi possível cadastrar o produto.\n"
                            + "Verifique se o código já existe ou se os dados são válidos.",
                    campoCadCodigo, campoCadNome, campoCadPreco, campoCadEstoque);
        } catch (NumberFormatException e) {
            mensagem("Preencha código, preço e estoque com números válidos.");
        }
    }

    private void alterarPreco() {
        try {
            int codigo = lerInteiro(campoPrecoCodigo);
            double novoPreco = lerDecimal(campoPrecoNovo);

            mostrarResultado(sistema.alterarPreco(funcionario, codigo, novoPreco),
                    "Preço alterado.",
                    "Não foi possível alterar o preço.\n"
                            + "Verifique o código do produto e o valor informado.",
                    campoPrecoCodigo, campoPrecoNovo);
        } catch (NumberFormatException e) {
            mensagem("Preencha código e preço com números válidos.");
        }
    }

    private void reporEstoque() {
        try {
            int codigo = lerInteiro(campoEstoqueCodigo);
            int quantidade = lerInteiro(campoEstoqueQtd);

            mostrarResultado(sistema.reporEstoque(funcionario, codigo, quantidade),
                    "Estoque atualizado.",
                    "Não foi possível repor o estoque.\n"
                            + "Verifique o código do produto e a quantidade (maior que zero).",
                    campoEstoqueCodigo, campoEstoqueQtd);
        } catch (NumberFormatException e) {
            mensagem("Preencha código e quantidade com números inteiros válidos.");
        }
    }

    private void sairDaConta() {
        onSair.run();
    }

    //Atualização dos Dados;
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
            texto.append("Pedido número ").append(pedido.getNumero())
                 .append(" | Cliente: ").append(pedido.getCliente().getNome())
                 .append(" | Situação: ").append(pedido.getSituacao())
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

    //Auxiliares - evitam repetir o mesmo código em cada ação;

    //Se deu certo: limpa os campos, atualiza as tabelas e avisa. Se não: só avisa;
    private void mostrarResultado(boolean sucesso, String msgSucesso, String msgFalha,
                                  JTextField... camposParaLimpar) {
        if (sucesso) {
            limpar(camposParaLimpar);
            atualizarTudo();
            mensagem(msgSucesso);
        } else {
            mensagem(msgFalha);
        }
    }

    private int lerInteiro(JTextField campo) {
        return Integer.parseInt(campo.getText().trim());
    }

    //Faz vírgula ou ponto funcionar para os números;
    private double lerDecimal(JTextField campo) {
        return Double.parseDouble(campo.getText().trim().replace(',', '.'));
    }

    private void limpar(JTextField... campos) {
        for (JTextField campo : campos) {
            campo.setText("");
        }
    }

    private String dinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }

    private void mensagem(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }
}
