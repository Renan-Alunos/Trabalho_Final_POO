import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TesteInterface {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TelaFarmacia tela = new TelaFarmacia();
            try {
                Field campoSistema = TelaFarmacia.class.getDeclaredField("sistema");
                campoSistema.setAccessible(true);
                Sistema sistema = (Sistema) campoSistema.get(tela);
                sistema.cadastrarCliente("Teste", "99988877766", "senha");

                Field cpf = TelaFarmacia.class.getDeclaredField("campoCpf");
                Field senha = TelaFarmacia.class.getDeclaredField("campoSenha");
                cpf.setAccessible(true);
                senha.setAccessible(true);
                ((JTextField) cpf.get(tela)).setText("99988877766");
                ((JPasswordField) senha.get(tela)).setText("senha");

                Method login = TelaFarmacia.class.getDeclaredMethod("fazerLogin");
                login.setAccessible(true);
                login.invoke(tela);

                if (!(tela.getContentPane() instanceof TelaCliente)) {
                    throw new AssertionError("Login deveria abrir a tela do cliente.");
                }
                TelaCliente painel = (TelaCliente) tela.getContentPane();
                Method alterarQuantidade = TelaCliente.class.getDeclaredMethod(
                        "alterarQuantidade", Produto.class, int.class);
                alterarQuantidade.setAccessible(true);
                Produto produto = sistema.listarProdutos().get(0);
                alterarQuantidade.invoke(painel, produto, 1);
                alterarQuantidade.invoke(painel, produto, 1);
                Field campoPedido = TelaCliente.class.getDeclaredField("pedidoAtual");
                campoPedido.setAccessible(true);
                Pedido pedido = (Pedido) campoPedido.get(painel);
                if (pedido == null || pedido.quantidadeDoProduto(produto) != 2
                        || Math.abs(pedido.calcularTotal() - 25.0) > 0.0001) {
                    throw new AssertionError("Carrinho nao refletiu as compras na logica.");
                }
                alterarQuantidade.invoke(painel, produto, -1);
                if (((Pedido) campoPedido.get(painel)).quantidadeDoProduto(produto) != 1) {
                    throw new AssertionError("Carrinho nao diminuiu a quantidade.");
                }
                alterarQuantidade.invoke(painel, produto, -1);
                if (campoPedido.get(painel) != null) {
                    throw new AssertionError("Carrinho vazio deveria cancelar o pedido aberto.");
                }
                System.out.println("GUI: login e carrinho de compras funcionando.");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Falha no teste da GUI.", e);
            } finally {
                tela.dispose();
            }
        });
    }
}
