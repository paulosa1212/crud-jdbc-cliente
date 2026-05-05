package dao;

import config.ConexaoSql;
import entity.Cliente;
import entity.Endereco;

import java.io.PipedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDao implements ClienteDaoInterface {


    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = """
                    SELECT 
                        c.id, c.nome, c.cpf, c.data_nascimento,
                        e.id AS endereco_id, e.cidade, e.bairro, e.rua, e.cep
                    FROM clientes c
                    LEFT JOIN endereco e ON c.id = e.cliente_id
                """;
        try (Connection conn = ConexaoSql.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();

                cliente.setId(rs.getLong("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpf(rs.getString("cpf"));
                if (rs.getDate("data_nascimento") != null) {
                    cliente.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                }
                long enderecoid = rs.getLong("endereco_id");
                if (enderecoid != 0) {
                    Endereco endereco = new Endereco();

                    endereco.setId(enderecoid);
                    endereco.setCidade(rs.getString("cidade"));
                    endereco.setBairro(rs.getString("bairro"));
                    endereco.setRua(rs.getString("rua"));
                    endereco.setCep(rs.getString("cep"));
                    cliente.setEndereco(endereco);
                }
                clientes.add(cliente);
            }


        } catch (SQLException e) {
            throw new RuntimeException("erro ao listar clientes ", e);
        }


        return clientes;
    }

    @Override
    public void salvar(Cliente cliente) {
        String sqlClientes = "INSERT INTO clientes(nome,cpf,data_nascimento) VALUES(?,?,?) RETURNING id";
        String sqlEndereco = "INSERT INTO endereco(cliente_id,cidade,bairro,rua,cep) VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement pscadastro = null;
        PreparedStatement psendereco = null;
        try {
            conn = ConexaoSql.getConnection();
            conn.setAutoCommit(false);
            pscadastro = conn.prepareStatement(sqlClientes);

            pscadastro.setString(1, cliente.getNome());
            pscadastro.setString(2, cliente.getCpf());
            if (cliente.getDataNascimento() != null) {
                pscadastro.setDate(3, java.sql.Date.valueOf(cliente.getDataNascimento()));
            } else {
                pscadastro.setDate(3, null);
            }
            ResultSet rs = pscadastro.executeQuery();
            long clienteId = 0;
            while (rs.next()) {
                clienteId = rs.getLong(1);
            }

            if (cliente.getEndereco() != null) {
                psendereco = conn.prepareStatement(sqlEndereco);
                psendereco.setLong(1, clienteId);
                psendereco.setString(2, cliente.getEndereco().getCidade());
                psendereco.setString(3, cliente.getEndereco().getBairro());
                psendereco.setString(4, cliente.getEndereco().getRua());
                psendereco.setString(5, cliente.getEndereco().getCep());
                psendereco.executeUpdate();

            }
            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException("Erro ao salvar cliente", e);
        } finally {
            try {
                if (pscadastro != null) {
                    pscadastro.close();
                }
                if (psendereco != null) {
                    psendereco.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public Cliente buscarPorCpf(String cpf) {

        String sql = """
                SELECT 
                    c.id, c.nome, c.cpf, c.data_nascimento,
                    e.id AS endereco_id, e.cidade, e.bairro, e.rua, e.cep
                FROM clientes c
                LEFT JOIN endereco e ON c.id = e.cliente_id
                WHERE c.cpf = ?
                """;

        try (Connection conn = ConexaoSql.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cpf);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Cliente cliente = new Cliente();

                    cliente.setId(rs.getLong("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setCpf(rs.getString("cpf"));

                    var data = rs.getDate("data_nascimento");
                    if (data != null) {
                        cliente.setDataNascimento(data.toLocalDate());
                    }

                    long enderecoId = rs.getLong("endereco_id");
                    if (enderecoId != 0) {
                        Endereco endereco = new Endereco();

                        endereco.setId(enderecoId);
                        endereco.setCidade(rs.getString("cidade"));
                        endereco.setBairro(rs.getString("bairro"));
                        endereco.setRua(rs.getString("rua"));
                        endereco.setCep(rs.getString("cep"));

                        cliente.setEndereco(endereco);
                    }

                    return cliente;
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por CPF", e);
        }

        return null;
    }

    @Override
    public void atualizar(Cliente cliente) {

        String sqlCliente = "UPDATE clientes SET nome = ?, cpf = ?, data_nascimento = ? WHERE id = ?";
        String sqlVerificaEndereco = "SELECT id FROM endereco WHERE cliente_id = ?";
        String sqlUpdateEndereco = "UPDATE endereco SET cidade = ?, bairro = ?, rua = ?, cep = ? WHERE cliente_id = ?";
        String sqlInsertEndereco = "INSERT INTO endereco(cliente_id, cidade, bairro, rua, cep) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = ConexaoSql.getConnection();
            conn.setAutoCommit(false);

            // 1. Atualizar cliente
            try (PreparedStatement psCliente = conn.prepareStatement(sqlCliente)) {
                psCliente.setString(1, cliente.getNome());
                psCliente.setString(2, cliente.getCpf());

                if (cliente.getDataNascimento() != null) {
                    psCliente.setDate(3, java.sql.Date.valueOf(cliente.getDataNascimento()));
                } else {
                    psCliente.setDate(3, null);
                }

                psCliente.setLong(4, cliente.getId());

                int linhas = psCliente.executeUpdate();
                if (linhas == 0) {
                    throw new RuntimeException("Cliente não encontrado para atualizar");
                }
            }

            // 2. Trabalhar endereço (se existir no objeto)
            if (cliente.getEndereco() != null) {

                boolean enderecoExiste;

                // verificar se já existe no banco
                try (PreparedStatement psVerifica = conn.prepareStatement(sqlVerificaEndereco)) {
                    psVerifica.setLong(1, cliente.getId());

                    try (ResultSet rs = psVerifica.executeQuery()) {
                        enderecoExiste = rs.next();
                    }
                }

                if (enderecoExiste) {
                    // UPDATE
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateEndereco)) {
                        psUpdate.setString(1, cliente.getEndereco().getCidade());
                        psUpdate.setString(2, cliente.getEndereco().getBairro());
                        psUpdate.setString(3, cliente.getEndereco().getRua());
                        psUpdate.setString(4, cliente.getEndereco().getCep());
                        psUpdate.setLong(5, cliente.getId());

                        psUpdate.executeUpdate();
                    }
                } else {
                    // INSERT
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsertEndereco)) {
                        psInsert.setLong(1, cliente.getId());
                        psInsert.setString(2, cliente.getEndereco().getCidade());
                        psInsert.setString(3, cliente.getEndereco().getBairro());
                        psInsert.setString(4, cliente.getEndereco().getRua());
                        psInsert.setString(5, cliente.getEndereco().getCep());

                        psInsert.executeUpdate();
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Erro ao atualizar cliente", e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deletar(String cpf) {
        String sql = "DELETE FROM clientes WHERE cpf=?";

        try (Connection conn = ConexaoSql.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);

            int linas = ps.executeUpdate();
            if (linas == 0) {
                throw new RuntimeException("cliente nao encontrado");
            }


        } catch (SQLException e) {
            throw new RuntimeException("erro ao deletar", e);
        }


    }
}
