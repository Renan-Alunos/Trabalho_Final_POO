import java.util.ArrayList;
import java.util.Scanner;
/*
  CADASTRAR CLIENTES, CADASTRAR PRODUTOS,
  REALIZAR PEDIDOS, GERAR RELATÓRIOS DE VENDAS, VER ESTOQUE
*/
public class Sistema {

    //armazena clientes e funcionários
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    //catálogo
    private Catalogo catalogo = new Catalogo();

    public Sistema() {
        //funcionário administrador
        Funcionario administrador = new Funcionario(
            "Administrador",
            "00000000000",
            "admin123",
            "F01"
        );
        usuarios.add(administrador);
    }
    
    //login usuário
    public Usuario fazerLogin(String cpf, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf) && usuario.getSenha().equals(senha)) {
                return usuario;
            }
        }
        return null; //retorna null se não encontrar o usuário
    }

    public void cadastrarCliente(String nome, String cpf, String senha) {
        //verifica se o CPF já está cadastrado
        for (Usuario usuario : usuarios) {
            if (usuario.getCpf().equals(cpf)) {
                System.out.println("CPF já cadastrado!");
                return;
            }
        }
        //cria um novo cliente
        Cliente cliente = new Cliente(nome, cpf, senha);

        //add o cliente na lista de usuários
        usuarios.add(cliente);

        System.out.println("Cliente cadastrado com sucesso!");
    }
}