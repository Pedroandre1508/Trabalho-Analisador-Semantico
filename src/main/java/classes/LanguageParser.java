package classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LanguageParser implements LanguageParserConstants {
  private int contParseError = 0;
  private final static List<AErrorStruct> output = new ArrayList<AErrorStruct>();
  private boolean eof;

  private ATabelaSimbolos tabelaSimbolos = new ATabelaSimbolos();
  private ATabelaInstrucoes tabelaInstrucoes = new ATabelaInstrucoes();
  private int VT = 0, VP = 0, ponteiro = 1;
  private String contexto;
  private int tipo;
  private ArrayList<Integer> pilhaDesvios = new ArrayList<>();
  private List<SemanticException> semanticErrors = new ArrayList<>();


  public static void main(String[] args) throws TokenMgrError, ParseException {
    LanguageParser parser;
    if (args.length == 0) {
      parser = new LanguageParser(System.in);
    } else if (args.length == 1) {
      try {
        parser = new LanguageParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("LanguageParser: file " + args[0] + " was not found.");
        return;
      }
    }
  }

  public static LanguageParser create(String stream) {
    InputStream target = new ByteArrayInputStream(stream.getBytes());
    return new LanguageParser(target);
  }

  public static List<Token> tokenize(LanguageParser parser) {
    List<Token> tokens = new ArrayList<Token>();
    Token token = parser.getNextToken();

    while (token.kind != LanguageParserConstants.EOF) {
      tokens.add(token);
      token = parser.getNextToken();
    }

    if (!ATokenHandler.isClosed()) {
      tokens.add(ATokenHandler.createToken());
    }
    return tokens;
  }

  public void consumeUntil(ARecoverySet g, ParseException e, String met) throws ParseException {
    Token tok;
    if (g == null) {
      throw e;
    }
    tok = getToken(1);
    while (!eof) {
      if (g.contains(tok.kind)) {
        break;
      }
      getNextToken();
      tok = getToken(1);
      if (tok.kind == EOF && !g.contains(EOF)) {
        eof = true;
      }
    }
    contParseError++;
  }

  public static ArrayList<AErrorStruct> analisadorSintatico(String input) throws SemanticException {
    ArrayList<AErrorStruct> output = new ArrayList<>();
    LanguageParser parser = create(input);
    boolean hasErrors = false;

    try {
        parser.programa();
    } catch (ParseException e) {
        hasErrors = true;
        AErrorStruct errorStruct = new AErrorStruct("\n"+ e.getMessage(), e);
        errorStruct.setExpected(e.expectedTokenSequences, e.tokenImage);
        output.add(errorStruct);
        System.err.println("Erro sintático: " + e.getMessage());
        System.err.println("Linha: " + e.currentToken.next.beginLine + ", Coluna: " + e.currentToken.next.beginColumn);
        System.err.println("Esperado: " + getExpectedTokens(e.expectedTokenSequences, e.tokenImage));
    }

    if (hasErrors) {
        System.err.println("Erro(s) sint\u00e1ticos encontrados.");
    }

    return output;
}

  // Método auxiliar para obter os tokens esperados como uma string
  private static String getExpectedTokens(int[][] expectedTokenSequences, String[] tokenImage) {
    StringBuilder expected = new StringBuilder();
    for (int[] sequence : expectedTokenSequences) {
        for (int token : sequence) {
            expected.append(tokenImage[token]).append(" ");
        }
        expected.append("\n");
    }
    return expected.toString();
  }

  public void exibirErrosSintaticos(String input) {
    try {
        ArrayList<AErrorStruct> errosSintaticos = analisadorSintatico(input);
        if (!errosSintaticos.isEmpty()) {
            for (AErrorStruct erro : errosSintaticos) {
                System.out.println(erro.toString());
            }
        }
    } catch (SemanticException e) {
        e.printStackTrace();
    }
  }

  public static List<AIntermediateCode> analisadorSemantico(String input, List<String> semanticErrors) throws ParseException, SemanticException {
    List<AIntermediateCode> intermediateCodeList = new ArrayList<>();
    LanguageParser parser = create(input);

    parser.programa();

    // Verifique se há erros semânticos
    if (!parser.getSemanticErrors().isEmpty()) {
        for (SemanticException e : parser.getSemanticErrors()) {
            String errorMessage = "\n" + e.getMessage();
            semanticErrors.add(errorMessage);
        }
    } else {
        // Adicionar as instruções geradas na lista de AIntermediateCode
        parser.tabelaInstrucoes.getInstrucoes().forEach(instrucao -> {
            intermediateCodeList.add(new AIntermediateCode(
                String.valueOf(instrucao.getNumero()),
                instrucao.getCodigo(),
                String.valueOf(instrucao.getParametro())
            ));
        });
    }

    return intermediateCodeList;
}

public class SemanticException extends Exception {
  private int line;
  private int column;

  public SemanticException(String message, int line, int column) {
      super(message);
      this.line = line;
      this.column = column;
  }

  public int getLine() {
      return line;
  }

  public int getColumn() {
      return column;
  }

  @Override
  public String getMessage() {
      return super.getMessage() + " (Linha: " + line + ", Coluna: " + column + ")";
  }
}

  public List<SemanticException> getSemanticErrors() {
    return semanticErrors;
  }

  public static List<Token> getTokens(String stream) {
    InputStream target = new ByteArrayInputStream(stream.getBytes());
    LanguageParser parser = new LanguageParser(target);
    return tokenize(parser);
  }

  static public String im(int x) {
    String s = tokenImage[x];
    int k = s.lastIndexOf("\"");
    try {
      s = s.substring(1, k);
    } catch (StringIndexOutOfBoundsException e) {
      // Handle exception or log if necessary
    }
    return s;
  }

  // Ação #1: reconhecimento de fim de programa
  public void acao1() {
    gerarInstrucao(ponteiro, "STP", 0);
  }

  // Ação #2: reconhecimento do identificador de programa
  public void acao2(String identificador) {
    tabelaSimbolos.adicionarSimbolo(identificador, 0, -1);
  }

  // Ação #3: reconhecimento da palavra reservada const
  public void acao3() {
    contexto = "constante";
  }

  // Ação #4: reconhecimento do término da declaração de constantes e/ou variáveis
  // de um determinado tipo
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
  public void acao10() throws SemanticException {
    if (contexto.equals("variavel")) {
      tipo = 4;
    } else {
      semanticErrors.add(new SemanticException("Erro Semantico: tipo inválido para constante", token.beginLine, token.beginColumn));

    }
  }

  // Ação #11: reconhecimento de identificador
  public void acao11(String identificador) throws SemanticException {
    switch (contexto) {
      case "constante":
      case "variavel":
        if (tabelaSimbolos.contains(identificador)) {
          semanticErrors.add(new SemanticException("Erro Semantico: identificador já declarado", token.beginLine, token.beginColumn));
        } else {
          VT++;
          VP++;
          tabelaSimbolos.adicionarSimbolo(identificador, tipo, VT);
        }
        break;
      case "entrada dados":
        if (tabelaSimbolos.contains(identificador)) {
          Simbolo simbolo = tabelaSimbolos.getSimbolo(identificador);
          gerarInstrucao(ponteiro, "REA", simbolo.getCategoria());
          ponteiro++;
          gerarInstrucao(ponteiro, "STR", simbolo.getAtributo());
          ponteiro++;
        } else {
          semanticErrors.add(new SemanticException("Erro Semantico: identificador não declarado", token.beginLine, token.beginColumn));
        }
        break;
    }
  }

  // Ação #12: reconhecimento de identificador em comando de atribuição
  public void acao12(String identificador) throws SemanticException {
    if (tabelaSimbolos.contains(identificador)) {
        Simbolo simbolo = tabelaSimbolos.getSimbolo(identificador);
        
        // Verificar se o identificador é um identificador de variável
        if (simbolo.getCategoria() >= 1 && simbolo.getCategoria() <= 4) { // Tipos 1 a 4 são variáveis
            gerarInstrucao(ponteiro, "STR", simbolo.getAtributo());
            ponteiro++;
        } else {
            semanticErrors.add(new SemanticException("Erro Semantico: identificador de programa ou de constante", token.beginLine, token.beginColumn));
        }
    } else {
        semanticErrors.add(new SemanticException("Erro Semantico: identificador não declarado", token.beginLine, token.beginColumn));
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
  public void acao15(String identificador) throws SemanticException {
    if (tabelaSimbolos.contains(identificador)) {
        Simbolo simbolo = tabelaSimbolos.getSimbolo(identificador);
        
        // Verificar se o identificador é uma constante ou uma variável
        if ((simbolo.getCategoria() >= 1 && simbolo.getCategoria() <= 4) || (simbolo.getCategoria() >= 5 && simbolo.getCategoria() <= 7)) {
            gerarInstrucao(ponteiro, "LDV", simbolo.getAtributo());
            ponteiro++;
        } else {
            semanticErrors.add(new SemanticException("Erro Semantico: identificador de programa", token.beginLine, token.beginColumn));
        }
    } else {
        semanticErrors.add(new SemanticException("Erro Semantico: identificador não declarado", token.beginLine, token.beginColumn));
    }
  }

  // Ação #16: reconhecimento de constante inteira em comando de saída ou em
  // expressão
  public void acao16(int constante) {
    gerarInstrucao(ponteiro, "LDI", constante);
    ponteiro++;
  }

  // Ação #17: reconhecimento de constante real em comando de saída ou em
  // expressão
  public void acao17(double constante) {
    gerarInstrucao(ponteiro, "LDR", constante);
    ponteiro++;
  }

  // Ação #18: reconhecimento de constante literal em comando de saída ou em
  // expressão
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
  public void acao22() {
    // Desempilhar da pilha de desvios o endereço da instrução JMP (ou JMF) empilhado na ação #23 (ou ação #21)
    int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);

    // Atualizar a instrução de desvio com: endereço  ponteiro
    tabelaInstrucoes.set(endereco, new Instrucao(endereco, "JMP", ponteiro));
  }

  // Ação #23: reconhecimento da cláusula senão em comando de seleção
  public void acao23() {
    // Desempilhar da pilha de desvios o endereço da instrução JMF empilhado na ação #21
    int endereco = pilhaDesvios.remove(pilhaDesvios.size() - 1);

    // Atualizar a instrução de desvio com: endereço  ponteiro + 1
    tabelaInstrucoes.set(endereco, new Instrucao(endereco, "JMF", ponteiro + 1));

    // Gerar instrução: (ponteiro, JMP, ?), onde endereço = ?
    gerarInstrucao(ponteiro, "JMP", "?");
    ponteiro++;

    // Empilhar (ponteiro - 1) em pilha de desvios, ou seja, o endereço da instrução JMP
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
  public void acao26() {
    // Desempilhar da pilha de desvios o endereço da instrução de desvio empilhado na ação #25
    int enderecoJMF = pilhaDesvios.remove(pilhaDesvios.size() - 1);

    // Atualizar a instrução de desvio com: endereço  ponteiro + 1
    tabelaInstrucoes.set(enderecoJMF, new Instrucao(enderecoJMF, "JMF", ponteiro + 1));

    // Desempilhar da pilha de desvios o endereço da instrução empilhado na ação #24
    int enderecoWhile = pilhaDesvios.remove(pilhaDesvios.size() - 1);

    // Gerar instrução: (ponteiro, JMP, “endereço”), onde "endereço" é igual ao valor desempilhado
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

  // Ação #31: reconhecimento de operação relacional menor igual
  public void acao31() {
    gerarInstrucao(ponteiro, "SGE", 0);
    ponteiro++;
  }

  // Ação #32: reconhecimento de operação relacional maior igual
  public void acao32() {
    gerarInstrucao(ponteiro, "BGE", 0);
    ponteiro++;
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

  // Ação #38: reconhecimento de operação aritmética divisão inteira
  public void acao38() {
    gerarInstrucao(ponteiro, "DII", 0);
    ponteiro++;
  }

  // Ação #39: reconhecimento de operação aritmética resto da divisão inteira
  public void acao39() {
    gerarInstrucao(ponteiro, "DIR", 0);
    ponteiro++;
  }

  // Ação #40: reconhecimento de operação lógica E
  public void acao40() {
    gerarInstrucao(ponteiro, "AND", 0);
    ponteiro++;
  }

  // Ação #41: reconhecimento de operação aritmética potenciação
  public void acao41() {
    gerarInstrucao(ponteiro, "POT", 0);
    ponteiro++;
  }

  // Ação #42: reconhecimento de operação lógica NÃO
  public void acao42() {
    gerarInstrucao(ponteiro, "NOT", 0);
    ponteiro++;
  }

  // Método auxiliar para gerar instruções
  private void gerarInstrucao(int ponteiro, String instrucao, Object operando) {
    tabelaInstrucoes.adicionarInstrucao(ponteiro, instrucao, operando);
  }

  // Analisador Sintatico
  // Produções
  final public void programa() throws ParseException, SemanticException {
    trace_call("programa");
    try {

      jj_consume_token(MAKE);
      identificador_do_programa();
      declaracao_constantes_variaveis();
      lista_comandos();
      jj_consume_token(END);
      jj_consume_token(PONTO);
      acao1();
    } finally {
      trace_return("programa");
    }
  }

  final public void identificador_do_programa() throws ParseException {
    trace_call("identificador_do_programa");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case IDENTIFICADOR: {
          identificador();
          //verificar
          acao2(token.image);
          break;
        }
        default:
          jj_la1[0] = jj_gen;
          ;
      }
    } finally {
      trace_return("identificador_do_programa");
    }
  }

  final public void declaracao_constantes_variaveis() throws ParseException, SemanticException {
    trace_call("declaracao_constantes_variaveis");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case CONST:
        case VAR: {
          constantes_e_variaveis();
          break;
        }
        default:
          jj_la1[1] = jj_gen;
          ;
      }
    } finally {
      trace_return("declaracao_constantes_variaveis");
    }
  }

  final public void constantes_e_variaveis() throws ParseException, SemanticException {
    trace_call("constantes_e_variaveis");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case CONST: {
          declaracao_constantes();
          constantes_e_variaveis_prime();
          break;
        }
        case VAR: {
          declaracao_variaveis();
          constantes_e_variaveis_prime();
          break;
        }
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("constantes_e_variaveis");
    }
  }

  final public void constantes_e_variaveis_prime() throws ParseException, SemanticException {
    trace_call("constantes_e_variaveis_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case CONST:
        case VAR: {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case CONST: {
              declaracao_constantes();
              constantes_e_variaveis_prime();
              break;
            }
            case VAR: {
              declaracao_variaveis();
              constantes_e_variaveis_prime();
              break;
            }
            default:
              jj_la1[3] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          break;
        }
        default:
          jj_la1[4] = jj_gen;
          ;
      }
    } finally {
      trace_return("constantes_e_variaveis_prime");
    }
  }

  final public void declaracao_constantes() throws ParseException, SemanticException {
    trace_call("declaracao_constantes");
    try {

      jj_consume_token(CONST);
      acao3();
      constantes();
      jj_consume_token(END);
      jj_consume_token(PONTOVIRGULA);
    } finally {
      trace_return("declaracao_constantes");
    }
  }

  final public void constantes() throws ParseException, SemanticException {
    trace_call("constantes");
    try {

      tipo();
      jj_consume_token(DOISPONTOS);
      lista_identificadores();
      acao4();
      jj_consume_token(IGUAL);
      valor();
      // verificar
      acao5(tipo);
      jj_consume_token(PONTO);
      constantes_prime();
    } finally {
      trace_return("constantes");
    }
  }

  final public void constantes_prime() throws ParseException, SemanticException {
    trace_call("constantes_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case INT:
        case REAL:
        case CHAR:
        case BOOL: {
          constantes();
          break;
        }
        default:
          jj_la1[5] = jj_gen;
          ;
      }
    } finally {
      trace_return("constantes_prime");
    }
  }

  final public void declaracao_variaveis() throws ParseException, SemanticException {
    trace_call("declaracao_variaveis");
    try {

      jj_consume_token(VAR);
      acao6();
      variaveis();
      jj_consume_token(END);
      jj_consume_token(PONTOVIRGULA);
    } finally {
      trace_return("declaracao_variaveis");
    }
  }

  final public void variaveis() throws ParseException, SemanticException {
    trace_call("variaveis");
    try {

      tipo();
      jj_consume_token(DOISPONTOS);
      lista_identificadores();
      acao4();
      jj_consume_token(PONTO);
      variaveis_prime();
    } finally {
      trace_return("variaveis");
    }
  }

  final public void variaveis_prime() throws ParseException, SemanticException {
    trace_call("variaveis_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case INT:
        case REAL:
        case CHAR:
        case BOOL: {
          variaveis();
          break;
        }
        default:
          jj_la1[6] = jj_gen;
          ;
      }
    } finally {
      trace_return("variaveis_prime");
    }
  }

  final public void tipo() throws ParseException, SemanticException {
    trace_call("tipo");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case INT: {
          jj_consume_token(INT);
          acao7();
          break;
        }
        case REAL: {
          jj_consume_token(REAL);
          acao8();
          break;
        }
        case CHAR: {
          jj_consume_token(CHAR);
          acao9();
          break;
        }
        case BOOL: {
          jj_consume_token(BOOL);
          acao10();
          break;
        }
        default:
          jj_la1[7] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("tipo");
    }
  }

  final public void lista_identificadores() throws ParseException, SemanticException {
    trace_call("lista_identificadores");
    try {

      identificador();
      //verificar
      acao11(token.image);
      lista_identificadores_prime();
    } finally {
      trace_return("lista_identificadores");
    }
  }

  final public void lista_identificadores_prime() throws ParseException, SemanticException {
    trace_call("lista_identificadores_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case VIRGULA: {
          jj_consume_token(VIRGULA);
          lista_identificadores();
          break;
        }
        default:
          jj_la1[8] = jj_gen;
          ;
      }
    } finally {
      trace_return("lista_identificadores_prime");
    }
  }

  final public void lista_comandos() throws ParseException, SemanticException {
    trace_call("lista_comandos");
    try {

      comando();
      jj_consume_token(PONTO);
      lista_comandos_prime();
    } finally {
      trace_return("lista_comandos");
    }
  }

  final public void lista_comandos_prime() throws ParseException, SemanticException {
    trace_call("lista_comandos_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case GET:
        case PUT:
        case IF:
        case TRUE:
        case FALSE:
        case WHILE:
        case ABRE_PARENTESES:
        case NAO:
        case CONSTANTE_INTEIRA:
        case CONSTANTE_REAL:
        case CONSTANTE_LITERAL:
        case IDENTIFICADOR: {
          lista_comandos();
          break;
        }
        default:
          jj_la1[9] = jj_gen;
          ;
      }
    } finally {
      trace_return("lista_comandos_prime");
    }
  }

  final public void comando() throws ParseException, SemanticException {
    trace_call("comando");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case TRUE:
        case FALSE:
        case ABRE_PARENTESES:
        case NAO:
        case CONSTANTE_INTEIRA:
        case CONSTANTE_REAL:
        case CONSTANTE_LITERAL:
        case IDENTIFICADOR: {
          atribuicao();
          break;
        }
        case GET: {
          entrada();
          break;
        }
        case PUT: {
          saida();
          break;
        }
        case IF: {
          selecao();
          break;
        }
        case WHILE: {
          repeticao();
          break;
        }
        default:
          jj_la1[10] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("comando");
    }
  }

  final public void atribuicao() throws ParseException, SemanticException {
    trace_call("atribuicao");
    try {

      expressao();
      jj_consume_token(ATRIBUICAO);
      identificador();
      //verificar
      acao12(token.image);
    } finally {
      trace_return("atribuicao");
    }
  }

  final public void entrada() throws ParseException, SemanticException {
    trace_call("entrada");
    try {

      jj_consume_token(GET);
      acao13();
      jj_consume_token(ABRE_PARENTESES);
      lista_identificadores();
      jj_consume_token(FECHA_PARENTESES);
    } finally {
      trace_return("entrada");
    }
  }

  final public void saida() throws ParseException, SemanticException {
    trace_call("saida");
    try {

      jj_consume_token(PUT);
      jj_consume_token(ABRE_PARENTESES);
      lista_identificadores_e_ou_constantes();
      jj_consume_token(FECHA_PARENTESES);
    } finally {
      trace_return("saida");
    }
  }

  final public void lista_identificadores_e_ou_constantes() throws ParseException, SemanticException {
    trace_call("lista_identificadores_e_ou_constantes");
    try {

      item();
      acao14();
      lista_identificadores_e_ou_constantes_prime();
    } finally {
      trace_return("lista_identificadores_e_ou_constantes");
    }
  }

  final public void lista_identificadores_e_ou_constantes_prime() throws ParseException, SemanticException {
    trace_call("lista_identificadores_e_ou_constantes_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case VIRGULA: {
          jj_consume_token(VIRGULA);
          lista_identificadores_e_ou_constantes();
          break;
        }
        default:
          jj_la1[11] = jj_gen;
          ;
      }
    } finally {
      trace_return("lista_identificadores_e_ou_constantes_prime");
    }
  }

  final public void item() throws ParseException, SemanticException {
    trace_call("item");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case IDENTIFICADOR: {
          identificador();
          //verificar
          acao15(token.image);
          break;
        }
        case CONSTANTE_INTEIRA: {
          jj_consume_token(CONSTANTE_INTEIRA);
          // verificar
          acao16(tipo);
          break;
        }
        case CONSTANTE_REAL: {
          jj_consume_token(CONSTANTE_REAL);
          acao17(tipo);
          break;
        }
        case CONSTANTE_LITERAL: {
          jj_consume_token(CONSTANTE_LITERAL);
          //verificar
          acao18(token.image);
          break;
        }
        case TRUE: {
          jj_consume_token(TRUE);
          acao19();
          break;
        }
        case FALSE: {
          jj_consume_token(FALSE);
          acao20();
          break;
        }
        default:
          jj_la1[12] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("item");
    }
  }

  final public void selecao() throws ParseException, SemanticException {
    trace_call("selecao");
    try {

      jj_consume_token(IF);
      expressao();
      acao21();
      jj_consume_token(THEN);
      lista_comandos();
      senao();
      jj_consume_token(END);
      jj_consume_token(PONTO);
      acao22();
    } finally {
      trace_return("selecao");
    }
  }

  final public void senao() throws ParseException, SemanticException {
    trace_call("senao");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case ELSE: {
          jj_consume_token(ELSE);
          acao23();
          lista_comandos();
          break;
        }
        default:
          jj_la1[13] = jj_gen;
          ;
      }
    } finally {
      trace_return("senao");
    }
  }

  final public void repeticao() throws ParseException, SemanticException {
    trace_call("repeticao");
    try {

      jj_consume_token(WHILE);
      acao24();
      expressao();
      acao25();
      jj_consume_token(DO);
      lista_comandos();
      jj_consume_token(END);
      jj_consume_token(PONTO);
      acao26();
    } finally {
      trace_return("repeticao");
    }
  }

  final public void expressao() throws ParseException, SemanticException {
    trace_call("expressao");
    try {

      expressao_aritmetica_ou_logica();
      expressao_prime();
    } finally {
      trace_return("expressao");
    }
  }

  final public void expressao_prime() throws ParseException, SemanticException {
    trace_call("expressao_prime");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case IGUAL:
        case DIFERENTE:
        case MENOR:
        case MAIOR:
        case MENOR_IGUAL:
        case MAIOR_IGUAL: {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case IGUAL: {
              jj_consume_token(IGUAL);
              expressao_aritmetica_ou_logica();
              acao27();
              break;
            }
            case DIFERENTE: {
              jj_consume_token(DIFERENTE);
              expressao_aritmetica_ou_logica();
              acao28();
              break;
            }
            case MENOR: {
              jj_consume_token(MENOR);
              expressao_aritmetica_ou_logica();
              acao29();
              break;
            }
            case MAIOR: {
              jj_consume_token(MAIOR);
              expressao_aritmetica_ou_logica();
              acao29();
              break;
            }
            case MENOR_IGUAL: {
              jj_consume_token(MENOR_IGUAL);
              expressao_aritmetica_ou_logica();
              acao30();
              break;
            }
            case MAIOR_IGUAL: {
              jj_consume_token(MAIOR_IGUAL);
              expressao_aritmetica_ou_logica();
              acao31();
              break;
            }
            default:
              jj_la1[14] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          break;
        }
        default:
          jj_la1[15] = jj_gen;
          ;
      }
    } finally {
      trace_return("expressao_prime");
    }
  }

  final public void expressao_aritmetica_ou_logica() throws ParseException, SemanticException {
    trace_call("expressao_aritmetica_ou_logica");
    try {

      termo2();
      menor_prioridade();
    } finally {
      trace_return("expressao_aritmetica_ou_logica");
    }
  }

  final public void menor_prioridade() throws ParseException, SemanticException {
    trace_call("menor_prioridade");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case ADICAO:
        case SUBTRACAO:
        case OU: {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case ADICAO: {
              jj_consume_token(ADICAO);
              termo2();
              acao33();
              menor_prioridade();
              break;
            }
            case SUBTRACAO: {
              jj_consume_token(SUBTRACAO);
              termo2();
              acao34();
              menor_prioridade();
              break;
            }
            case OU: {
              jj_consume_token(OU);
              termo2();
              acao35();
              menor_prioridade();
              break;
            }
            default:
              jj_la1[16] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          break;
        }
        default:
          jj_la1[17] = jj_gen;
          ;
      }
    } finally {
      trace_return("menor_prioridade");
    }
  }

  final public void termo2() throws ParseException, SemanticException {
    trace_call("termo2");
    try {

      termo1();
      media_prioridade();
    } finally {
      trace_return("termo2");
    }
  }

  final public void media_prioridade() throws ParseException, SemanticException {
    trace_call("media_prioridade");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case MULTIPLICACAO:
        case DIVISAO:
        case DIVISAO_INTEIRA:
        case RESTO:
        case E: {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case MULTIPLICACAO: {
              jj_consume_token(MULTIPLICACAO);
              termo1();
              acao36();
              media_prioridade();
              break;
            }
            case DIVISAO: {
              jj_consume_token(DIVISAO);
              termo1();
              acao37();
              media_prioridade();
              break;
            }
            case DIVISAO_INTEIRA: {
              jj_consume_token(DIVISAO_INTEIRA);
              termo1();
              acao38();
              media_prioridade();
              break;
            }
            case RESTO: {
              jj_consume_token(RESTO);
              termo1();
              acao39();
              media_prioridade();
              break;
            }
            case E: {
              jj_consume_token(E);
              termo1();
              acao40();
              media_prioridade();
              break;
            }
            default:
              jj_la1[18] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          break;
        }
        default:
          jj_la1[19] = jj_gen;
          ;
      }
    } finally {
      trace_return("media_prioridade");
    }
  }

  final public void termo1() throws ParseException, SemanticException {
    trace_call("termo1");
    try {

      elemento();
      maior_prioridade();
    } finally {
      trace_return("termo1");
    }
  }

  final public void maior_prioridade() throws ParseException, SemanticException {
    trace_call("maior_prioridade");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case POTENCIA: {
          jj_consume_token(POTENCIA);
          elemento();
          acao41();
          maior_prioridade();
          break;
        }
        default:
          jj_la1[20] = jj_gen;
          ;
      }
    } finally {
      trace_return("maior_prioridade");
    }
  }

  final public void elemento() throws ParseException, SemanticException {
    trace_call("elemento");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case IDENTIFICADOR: {
          identificador();
          //verificar
          acao15(token.image);
          break;
        }
        case CONSTANTE_INTEIRA: {
          jj_consume_token(CONSTANTE_INTEIRA);
          //verificar
          acao16(tipo);
          break;
        }
        case CONSTANTE_REAL: {
          jj_consume_token(CONSTANTE_REAL);
          //verificar
          acao17(tipo);
          break;
        }
        case CONSTANTE_LITERAL: {
          jj_consume_token(CONSTANTE_LITERAL);
          //verificar
          acao18(token.image);
          break;
        }
        case TRUE: {
          jj_consume_token(TRUE);
          acao19();
          break;
        }
        case FALSE: {
          jj_consume_token(FALSE);
          acao20();
          break;
        }
        case ABRE_PARENTESES: {
          jj_consume_token(ABRE_PARENTESES);
          expressao();
          jj_consume_token(FECHA_PARENTESES);
          break;
        }
        case NAO: {
          jj_consume_token(NAO);
          jj_consume_token(ABRE_PARENTESES);
          expressao();
          jj_consume_token(FECHA_PARENTESES);
          acao42();
          break;
        }
        default:
          jj_la1[21] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("elemento");
    }
  }

  final public void identificador() throws ParseException {
    trace_call("identificador");
    try {

      jj_consume_token(IDENTIFICADOR);
    } finally {
      trace_return("identificador");
    }
  }

  final public void valor() throws ParseException {
    trace_call("valor");
    try {

      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case CONSTANTE_INTEIRA: {
          jj_consume_token(CONSTANTE_INTEIRA);
          break;
        }
        case CONSTANTE_REAL: {
          jj_consume_token(CONSTANTE_REAL);
          break;
        }
        case CONSTANTE_LITERAL: {
          jj_consume_token(CONSTANTE_LITERAL);
          break;
        }
        case TRUE: {
          jj_consume_token(TRUE);
          break;
        }
        case FALSE: {
          jj_consume_token(FALSE);
          break;
        }
        default:
          jj_la1[22] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    } finally {
      trace_return("valor");
    }
  }

  /** Generated Token Manager. */
  public LanguageParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[23];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
    jj_la1_init_0();
    jj_la1_init_1();
  }

  private static void jj_la1_init_0() {
    jj_la1_0 = new int[] { 0x0, 0x1800, 0x1800, 0x1800, 0x1800, 0x1e000, 0x1e000, 0x1e000, 0x10000000, 0x3ce0000,
        0x3ce0000, 0x10000000, 0xc00000, 0x200000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2c00000, 0xc00000, };
  }

  private static void jj_la1_init_1() {
    jj_la1_1 = new int[] { 0x200000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2e8000, 0x2e8000, 0x0, 0x2e0000, 0x0,
        0x1f80, 0x1f80, 0x4003, 0x4003, 0x206c, 0x206c, 0x10, 0x2e8000, 0xe0000, };
  }

  {
    enable_tracing();
  }

  /** Constructor with InputStream. */
  public LanguageParser(java.io.InputStream stream) {
    this(stream, null);
  }

  /** Constructor with InputStream and supplied encoding */
  public LanguageParser(java.io.InputStream stream, String encoding) {
    try {
      jj_input_stream = new JavaCharStream(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    token_source = new LanguageParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
    ReInit(stream, null);
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try {
      jj_input_stream.ReInit(stream, encoding, 1, 1);
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  /** Constructor. */
  public LanguageParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new LanguageParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    if (jj_input_stream == null) {
      jj_input_stream = new JavaCharStream(stream, 1, 1);
    } else {
      jj_input_stream.ReInit(stream, 1, 1);
    }
    if (token_source == null) {
      token_source = new LanguageParserTokenManager(jj_input_stream);
    }

    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public LanguageParser(LanguageParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(LanguageParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 23; i++)
      jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null)
      token = token.next;
    else
      token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      trace_token(token, "");
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  /** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null)
      token = token.next;
    else
      token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    trace_token(token, " (in getNextToken)");
    return token;
  }

  /** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null)
        t = t.next;
      else
        t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt = token.next) == null)
      return (jj_ntk = (token.next = token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[59];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 23; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1 << j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1 << j)) != 0) {
            la1tokens[32 + j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 59; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

  /** Trace enabled. */
  final public boolean trace_enabled() {
    return trace_enabled;
  }

  private int trace_indent = 0;

  /** Enable tracing. */
  final public void enable_tracing() {
    trace_enabled = true;
  }

  /** Disable tracing. */
  final public void disable_tracing() {
    trace_enabled = false;
  }

  protected void trace_call(String s) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) {
        System.out.print(" ");
      }
      System.out.println("Call:	" + s);
    }
    trace_indent = trace_indent + 2;
  }

  protected void trace_return(String s) {
    trace_indent = trace_indent - 2;
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) {
        System.out.print(" ");
      }
      System.out.println("Return: " + s);
    }
  }

  protected void trace_token(Token t, String where) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) {
        System.out.print(" ");
      }
      System.out.print("Consumed token: <" + tokenImage[t.kind]);
      if (t.kind != 0 && !tokenImage[t.kind].equals("\"" + t.image + "\"")) {
        System.out.print(": \"" + TokenMgrError.addEscapes(t.image) + "\"");
      }
      System.out.println(" at line " + t.beginLine + " column " + t.beginColumn + ">" + where);
    }
  }

  protected void trace_scan(Token t1, int t2) {
    if (trace_enabled) {
      for (int i = 0; i < trace_indent; i++) {
        System.out.print(" ");
      }
      System.out.print("Visited token: <" + tokenImage[t1.kind]);
      if (t1.kind != 0 && !tokenImage[t1.kind].equals("\"" + t1.image + "\"")) {
        System.out.print(": \"" + TokenMgrError.addEscapes(t1.image) + "\"");
      }
      System.out.println(
          " at line " + t1.beginLine + " column " + t1.beginColumn + ">; Expected token: <" + tokenImage[t2] + ">");
    }
  }

}
