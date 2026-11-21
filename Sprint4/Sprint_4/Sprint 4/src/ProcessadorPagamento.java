public interface ProcessadorPagamento {
    String getForma();
    boolean processar(double valor);
}
