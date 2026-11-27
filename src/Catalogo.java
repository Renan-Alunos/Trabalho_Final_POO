import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Catalogo {

    private static final int IDENTIFICADOR = 0x4641524D;
    private static final int VERSAO = 1;
    private static final int LIMITE_PRODUTOS = 100000;

    private final ArrayList<Produto> produtos;
    private final Path arquivo = Paths.get("dados", "catalogo.dat");

    public Catalogo() {
        produtos = new ArrayList<>();
    }

    public void adicionarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto nao pode ser nulo.");
        }
        if (buscarProduto(produto.getCodigo()) != null) {
            throw new IllegalArgumentException("Ja existe um produto com esse codigo.");
        }
        produtos.add(produto);
    }

    public boolean removerProduto(int codigo) {
        return produtos.removeIf(produto -> produto.getCodigo() == codigo);
    }

    public Produto buscarProduto(int codigo) {
        for (Produto p : produtos) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Produto> getProdutos() {
        return new ArrayList<>(produtos);
    }

    // Retorna false quando ainda nao existe um arquivo salvo ou quando ele esta danificado.
    public boolean carregar() {
        if (!Files.exists(arquivo)) {
            return false;
        }

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(arquivo)))) {

            if (entrada.readInt() != IDENTIFICADOR || entrada.readInt() != VERSAO) {
                throw new IOException("Formato do catalogo desconhecido.");
            }

            int quantidade = entrada.readInt();
            if (quantidade < 0 || quantidade > LIMITE_PRODUTOS) {
                throw new IOException("Quantidade de produtos invalida.");
            }

            ArrayList<Produto> carregados = new ArrayList<>();
            Set<Integer> codigos = new HashSet<>();

            for (int i = 0; i < quantidade; i++) {
                int codigo = entrada.readInt();
                String nome = entrada.readUTF();
                double preco = entrada.readDouble();
                int estoque = entrada.readInt();

                if (!codigos.add(codigo)) {
                    throw new IOException("Codigo de produto repetido no arquivo.");
                }
                carregados.add(new Produto(codigo, nome, preco, estoque));
            }

            if (entrada.read() != -1) {
                throw new IOException("Existem dados extras no arquivo do catalogo.");
            }

            produtos.clear();
            produtos.addAll(carregados);
            return true;
        } catch (IOException | IllegalArgumentException erro) {
            guardarBackup();
            System.err.println("Nao foi possivel ler o catalogo salvo. Os produtos iniciais serao carregados.");
            return false;
        }
    }

    // Grava primeiro em um temporario para evitar deixar o arquivo incompleto.
    public boolean salvar() {
        Path temporario = arquivo.resolveSibling("catalogo.tmp");

        try {
            Files.createDirectories(arquivo.getParent());

            try (DataOutputStream saida = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporario)))) {
                saida.writeInt(IDENTIFICADOR);
                saida.writeInt(VERSAO);
                saida.writeInt(produtos.size());

                for (Produto produto : produtos) {
                    saida.writeInt(produto.getCodigo());
                    saida.writeUTF(produto.getNome());
                    saida.writeDouble(produto.getPreco());
                    saida.writeInt(produto.getQuantidade());
                }
            }

            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException erro) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException erro) {
            try {
                Files.deleteIfExists(temporario);
            } catch (IOException ignorado) {
                // Mantem o erro original como referencia.
            }
            System.err.println("Erro ao salvar o catalogo: " + erro.getMessage());
            return false;
        }
    }

    private void guardarBackup() {
        Path backup = arquivo.resolveSibling("catalogo.dat.bak");
        try {
            Files.copy(arquivo, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException erro) {
            System.err.println("Nao foi possivel criar o backup do catalogo: " + erro.getMessage());
        }
    }
}
