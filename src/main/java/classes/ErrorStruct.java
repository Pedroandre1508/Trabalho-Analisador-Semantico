package classes;

public class ErrorStruct {
    private ParseException error = null;
    private String msg = null;
    public ErrorStruct(String msg, ParseException error){
        this.error = error;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public ParseException getError() {
        return error;
    }

    public String expected(){
        String expectedMsg = "";
        for (int i=0; i < this.error.expectedTokenSequences.length; i++){
            for (int j=0; j < this.error.expectedTokenSequences[i].length; j++){
                expectedMsg += LanguageParserConstants.tokenImage[this.error.expectedTokenSequences[i][j]] + ", ";
            }
        }
        return expectedMsg;
    }
}
