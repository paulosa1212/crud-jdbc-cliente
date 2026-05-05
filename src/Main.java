import dao.ClienteDao;
import dao.ClienteDaoInterface;
import entity.Cliente;
import entity.Endereco;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ClienteDaoInterface clienteDao = new ClienteDao();

        int opcao;

        do {
            System.out.println("\n==== MENU ====");
            System.out.println("1 - Listar clientes");
            System.out.println("2 - Buscar por CPF");
            System.out.println("3 - Salvar cliente");
            System.out.println("4 - Atualizar cliente");
            System.out.println("5 - Deletar cliente");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            opcao = sc.nextInt();
            sc.nextLine(); // limpar buffer

            switch (opcao) {

                case 1:
                    List<Cliente> lista = clienteDao.listarTodos();
                    for (Cliente c : lista) {
                        System.out.println("ID: " + c.getId());
                        System.out.println("Nome: " + c.getNome());
                        System.out.println("CPF: " + c.getCpf());
                        System.out.println("Data: " + c.getDataNascimento());

                        if (c.getEndereco() != null) {
                            System.out.println("Cidade: " + c.getEndereco().getCidade());
                        }

                        System.out.println("----------------------");
                    }
                    break;

                case 2:
                    System.out.print("Digite o CPF: ");
                    String cpfBusca = sc.nextLine();

                    Cliente encontrado = clienteDao.buscarPorCpf(cpfBusca);

                    if (encontrado != null) {
                        System.out.println("Nome: " + encontrado.getNome());
                        System.out.println("CPF: " + encontrado.getCpf());

                        if (encontrado.getEndereco() != null) {
                            System.out.println("Cidade: " + encontrado.getEndereco().getCidade());
                        }
                    } else {
                        System.out.println("Cliente não encontrado.");
                    }
                    break;

                case 3:
                    Cliente novo = new Cliente();

                    System.out.print("Nome: ");
                    novo.setNome(sc.nextLine());

                    System.out.print("CPF: ");
                    novo.setCpf(sc.nextLine());

                    System.out.print("Data nascimento (YYYY-MM-DD): ");
                    novo.setDataNascimento(LocalDate.parse(sc.nextLine()));

                    System.out.print("Tem endereço? (s/n): ");
                    String temEndereco = sc.nextLine();

                    if (temEndereco.equalsIgnoreCase("s")) {
                        Endereco end = new Endereco();

                        System.out.print("Cidade: ");
                        end.setCidade(sc.nextLine());

                        System.out.print("Bairro: ");
                        end.setBairro(sc.nextLine());

                        System.out.print("Rua: ");
                        end.setRua(sc.nextLine());

                        System.out.print("CEP: ");
                        end.setCep(sc.nextLine());

                        novo.setEndereco(end);
                    }

                    clienteDao.salvar(novo);
                    System.out.println("Cliente salvo!");
                    break;

                case 4:
                    System.out.print("CPF do cliente para atualizar: ");
                    String cpfUpdate = sc.nextLine();

                    Cliente cUpdate = clienteDao.buscarPorCpf(cpfUpdate);

                    if (cUpdate == null) {
                        System.out.println("Cliente não encontrado.");
                        break;
                    }

                    System.out.print("Novo nome: ");
                    cUpdate.setNome(sc.nextLine());

                    clienteDao.atualizar(cUpdate);
                    System.out.println("Atualizado!");
                    break;

                case 5:
                    System.out.print("CPF para deletar: ");
                    String cpfDelete = sc.nextLine();

                    clienteDao.deletar(cpfDelete);
                    System.out.println("Deletado!");
                    break;

                case 0:
                    System.out.println("Saindo...");
                    break;

                default:
                    System.out.println("Opção inválida!");
            }

        } while (opcao != 0);

        sc.close();
    }
}