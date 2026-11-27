public interface EstrategiaPagamento {
    String getForma();

    boolean processarPagamento(double valor);
}