import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class TelaFarmacia extends JFrame {

    public TelaFarmacia() {
        setTitle("Trabalho farmacia");//nome superior
        setSize(600, 500);//tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//encerra orp rograma
        setLocationRelativeTo(null);//centraliza janela
        JPanel painel = new JPanel();
        painel.setLayout(new GridLayout(0, 1, 10, 10));
        JLabel titulo = new JLabel("FARMA AURA", JLabel.CENTER);//nome da farmacia+local de onde ele aparece
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 30));//fonte e tamanho
        titulo.setForeground(Color.BLUE);//cor da fonte

        painel.add(titulo);//titulo vai pra dentro de um painel ao inves de estar na janela 
        add(painel);
    }

    public static void main(String[] args) {
        TelaFarmacia tela = new TelaFarmacia();
        tela.setVisible(true);
    }
}