public class PagamentoCartao implements EstrategiaPagamento {
    @Override
    public String getForma() {
        return "CARTAO";
    }

    @Override
    public boolean processarPagamento(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }
}