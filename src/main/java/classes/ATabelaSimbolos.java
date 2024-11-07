package classes;

public class ATabelaSimbolos {
    private String simbolo;
    private String endereco;

    public ATabelaSimbolos(String simbolo, String endereco) {
        this.simbolo = simbolo;
        this.endereco = endereco;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}
