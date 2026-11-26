import java.util.Locale;

public class Pagamento {

    private String forma;

    public Pagamento(String forma) {
        if (forma == null) {
            throw new IllegalArgumentException("Escolha uma forma de pagamento.");
        }
        String escolhida = forma.trim().toUpperCase(Locale.ROOT);
        if (!escolhida.equals("PIX") && !escolhida.equals("CARTAO")) {
            throw new IllegalArgumentException("Forma invalida. Escolha PIX ou CARTAO.");
        }
        this.forma = escolhida;
    }

    public String getForma() {
        return forma;
    }

    // Nesta sprint, o pagamento e apenas uma simulacao.
    public boolean processarPagamento(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}