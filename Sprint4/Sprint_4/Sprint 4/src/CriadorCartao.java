public class CriadorCartao extends CriadorPagamento {
    @Override
    public ProcessadorPagamento criar() {
        return new PagamentoCartao();
    }
}
