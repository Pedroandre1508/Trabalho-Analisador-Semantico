package classes;

import java.util.ArrayList;

public class Acoes {
    int VT = 0, VP = 0, ponteiro = 1;
    String contexto;
    int tipo; // Corrigido para ser um único valor em vez de uma lista
    //ArrayList<Integer> pilhaDesvios = new ArrayList<>();
    ArrayList<String> areaInstrucoes = new ArrayList<>();
    ArrayList<String> tabelaSimbolos = new ArrayList<>();

    // Ação #1: reconhecimento de fim de programa
    public void fimPrograma() {
        gerarInstrucao(ponteiro, "STP", 0);
    }

    // Ação #2: reconhecimento do identificador de programa
    public void identificadorPrograma(String identificador) {
        tabelaSimbolos.add(identificador + ", 0, -");
    }

    // Ação #3: reconhecimento da palavra reservada const
    public void reconhecerConst() {
        contexto = "constante";
    }

    // Ação #4: reconhecimento do término da declaração de constantes e/ou variáveis de um determinado tipo
    public void terminoDeclaracao() {
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
    public void valorDeclaracaoConstante(int valor) {
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
    public void reconhecerVar() {
        contexto = "variavel";
    }

    // Ação #7: reconhecimento da palavra reservada int
    public void reconhecerInt() {
        if (contexto.equals("variavel")) {
            tipo = 1;
        } else {
            tipo = 5;
        }
    }

    // Ação #8: reconhecimento da palavra reservada real
    public void reconhecerReal() {
        if (contexto.equals("variavel")) {
            tipo = 2;
        } else {
            tipo = 6;
        }
    }

    // Ação #9: reconhecimento da palavra reservada char
    public void reconhecerChar() {
        if (contexto.equals("variavel")) {
            tipo = 3;
        } else {
            tipo = 7;
        }
    }

    // Ação #10: reconhecimento da palavra reservada bool
    public void reconhecerBool() {
        if (contexto.equals("variavel")) {
            tipo = 4;
        } else {
            System.err.println("Erro: tipo inválido para constante");
        }
    }

    // Ação #11: reconhecimento de identificador
    public void reconhecerIdentificador(String identificador) {
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
    public void reconhecerIdentificadorAtribuicao(String identificador) {
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
    public void reconhecerGet() {
        contexto = "entrada dados";
    }

    // Ação #14: reconhecimento de mensagem em comando de saída de dados
    public void reconhecerMensagemSaida() {
        gerarInstrucao(ponteiro, "WRT", 0);
        ponteiro++;
    }

    // Ação #15: reconhecimento de identificador em comando de saída ou em expressão
    public void reconhecerIdentificadorSaida(String identificador) {
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
    public void reconhecerConstanteInteira(int constante) {
        gerarInstrucao(ponteiro, "LDI", constante);
        ponteiro++;
    }

    // Ação #17: reconhecimento de constante real em comando de saída ou em expressão
    public void reconhecerConstanteReal(double constante) {
        gerarInstrucao(ponteiro, "LDR", constante);
        ponteiro++;
    }

    // Ação #18: reconhecimento de constante literal em comando de saída ou em expressão
    public void reconhecerConstanteLiteral(String constante) {
        gerarInstrucao(ponteiro, "LDS", constante);
        ponteiro++;
    }

    // Ação #19: reconhecimento de constante lógica verdadeiro
    public void reconhecerConstanteLogicaVerdadeiro() {
        gerarInstrucao(ponteiro, "LDB", "TRUE");
        ponteiro++;
    }

    // Ação #20: reconhecimento de constante lógica falso
    public void reconhecerConstanteLogicaFalso() {
        gerarInstrucao(ponteiro, "LDB", "FALSE");
        ponteiro++;
    }

    // Ação #21: reconhecimento de expressão em comando de seleção
    public void reconhecerExpressaoSelecao() {
        gerarInstrucao(ponteiro, "JMF", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #22: reconhecimento do fim de comando de seleção
    public void fimComandoSelecao() {
        int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(endereco, "JMF " + ponteiro);
    }

    // Ação #23: reconhecimento da cláusula senão em comando de seleção
    public void reconhecerClausulaSenao() {
        int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(endereco, "JMF " + (ponteiro + 1));
        gerarInstrucao(ponteiro, "JMP", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #24: reconhecimento da palavra reservada while
    public void reconhecerWhile() {
        pilhaDesvios.add(ponteiro);
    }

    // Ação #25: reconhecimento de expressão em comando de repetição
    public void reconhecerExpressaoRepeticao() {
        gerarInstrucao(ponteiro, "JMF", "?");
        ponteiro++;
        pilhaDesvios.add(ponteiro - 1);
    }

    // Ação #26: reconhecimento do fim do comando de repetição
    public void fimComandoRepeticao() {
        int enderecoJMF = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        // Atualizar a instrução de desvio com o endereço atual
        // Exemplo: areaInstrucoes.set(enderecoJMF, "JMF " + (ponteiro + 1));
        int enderecoWhile = pilhaDesvios.remove(pilhaDesvios.size() - 1);
        gerarInstrucao(ponteiro, "JMP", enderecoWhile);
        ponteiro++;
    }

    // Ação #27: reconhecimento de operação relacional igual
    public void reconhecerOperacaoRelacionalIgual() {
        gerarInstrucao(ponteiro, "EQL", 0);
        ponteiro++;
    }

    // Ação #28: reconhecimento de operação relacional diferente
    public void reconhecerOperacaoRelacionalDiferente() {
        gerarInstrucao(ponteiro, "DIF", 0);
        ponteiro++;
    }

    // Ação #29: reconhecimento de operação relacional menor
    public void reconhecerOperacaoRelacionalMenor() {
        gerarInstrucao(ponteiro, "SMR", 0);
        ponteiro++;
    }

    // Ação #30: reconhecimento de operação relacional maior
    public void reconhecerOperacaoRelacionalMaior() {
        gerarInstrucao(ponteiro, "BGR", 0);
        ponteiro++;
    }

    // Ação #33: reconhecimento de operação aritmética adição
    public void reconhecerOperacaoAritmeticaAdicao() {
        gerarInstrucao(ponteiro, "ADD", 0);
        ponteiro++;
    }

    // Ação #34: reconhecimento de operação aritmética subtração
    public void reconhecerOperacaoAritmeticaSubtracao() {
        gerarInstrucao(ponteiro, "SUB", 0);
        ponteiro++;
    }

    // Ação #35: reconhecimento de operação lógica OU
    public void reconhecerOperacaoLogicaOU() {
        gerarInstrucao(ponteiro, "OR", 0);
        ponteiro++;
    }

    // Ação #36: reconhecimento de operação aritmética multiplicação
    public void reconhecerOperacaoAritmeticaMultiplicacao() {
        gerarInstrucao(ponteiro, "MUL", 0);
        ponteiro++;
    }

    // Ação #37: reconhecimento de operação aritmética divisão real
    public void reconhecerOperacaoAritmeticaDivisaoReal() {
        gerarInstrucao(ponteiro, "DIV", 0);
        ponteiro++;
    }

    // Ação #40: reconhecimento de operação lógica E
    public void reconhecerOperacaoLogicaE() {
        gerarInstrucao(ponteiro, "AND", 0);
        ponteiro++;
    }

    // Ação #42: reconhecimento de operação lógica NÃO
    public void reconhecerOperacaoLogicaNAO() {
        gerarInstrucao(ponteiro, "NOT", 0);
        ponteiro++;
    }

    // Método auxiliar para gerar instruções
    private void gerarInstrucao(int ponteiro, String instrucao, Object operando) {
        areaInstrucoes.add(ponteiro + ": " + instrucao + " " + operando);
    }
}