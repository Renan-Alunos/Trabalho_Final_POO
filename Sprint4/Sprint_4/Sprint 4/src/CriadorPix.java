public class CriadorPix extends CriadorPagamento {
    @Override
    public ProcessadorPagamento criar() {
        return new PagamentoPix();
    }
}
