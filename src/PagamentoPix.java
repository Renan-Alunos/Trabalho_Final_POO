public class PagamentoPix implements EstrategiaPagamento {
    @Override
    public String getForma() {
        return "PIX";
    }

    @Override
    public boolean processarPagamento(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}