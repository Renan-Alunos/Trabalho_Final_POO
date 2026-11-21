import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;

public class TelaFarmacia extends JFrame {
    private JTextField campoCpf = new JTextField();
    private JPasswordField campoSenha = new JPasswordField();
    private Sistema sistema = new Sistema();

    public TelaFarmacia() {
        setTitle("Trabalho farmacia");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        mostrarTelaLogin();
    }

    private void mostrarTelaLogin() {
        JPanel painel = new JPanel();
        // O CPF e a senha ficam na primeira tela.
        painel.setLayout(new GridLayout(8, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        // Título da farmácia.
        JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titulo.setForeground(Color.BLUE);

        painel.add(titulo);

        painel.add(new JLabel("CPF:"));
        painel.add(campoCpf);
        
        painel.add(new JLabel("Senha:"));
        painel.add(campoSenha);

        // Entra na conta com os dados informados.
        JButton botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBackground(Color.BLUE);
        botaoEntrar.setForeground(Color.WHITE);
        botaoEntrar.addActionListener(e -> fazerLogin());
        painel.add(botaoEntrar);
        // Abre o cadastro de cliente.
        JButton botaoCadastro = new JButton("Cadastrar");
        botaoCadastro.setBackground(Color.WHITE);
        botaoCadastro.setForeground(Color.BLUE);
        botaoCadastro.addActionListener(e -> mostrarTelaCadastro());
        painel.add(botaoCadastro);
        // Dá para consultar o catálogo mesmo sem fazer login.
        JButton botaoCatalogo = new JButton("Ver catálogo");
        botaoCatalogo.setBackground(Color.WHITE);
        botaoCatalogo.setForeground(Color.BLUE);
        botaoCatalogo.addActionListener(e -> mostrarCatalogo());
        painel.add(botaoCatalogo);

        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void fazerLogin() { // Login de cliente ou funcionário.
        String cpf = campoCpf.getText();
        String senha = new String(campoSenha.getPassword());

        Usuario usuario = sistema.fazerLogin(cpf, senha);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "CPF ou senha incorretos!");
        } else if (usuario instanceof Cliente) {
            mostrarTelaCliente((Cliente) usuario);
        } else if (usuario instanceof Funcionario) {
            mostrarTelaFuncionario((Funcionario) usuario);
        }
    }

    private void mostrarTelaCadastro() {
        JPanel painel = new JPanel();
        
        painel.setLayout(new GridLayout(8, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        // Título da farmácia.
        JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titulo.setForeground(Color.BLUE);

        JTextField campoNome = new JTextField();
        JTextField campoCpf = new JTextField();
        JPasswordField campoSenha = new JPasswordField();

        painel.add(titulo);
        painel.add(new JLabel("Cria a sua conta:", JLabel.CENTER));

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);

        painel.add(new JLabel("CPF:"));
        painel.add(campoCpf);
        
        painel.add(new JLabel("Senha:"));
        painel.add(campoSenha);

        // Salva os dados e volta para o login.
        JButton botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.setBackground(Color.BLUE);
        botaoCadastrar.setForeground(Color.WHITE);
        botaoCadastrar.addActionListener(e -> cadastrarCliente(
        campoNome.getText(),
        campoCpf.getText(),
        new String(campoSenha.getPassword())));

        painel.add(botaoCadastrar);
        // Volta sem fazer o cadastro.
        JButton botaoVoltar = new JButton("Voltar");
        botaoVoltar.setBackground(Color.WHITE);
        botaoVoltar.setForeground(Color.BLUE);
        botaoVoltar.addActionListener(e -> mostrarTelaLogin());
        painel.add(botaoVoltar);

        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void cadastrarCliente(String nome, String cpf, String senha){
        try {
            sistema.cadastrarCliente(nome, cpf, senha);
            JOptionPane.showMessageDialog(this, "Cadastro realizado!");
            mostrarTelaLogin();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void mostrarCatalogo() {
        JPanel painel = new JPanel();
        painel.setLayout(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JLabel titulo = new JLabel("Catálogo de produtos", JLabel.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titulo.setForeground(Color.BLUE);

        painel.add(titulo, BorderLayout.NORTH);

        JPanel listaProdutos = new JPanel();
        listaProdutos.setLayout(new GridLayout(0, 1, 10, 10));

        for (Produto produto : sistema.listarProdutos()) {
            JLabel item = new JLabel(produto.getNome() + " - R$ " + String.format("%.2f", produto.getPreco()));
            listaProdutos.add(item);
        }
        painel.add(new JScrollPane(listaProdutos), BorderLayout.CENTER);

        JButton botaoVoltar = new JButton("Voltar");
        botaoVoltar.setBackground(Color.BLUE);
        botaoVoltar.setForeground(Color.WHITE);
        botaoVoltar.addActionListener(e -> mostrarTelaLogin());

        painel.add(botaoVoltar, BorderLayout.SOUTH);

        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void mostrarTelaCliente(Cliente cliente) {
        // Uso o mesmo Sistema para manter os dados da conta e das compras.
        setContentPane(new TelaCliente(sistema, cliente, this::voltarAoLogin));
        revalidate();
        repaint();
    }
    
    private void mostrarTelaFuncionario(Funcionario funcionario) {
        setVisible(false);
        new TelaFuncionario(sistema, funcionario, this).setVisible(true);
    }

    // Limpa os campos para o próximo login.
    public void voltarAoLogin() {
        campoCpf.setText("");
        campoSenha.setText("");
        mostrarTelaLogin();
        setVisible(true);
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new TelaFarmacia().setVisible(true));
    }
}