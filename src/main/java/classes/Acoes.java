package classes;

import java.util.ArrayList;

public class Acoes {
    int VT = 0, VP = 0, ponteiro = 1;
    String contexto;
    int tipo; // Corrigido para ser um único valor em vez de uma lista
    ArrayList<Integer> pilhaDesvios = new ArrayList<>();
    ArrayList<String> areaInstrucoes = new ArrayList<>();
    ArrayList<String> tabelaSimbolos = new ArrayList<>();

    // Ação #1: reconhecimento de fim de programa
    public void acao1() {
        gerarInstrucao(ponteiro, "STP", 0);
    }

    // Ação #2: reconhecimento do identificador de programa
    public void acao2(String identificador) {
        tabelaSimbolos.add(identificador + ", 0, -");
    }

    // Ação #3: reconhecimento da palavra reservada const
    public void acao3() {
        contexto = "constante";
    }

    // Ação #4: reconhecimento do término da declaração de constantes e/ou variáveis de um determinado tipo
    public void acao4() {
        switch (tipo) {
            case 1:
            case 5:
                gerarInstrucao(ponteiro, "ALI", VP);
                break;
            case 2:
            case 6:
                gerarInstrucao(ponteiro, "ALR", VP);
                break;
            case 3:
            case 7:
                gerarInstrucao(ponteiro, "ALS", VP);
                break;
            case 4:
                gerarInstrucao(ponteiro, "ALB", VP);
                break;
        }
        ponteiro++;
        if (tipo >= 1 && tipo <= 4) {
            VP = 0;
        }
    }

    // Ação #5: reconhecimento de valor na declaração de constante
    public void acao5(int valor) {
        switch (tipo) {
            case 5:
                gerarInstrucao(ponteiro, "LDI", valor);
                break;
            case 6:
                gerarInstrucao(ponteiro, "LDR", valor);
                break;
            case 7:
                gerarInstrucao(ponteiro, "LDS", valor);
                break;
        }
        ponteiro++;
        gerarInstrucao(ponteiro, "STC", VP);
        ponteiro++;
        VP = 0;
    }

    // Ação #6: reconhecimento da palavra reservada var
    public void acao6() {
        contexto = "variavel";
    }

    // Ação #7: reconhecimento da palavra reservada int
    public void acao7() {
        if (contexto.equals("variavel")) {
            tipo = 1;
        } else {
            tipo = 5;
        }
    }

    // Ação #8: reconhecimento da palavra reservada real
    public void acao8() {
        if (contexto.equals("variavel")) {
            tipo = 2;
        } else {
            tipo = 6;
        }
    }

    // Ação #9: reconhecimento da palavra reservada char
    public void acao9() {
        if (contexto.equals("variavel")) {
            tipo = 3;
        } else {
            tipo = 7;
        }
    }

    // Ação #10: reconhecimento da palavra reservada bool
    public void acao10() {
        if (contexto.equals("variavel")) {
            tipo = 4;
        } else {
            System.err.println("Erro: tipo inválido para constante");
        }
    }

    // Ação #11: reconhecimento de identificador
    //validar novamente
    public void acao11(String identificador) {
        switch (contexto) {
            case "constante":
            case "variavel":
                if (tabelaSimbolos.contains(identificador)) {
                    System.err.println("Erro: identificador já declarado");
                } else {
                    VT++;
                    VP++;
                    tabelaSimbolos.add(identificador + ", " + tipo + ", " + VT);
                }
                break;
            case "entrada dados":
                if (tabelaSimbolos.contains(identificador)) {
                    // Recuperar categoria e atributo do identificador
                    String categoria = "categoria"; // Exemplo, deve ser recuperado da tabela de símbolos
                    String atributo = "atributo"; // Exemplo, deve ser recuperado da tabela de símbolos
                    gerarInstrucao(ponteiro, "REA", categoria);
                    ponteiro++;
                    gerarInstrucao(ponteiro, "STR", atributo);
                    ponteiro++;
                } else {
                    System.err.println("Erro: identificador não declarado");
                }
                break;
        }
    }

    // Ação #12: reconhecimento de identificador em comando de atribuição
    //validar novamente
    public void acao12(String identificador) {
        if (tabelaSimbolos.contains(identificador)) {
            // Recuperar atributo do identificador
            String atributo = "atributo"; // Exemplo, deve ser recuperado da tabela de símbolos
            gerarInstrucao(ponteiro, "STR", atributo);
            ponteiro++;
        } else {
            System.err.println("Erro: identificador não declarado");
        }
    }

    // Ação #13: reconhecimento da palavra reservada get
    public void acao13() {
        contexto = "entrada dados";
    }

    // Ação #14: reconhecimento de mensagem em comando de saída de dados
    public void acao14() {
        gerarInstrucao(ponteiro, "WRT", 0);
        ponteiro++;
    }

