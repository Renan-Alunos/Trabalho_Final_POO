public class PagamentoCartao implements ProcessadorPagamento {
    @Override
    public String getForma() {
        return "CARTAO";
    }

    @Override
    public boolean processar(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}
