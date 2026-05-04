package dao;

import config.ConexaoSql;
import entity.Cliente;
import entity.Endereco;

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
                    FROM cliente c
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
        String sqlClientes = "INSERT INTO clientes(nome,cpf,data_nascimento) VALUES(?,?,?)";
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
            pscadastro.executeUpdate();
            ResultSet rs = pscadastro.getGeneratedKeys();
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
                conn.commit();

            }
        } catch (SQLException e) {
         try {
             if (conn!=null){
                 conn.rollback();
             }
         }catch (SQLException e1){
             e1.printStackTrace();
         }
        }finally {
            try {
                if (pscadastro!=null){
                    pscadastro.close();
                }if (psendereco!=null){
                    psendereco.close();
                }if (conn!=null){
                    conn.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }


    }

    @Override
    public Cliente buscarPorCpf(String cpf) {
        return null;
    }

    @Override
    public void atualizar(Cliente cliente) {

    }

    @Override
    public void deltar(String cpf) {

    }
}
