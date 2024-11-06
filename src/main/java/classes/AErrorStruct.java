package classes;

public class AErrorStruct {

    private ParseException error = null;
    private Token lexicalError;
    private String msg = null;
    private String expectedMsg;

    public AErrorStruct(String msg, ParseException error){
        this.error = error;
        this.msg = msg;
        this.expectedMsg = "";
    }

    public AErrorStruct(String msg, Token lexicalError) {
        this.msg = msg;
        this.lexicalError = lexicalError;
        this.expectedMsg = "";
    }

    public String getMsg() {
        return msg;
    }

    public ParseException getError() {
        return error;
    }

    public Token getLexicalError() {
        return lexicalError;
    }

    public void setExpected(int[][] expectedTokenSequences, String[] tokenImage) {
        for (int i = 0; i < expectedTokenSequences.length; i++) {
            for (int j = 0; j < expectedTokenSequences[i].length; j++) {
                this.expectedMsg += tokenImage[expectedTokenSequences[i][j]] + ", ";
            }
        }
    }

    public String expected() {
        return expectedMsg;
    }
}
