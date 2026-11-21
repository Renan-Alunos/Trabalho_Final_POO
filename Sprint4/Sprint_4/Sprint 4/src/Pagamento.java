import java.util.Locale;

public class Pagamento {
    private final ProcessadorPagamento processador;

    public Pagamento(String forma) {
        if (forma == null) {
            throw new IllegalArgumentException("Escolha uma forma de pagamento.");
        }

        CriadorPagamento criador;
        String escolhida = forma.trim().toUpperCase(Locale.ROOT);
        switch (escolhida) {
            case "PIX":
                criador = new CriadorPix();
                break;
            case "CARTAO":
                criador = new CriadorCartao();
                break;
            default:
                throw new IllegalArgumentException("Forma invalida. Escolha PIX ou CARTAO.");
        }
        this.processador = criador.criar();
    }

    public String getForma() {
        return processador.getForma();
    }

    // Aqui só simulamos o pagamento; não existe cobrança de verdade.
    public boolean processarPagamento(double valor) {
        return processador.processar(valor);
    }
}
