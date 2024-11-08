package classes;

import java.util.ArrayList;
import java.util.List;

public class ATabelaInstrucoes {
    private List<Instrucao> instrucoes;

    public ATabelaInstrucoes() {
        this.instrucoes = new ArrayList<>();
    }

    public void adicionarInstrucao(int numero, String codigo, Object parametro) {
        instrucoes.add(new Instrucao(numero, codigo, parametro));
    }

    public void set(int numero, Instrucao instrucao) {
        instrucoes.set(numero, instrucao);
    }

    public Instrucao getInstrucao(int numero) {
        for (Instrucao instrucao : instrucoes) {
            if (instrucao.getNumero() == numero) {
                return instrucao;
            }
        }
        return null;
    }

    public List<Instrucao> getInstrucoes() {
        return instrucoes;
    }
}

class Instrucao {
    private int numero;
    private String codigo;
    private Object parametro;

    public Instrucao(int numero, String codigo, Object parametro) {
        this.numero = numero;
        this.codigo = codigo;
        this.parametro = parametro;
    }

    public int getNumero() {
        return numero;
    }

    public String getCodigo() {
        return codigo;
    }

    public Object getParametro() {
        return parametro;
    }
}