package classes;

import java.util.ArrayList;
import java.util.List;

public class ATabelaSimbolos {
    private List<Simbolo> simbolos;

    public ATabelaSimbolos() {
        this.simbolos = new ArrayList<>();
    }

    public void adicionarSimbolo(String nome, int categoria, int atributo) {
        simbolos.add(new Simbolo(nome, categoria, atributo));
    }

    public Simbolo getSimbolo(String nome) {
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getNome().equals(nome)) {
                return simbolo;
            }
        }
        return null;
    }

    public List<Simbolo> getSimbolos() {
        return simbolos;
    }

    public boolean contains(String nome) {
        return getSimbolo(nome) != null;
    }
}

class Simbolo {
    private String nome;
    private int categoria;
    private int atributo;

    public Simbolo(String nome, int categoria, int atributo) {
        this.nome = nome;
        this.categoria = categoria;
        this.atributo = atributo;
    }

    public String getNome() {
        return nome;
    }

    public int getCategoria() {
        return categoria;
    }

    public int getAtributo() {
        return atributo;
    }
}