    // Ação #15: reconhecimento de identificador em comando de saída ou em expressão
    //validar novamente
    public void acao15(String identificador) {
        if (tabelaSimbolos.contains(identificador)) {
            // Recuperar atributo do identificador
            String atributo = "atributo"; // Exemplo, deve ser recuperado da tabela de símbolos
            gerarInstrucao(ponteiro, "LDV", atributo);
            ponteiro++;
        } else {
            System.err.println("Erro: identificador não declarado");
        }
    }

    // Ação #16: reconhecimento de constante inteira em comando de saída ou em expressão
    public void acao16(int constante) {
        gerarInstrucao(ponteiro, "LDI", constante);
        ponteiro++;
    }

    // Ação #17: reconhecimento de constante real em comando de saída ou em expressão
    public void acao17(double constante) {
        gerarInstrucao(ponteiro, "LDR", constante);
        ponteiro++;
    }

    // Ação #18: reconhecimento de constante literal em comando de saída ou em expressão
    public void acao18(String constante) {
        gerarInstrucao(ponteiro, "LDS", constante);
        ponteiro++;
    }

    // Ação #19: reconhecimento de constante lógica verdadeiro
    public void acao19() {
        gerarInstrucao(ponteiro, "LDB", "TRUE");
        ponteiro++;
    }

    // Ação #20: reconhecimento de constante lógica falso
    public void acao20() {
        gerarInstrucao(ponteiro, "LDB", "FALSE");
        ponteiro++;
    }

    // Ação #21: reconhecimento de expressão em comando de seleção
    public void acao21() {
        gerarInstrucao(ponteiro, "JMF", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #22: reconhecimento do fim de comando de seleção
    //validar novamente
    public void acao22() {
        int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(endereco, "JMF " + ponteiro);
    }

    // Ação #23: reconhecimento da cláusula senão em comando de seleção
    //validar novamente
    public void acao23() {
        int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(endereco, "JMF " + (ponteiro + 1));
        gerarInstrucao(ponteiro, "JMP", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #24: reconhecimento da palavra reservada while
    public void acao24() {
        pilhaDesvios.add(ponteiro);
    }

    // Ação #25: reconhecimento de expressão em comando de repetição
    public void acao25() {
        gerarInstrucao(ponteiro, "JMF", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #26: reconhecimento do fim do comando de repetição
    //validar novamente
    public void acao26() {
        int enderecoJMF = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(enderecoJMF, "JMF " + (ponteiro + 1));
        int enderecoWhile = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        gerarInstrucao(ponteiro, "JMP", enderecoWhile);
        ponteiro++;
    }

    // Ação #27: reconhecimento de operação relacional igual
    public void acao27() {
        gerarInstrucao(ponteiro, "EQL", 0);
        ponteiro++;
    }

    // Ação #28: reconhecimento de operação relacional diferente
    public void acao28() {
        gerarInstrucao(ponteiro, "DIF", 0);
        ponteiro++;
    }

    // Ação #29: reconhecimento de operação relacional menor
    public void acao29() {
        gerarInstrucao(ponteiro, "SMR", 0);
        ponteiro++;
    }

    // Ação #30: reconhecimento de operação relacional maior
    public void acao30() {
        gerarInstrucao(ponteiro, "BGR", 0);
        ponteiro++;
    }

    // Ação #31: reconhecimento de operação relacional maior
    //validar novamente
    public void acao31() {
    }


    // Ação #32: reconhecimento de operação relacional maior
    //validar novamente
    public void acao32() {
     
    }

    // Ação #33: reconhecimento de operação aritmética adição
    public void acao33() {
        gerarInstrucao(ponteiro, "ADD", 0);
        ponteiro++;
    }

    // Ação #34: reconhecimento de operação aritmética subtração
    public void acao34() {
        gerarInstrucao(ponteiro, "SUB", 0);
        ponteiro++;
    }

    // Ação #35: reconhecimento de operação lógica OU
    public void acao35() {
        gerarInstrucao(ponteiro, "OR", 0);
        ponteiro++;
    }

    // Ação #36: reconhecimento de operação aritmética multiplicação
    public void acao36() {
        gerarInstrucao(ponteiro, "MUL", 0);
        ponteiro++;
    }

    // Ação #37: reconhecimento de operação aritmética divisão real
    public void acao37() {
        gerarInstrucao(ponteiro, "DIV", 0);
        ponteiro++;
    }

    // Ação #38: reconhecimento de operação relacional maior
    //validar novamente
    public void acao38() {
     
    }

    // Ação #39: reconhecimento de operação relacional maior
    //validar novamente
    public void acao39() {
     
    }

    // Ação #40: reconhecimento de operação lógica E
    public void acao40() {
        gerarInstrucao(ponteiro, "AND", 0);
        ponteiro++;
    }

    // Ação #41: reconhecimento de operação relacional maior
    //validar novamente
    public void acao41() {
     
    }

    // Ação #42: reconhecimento de operação lógica NÃO
    public void acao42() {
        gerarInstrucao(ponteiro, "NOT", 0);
        ponteiro++;
    }

    // Método auxiliar para gerar instruções
    private void gerarInstrucao(int ponteiro, String instrucao, Object operando) {
        areaInstrucoes.add(ponteiro + ": " + instrucao + " " + operando);
    }
}