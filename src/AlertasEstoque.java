import java.util.ArrayList;
import java.util.LinkedHashMap;

// Observador que guarda os avisos de estoque baixo ate o funcionario ver.
// Como o funcionario normalmente nao esta logado quando a venda acontece,
// os avisos ficam pendentes e sao entregues quando ele pedir (retirarPendentes).
public class AlertasEstoque implements ObservadorSistema {

    private final int limite;
    // um aviso por produto (o mais recente); a chave e o codigo do produto
    private final LinkedHashMap<Integer, String> pendentes = new LinkedHashMap<>();

    public AlertasEstoque(int limite) {
        this.limite = limite;
    }

    @Override
    public void atualizar(EventoSistema evento, Object dados) {
        if (!(dados instanceof Produto)) {
            return;
        }
        Produto produto = (Produto) dados;

        if (evento == EventoSistema.ESTOQUE_BAIXO) {
            pendentes.put(produto.getCodigo(), montarMensagem(produto));
        } else if (evento == EventoSistema.ESTOQUE_REPOSTO && produto.getQuantidade() > limite) {
            // reposto acima do limite: o aviso nao faz mais sentido
            pendentes.remove(produto.getCodigo());
        }
    }

    // Devolve os avisos pendentes e limpa a lista.
    public ArrayList<String> retirarPendentes() {
        ArrayList<String> avisos = new ArrayList<>(pendentes.values());
        pendentes.clear();
        return avisos;
    }

    private String montarMensagem(Produto produto) {
        int qtd = produto.getQuantidade();
        String situacao;
        if (qtd <= 0) {
            situacao = "ESGOTADO";
        } else {
            situacao = "restam " + qtd + (qtd == 1 ? " unidade" : " unidades");
        }
        return "Estoque baixo: " + produto.getNome() + " (codigo " + produto.getCodigo() + ") - " + situacao + ".";
    }
}
