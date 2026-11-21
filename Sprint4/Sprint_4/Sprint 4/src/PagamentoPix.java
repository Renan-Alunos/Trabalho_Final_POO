public class PagamentoPix implements ProcessadorPagamento {
    @Override
    public String getForma() {
        return "PIX";
    }

    @Override
    public boolean processar(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}
