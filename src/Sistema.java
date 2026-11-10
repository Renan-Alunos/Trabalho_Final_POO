import java.util.ArrayList;
/*
  CADASTRAR CLIENTES, CADASTRAR PRODUTOS,
  REALIZAR PEDIDOS, GERAR RELATÓRIOS DE VENDAS, VER ESTOQUE
*/
public class Sistema{
    //armazena clientes e funcionários
    private ArrayList<Usuario> usuarios = new ArrayList<>();

    //catálogo
    private Catalogo catalogo = new Catalogo();

    public Sistema(){
        //funcionário administrador
        Funcionario administrador = new Funcionario("Administrador","00000000000","admin123","F01");
        usuarios.add(administrador);

        //produtos iniciais
        catalogo.adicionarProduto(new Produto(1, "Dipirona 500mg", 12.50, 5));//(codigo, nome, preço, quantidade)
        catalogo.adicionarProduto(new Produto(2, "Paracetamol 750mg", 9.90, 10));
        catalogo.adicionarProduto(new Produto(3, "Ibuprofeno 400mg", 15.90, 6));
        catalogo.adicionarProduto(new Produto(4, "Vitamina C", 19.90, 5));
        catalogo.adicionarProduto(new Produto(5, "Soro Fisiológico", 7.50, 10));
    }
    
    //login usuário
    public Usuario fazerLogin(String cpf, String senha){
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf) && usuario.getSenha().equals(senha)){
                return usuario;
            }
        }
        return null; //retorna null se não encontrar o usuário
    }

    public void cadastrarCliente(String nome, String cpf, String senha){
        //verifica se o CPF já está cadastrado
        for (Usuario usuario : usuarios){
            if (usuario.getCpf().equals(cpf)){
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

    public String buscarClientes(String cpf){ //buscara usuário a partir de seu cpf
        for(Usuario usuario : usuarios){
            if (usuario instanceof Cliente && usuario.getCpf().equals(cpf)){
                return usuario.getNome();
            }
        }
        return null;
    }

    public ArrayList<Produto> listarProdutos(){
        return catalogo.getProdutos();
    }

    public boolean cadastrarProduto(Funcionario funcionario, int codigo, String nome, double preco, int quantidade){
        //verifica se o funcionário pertence ao sistema
        if (funcionario == null || !usuarios.contains(funcionario)){
            return false;
        }

        //verifica se os dados são válidos
        if (codigo <= 0 || nome == null || nome.isBlank() || !Double.isFinite(preco) || preco <= 0 || quantidade < 0){
            return false;
        }

        //verifica se o código já existe
        if (catalogo.buscarProduto(codigo) != null){
            return false;
        }

        //cria um produto
        Produto produto = new Produto(codigo, nome, preco, quantidade);

        //só o funcionário adiciona o produto ao catálogo
        funcionario.adicionarProduto(catalogo, produto);

        return true;
    }

    public boolean alterarPreco(Funcionario funcionario, int codigo, double novoPreco) {
        //verifica se o funcionário está cadastrado
        if (funcionario == null || !usuarios.contains(funcionario)) {
            return false;
        }

        //verifica se o preço é válido
        if (!Double.isFinite(novoPreco) || novoPreco < 0) {
            return false;
        }

        //busca produto por código
        Produto produto = catalogo.buscarProduto(codigo);

        //verifica se o produto existe
        if (produto == null) {
            return false;
        }

        //altera preço
        funcionario.alterarPreco(produto, novoPreco);

        return true;
    }

}