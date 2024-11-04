package util;

import java.util.ArrayList;

public class OutputData {
    private static ArrayList<String> valid = new ArrayList<>();
    private static ArrayList<String> invalid = new ArrayList<>();
    private static ArrayList<String> invalidSyntax = new ArrayList<>();
    private static ArrayList<Integer> invalidSyntaxLines = new ArrayList<>();

    public static void addOutputValid(String token, int row, int column, String type, int id){
        token = token.substring(token.length()-1, token.length()).equals("\n") ? token.substring(0, token.length()-1) : token;
        valid.add(token + " na linha: " + row + ", coluna: " + column + " do tipo '" + type + "' | ID: '" + id + "'");
    }

    public static void addOutputInvalid(String token, int row, int column, String type, int id){
        token = token.substring(token.length()-1, token.length()).equals("\n") ? token.substring(0, token.length()-1) : token;
        invalid.add("Er87979ro: " + token + " na linha: " + row + ", coluna: " + column + " do tipo '" + type + "' | ID: '" + id + "'");
    }

    public static void addInvalidSyntax(int row, String syntacticError){
        if(!invalidSyntaxLines.contains(row)){
            invalidSyntaxLines.add(row);
            invalidSyntax.add("Erro(s) sintatico(s) encontrado(s) na linha: " + row + ":" + syntacticError + " \n");
        }
    }

    public static void removeLastOutputInvalid(){
        invalid.remove(invalid.size()-1);
    }

    public static ArrayList<String> getValid(){
        return valid;
    }

    public static ArrayList<String> getInvalid(){
        return invalid;
    }

    public static ArrayList<String> getInvalidSyntax(){ return invalidSyntax; }

    public static void clean(){
        valid.clear();
        invalid.clear();
        invalidSyntax.clear();
        invalidSyntaxLines.clear();
    }
}