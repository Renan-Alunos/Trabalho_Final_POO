public class EstoqueInsuficienteException extends IllegalStateException {
    public EstoqueInsuficienteException(String nomeProduto, int disponivel) {
        super("Estoque insuficiente de " + nomeProduto + ". Disponivel: " + disponivel + ".");
    }
}
