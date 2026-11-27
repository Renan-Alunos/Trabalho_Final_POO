import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JTextField;//campo de texto
import javax.swing.BorderFactory;//margens do painel
import javax.swing.JPasswordField;//senha
import javax.swing.JButton;//cria botao
import javax.swing.JOptionPane;//exibe janelas de confirmaçao
import java.awt.BorderLayout;
import javax.swing.JScrollPane;

public class TelaFarmacia extends JFrame {
    private JTextField campoCpf = new JTextField();
    private JPasswordField campoSenha = new JPasswordField();
    private Sistema sistema = new Sistema();

    public TelaFarmacia() {
        setTitle("Trabalho farmacia");//nome superior
        setSize(600, 500);//tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//encerra op rograma
        setLocationRelativeTo(null);
        mostrarTelaLogin();
    }
    
    private JLabel criarTitulo(String texto) {
        JLabel titulo = new JLabel(texto, JLabel.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titulo.setForeground(Color.BLUE);
        return titulo;
    }

    private void atualizarPainel(JPanel painel) {
        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void mostrarTelaLogin() {
        JPanel painel = new JPanel();
        //campo cpf
        painel.setLayout(new GridLayout(8, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        //titulos
        /*
            JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);//nome da farmacia+local de onde ele aparece
            titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));//fonte e tamanho
            titulo.setForeground(Color.BLUE);//cor da fonte
        */

        painel.add(criarTitulo("FARMA AURA"));//titulo é adicionado com a função criada

        painel.add(new JLabel("CPF:"));//campo cpf
        painel.add(campoCpf);
        
        painel.add(new JLabel("Senha:"));//campo senha
        painel.add(campoSenha);

        //botao entrar
        JButton botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBackground(Color.BLUE);
        botaoEntrar.setForeground(Color.WHITE);
        botaoEntrar.addActionListener(e -> fazerLogin());
        painel.add(botaoEntrar);
        //botao cadastro
        JButton botaoCadastro = new JButton("Cadastrar");
        botaoCadastro.setBackground(Color.WHITE);
        botaoCadastro.setForeground(Color.BLUE);
        botaoCadastro.addActionListener(e -> mostrarTelaCadastro());
        painel.add(botaoCadastro);
        //botao catalogo
        JButton botaoCatalogo = new JButton("Ver catálogo");
        botaoCatalogo.setBackground(Color.WHITE);
        botaoCatalogo.setForeground(Color.BLUE);
        botaoCatalogo.addActionListener(e -> mostrarCatalogo());
        painel.add(botaoCatalogo);

        atualizarPainel(painel);
    }

    private void fazerLogin() {//func. login
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
        //titulos
        /*
            JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);
            titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
            titulo.setForeground(Color.BLUE);
        */

        JTextField campoNome = new JTextField();
        JTextField campoCpf = new JTextField();
        JPasswordField campoSenha = new JPasswordField();

        painel.add(criarTitulo("FARMA AURA"));
        painel.add(new JLabel("Cria a sua conta:", JLabel.CENTER));

        painel.add(new JLabel("Nome:"));//campo cpf
        painel.add(campoNome);

        painel.add(new JLabel("CPF:"));//campo cpf
        painel.add(campoCpf);
        
        painel.add(new JLabel("Senha:"));//campo senha
        painel.add(campoSenha);

        //botao cadastrar
        JButton botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.setBackground(Color.BLUE);
        botaoCadastrar.setForeground(Color.WHITE);
        botaoCadastrar.addActionListener(e -> cadastrarCliente(
        campoNome.getText(),
        campoCpf.getText(),
        new String(campoSenha.getPassword())));

        painel.add(botaoCadastrar);
        //botao voltar a tela anterior
        JButton botaoVoltar = new JButton("Voltar");
        botaoVoltar.setBackground(Color.WHITE);
        botaoVoltar.setForeground(Color.BLUE);
        botaoVoltar.addActionListener(e -> mostrarTelaLogin());
        painel.add(botaoVoltar);

        atualizarPainel(painel);
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
        /*
            JLabel titulo = new JLabel("Catálogo de produtos", JLabel.CENTER);
            titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
            titulo.setForeground(Color.BLUE);
        */
        painel.add(criarTitulo("Catálogo de produtos"), BorderLayout.NORTH);

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

        atualizarPainel(painel);
    }

    private void mostrarTelaCliente(Cliente cliente) {
        setSize(1000, 650);
        setLocationRelativeTo(null);

        atualizarPainel(new TelaCliente(sistema, cliente, this::voltarAoLogin));
    }
    
    private void mostrarTelaFuncionario(Funcionario funcionario) {
        setVisible(false);
        new TelaFuncionario(sistema, funcionario, this).setVisible(true);
    }

    //retorna para a tela de login
    public void voltarAoLogin() {
        campoCpf.setText("");
        campoSenha.setText("");

        setSize(600, 500);
        setLocationRelativeTo(null);

        mostrarTelaLogin();
        setVisible(true);
    }
    public static void main(String[] args) {
        TelaFarmacia tela = new TelaFarmacia();
        tela.setVisible(true);
    }
}
