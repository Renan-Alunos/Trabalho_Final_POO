// Observador que salva o catalogo no arquivo sempre que algo muda.
// Assim o Sistema nao precisa lembrar de chamar salvar() em cada metodo.
public class PersistenciaCatalogo implements ObservadorSistema {

    private final Catalogo catalogo;

    public PersistenciaCatalogo(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public void atualizar(EventoSistema evento, Object dados) {
        switch (evento) {
            case PRODUTO_CADASTRADO:
            case PRECO_ALTERADO:
            case ESTOQUE_REPOSTO:
            case PEDIDO_FINALIZADO:
                catalogo.salvar();
                break;
            default:
                break;
        }
    }
}
