package dao;

import entity.Endereco;

import java.util.List;

public interface EnderecoDaoInterface {

    void salvar(Endereco endereco);

    Endereco buscarPorId(String cpf);

    List<Endereco> listarTodos();

    void atualizar(Endereco endereco);

    void deletar(String cpf);

}
