package dao;

import entity.Cliente;

import java.util.List;

public interface ClienteDaoInterface {
    List<Cliente> listarTodos();

    void salvar(Cliente cliente);

    Cliente buscarPorCpf(String cpf);

    void atualizar(Cliente cliente);

    void deltar(String cpf);

}
