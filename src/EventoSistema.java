// Tipos de acontecimento que o Sistema avisa aos observadores.
public enum EventoSistema {
    PRODUTO_CADASTRADO,   // dados = Produto
    PRECO_ALTERADO,       // dados = Produto
    ESTOQUE_REPOSTO,      // dados = Produto
    PEDIDO_FINALIZADO,    // dados = Pedido
    ESTOQUE_BAIXO         // dados = Produto (estoque igual ou abaixo do limite)
}