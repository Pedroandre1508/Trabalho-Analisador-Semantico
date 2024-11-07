package classes;

public class ATabelaInstrucoes {
    private String instrucao;
    private String operando;

    public ATabelaInstrucoes(String instrucao, String operando) {
        this.instrucao = instrucao;
        this.operando = operando;
    }

    public String getInstrucao() {
        return instrucao;
    }

    public void setInstrucao(String instrucao) {
        this.instrucao = instrucao;
    }

    public String getOperando() {
        return operando;
    }

    public void setOperando(String operando) {
        this.operando = operando;
    }

}
