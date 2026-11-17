import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JTextField;//campo de texto
import javax.swing.BorderFactory;//margens do paieel
import javax.swing.JPasswordField;//senha
import javax.swing.JButton;//cria botao
import javax.swing.JOptionPane;//exibe janelas de confirmaçao

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

    private void mostrarTelaLogin() {
        JPanel painel = new JPanel();
        //campo cpf
        painel.setLayout(new GridLayout(8, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        //titulos
        JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);//nome da farmacia+local de onde ele aparece
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));//fonte e tamanho
        titulo.setForeground(Color.BLUE);//cor da fonte

        painel.add(titulo);//titulo vai pra dentro de um painel ao inves de estar na janela

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

        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void fazerLogin() {//func. login
        String cpf = campoCpf.getText();
        String senha = new String(campoSenha.getPassword());

        Usuario usuario = sistema.fazerLogin(cpf, senha);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this, "CPF ou senha incorretos!");
        } else {
            JOptionPane.showMessageDialog(this, "Bem-vindo, " + usuario.getNome() + "!");
        }
    }

    private void mostrarTelaCadastro() {
        JPanel painel = new JPanel();
        
        painel.setLayout(new GridLayout(8, 1, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
        //titulos
        JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titulo.setForeground(Color.BLUE);

        JTextField campoNome = new JTextField();
        JTextField campoCpf = new JTextField();
        JTextField campoSenha = new JTextField();

        painel.add(titulo);
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
        painel.add(botaoCadastrar);
        //botao voltar a tela anterior
        JButton botaoVoltar = new JButton("Voltar");
        botaoVoltar.setBackground(Color.WHITE);
        botaoVoltar.setForeground(Color.BLUE);
        botaoVoltar.addActionListener(e -> mostrarTelaLogin());
        painel.add(botaoVoltar);

        setContentPane(painel);
        revalidate();
        repaint();
    }
.
    public static void main(String[] args) {
        TelaFarmacia tela = new TelaFarmacia();
        tela.setVisible(true);
    }
}