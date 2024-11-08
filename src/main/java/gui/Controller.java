package gui;

import classes.AErrorStruct;
import classes.AIntermediateCode;
import classes.LanguageParser;
import classes.LanguageParser.SemanticException;
import classes.LanguageParserConstants;
import classes.ParseException;
import classes.Token;
import util.AlertFactory;
import util.Operation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {
    private EditorFile editorFile = new EditorFile();
    private static boolean hasEditedFile = false;
    private static boolean hasOpenFile = false;
    @FXML
    private Stage stage;

    public CodeArea inputTextArea;
    public TextArea messageTextArea;
    public Label statusBar, lineColLabel;
    // Menu bar items
    public MenuItem saveMenuItem, saveAsMenuItem, exitProgramItem;
    public MenuItem cutMenuItem, copyMenuItem, pasteMenuItem;
    // Menu toolbar buttons
    public Button newBtn, openBtn, saveBtn;
    public Button copyBtn, cutBtn, pasteBtn;
    public Button buildBtn, runBtn;

    @FXML
    private TextArea outputArea;

    @FXML
    private TableView<AIntermediateCode> AIntermediateCodeTable;
    @FXML
    private TableColumn<AIntermediateCode, String> col1;
    @FXML
    private TableColumn<AIntermediateCode, String> col2;
    @FXML
    private TableColumn<AIntermediateCode, String> col3;

    @FXML
    public void initialize() {
        col1.setCellValueFactory(cellData -> cellData.getValue().col1Property());
        col2.setCellValueFactory(cellData -> cellData.getValue().col2Property());
        col3.setCellValueFactory(cellData -> cellData.getValue().col3Property());
    }

    @FXML
    public void openFileDialog(ActionEvent actionEvent) {
        actionEvent.consume();
        if (handleOpenUnsavedFile() != Operation.SUCCESS) {
            return;
        }
        FileChooser filePicker = new FileChooser();
        filePicker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*." + EditorFile.FILE_EXT));
        filePicker.setInitialDirectory(new File(System.getProperty("user.dir")));
        editorFile = new EditorFile(filePicker.showOpenDialog(new Stage()), false);

        if (!editorFile.isFileStatusOK()) {
            Alert alert = AlertFactory.create(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Erro",
                    String.format("Falha ao abrir arquivo: %s", editorFile.getFileStatus()));
            alert.showAndWait();
            return;
        }
        fileContentsToCodeArea();
    }

    public void newFileDialog(ActionEvent event) {
        event.consume();
        if (handleOpenUnsavedFile() != Operation.SUCCESS) {
            return;
        }
        inputTextArea.clear();
        clearMessageArea();
        hasOpenFile = false;
        this.editorFile.setFile(null);
        updateStageTitle();
        setStatusMsg("");
    }

    private void fileContentsToCodeArea() {
        hasEditedFile = false;
        hasOpenFile = true;
        inputTextArea.setWrapText(false);
        try {
            inputTextArea.replaceText(editorFile.getFileContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setStatusMsg(String.format("Arquivo lido com sucesso %s", editorFile.getFilePath().get()));
        updateStageTitle();
        clearMessageArea();
    }

    private void updateStageTitle() {
        String title = "Compilador";
        if (hasOpenFile) {
            title += String.format(" - [%s]", editorFile.getFilePath().get());
        }
        this.stage.setTitle(title);
    }

    @FXML
    public Operation saveFileDialog(ActionEvent actionEvent) {
        Operation op = Operation.FAILURE;
        actionEvent.consume();
        EditorFile.FileStatus status = editorFile.save(inputTextArea.getText());
        if (status == EditorFile.FileStatus.OK) {
            onSaveSuccess();
            op = Operation.SUCCESS;
        } else {
            AlertFactory.create(Alert.AlertType.ERROR, "Erro", "Falha na operacao",
                    String.format("Falha ao salvar arquivo em '%s'", editorFile.getFilePath().get())).show();
        }
        return op;
    }

    @FXML
    private Operation saveAsDialog(ActionEvent actionEvent) {
        actionEvent.consume();
        Operation op = Operation.CANCELED;
        FileChooser filePicker = new FileChooser();
        filePicker.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*." + EditorFile.FILE_EXT));
        filePicker.setInitialDirectory(new File(System.getProperty("user.dir")));
        File newFile = filePicker.showSaveDialog(new Stage());
        EditorFile newED = new EditorFile(newFile, false);
        switch (newED.getFileStatus()) {
            case INVALID_EXTENSION -> {
                AlertFactory.create(
                        Alert.AlertType.ERROR,
                        "Erro",
                        "Extensao invalida",
                        "O nome do arquivo deve usar a extensao '.txt'").show();
                op = Operation.FAILURE;
            }
            case IO_ERROR -> {
                AlertFactory.create(
                        Alert.AlertType.ERROR,
                        "Erro",
                        "Erro de leitura/escrita de arquivo",
                        "Erro ao ler/escrever arquivo").show();
                op = Operation.FAILURE;
            }
            case NO_OPEN_FILE -> {
                AlertFactory.create(
                        Alert.AlertType.INFORMATION,
                        "Info",
                        "Operacao cancelada",
                        "Salvar em um novo arquivo cancelado").show();
                op = Operation.CANCELED;
            }
            case OK -> {
                editorFile.saveAs(inputTextArea.getText(), newFile);
                System.out.println("Salvo com sucesso");
                onSaveSuccess();
                op = Operation.SUCCESS;
            }
        }
        return op;
    }

    public Operation saveAction() {
        if (hasOpenFile && hasEditedFile) {
            return saveFileDialog(new ActionEvent());
        }
        if (!hasOpenFile && hasEditedFile) {
            return saveAsDialog(new ActionEvent());
        }
        return Operation.SUCCESS;
    }

    private void onSaveSuccess() {
        hasOpenFile = true;
        hasEditedFile = false;
        setStatusMsg("Arquivo salvo");
        updateStageTitle();
        disableSaving(true);
    }

    public void disableSaving(boolean b) {
        saveBtn.setDisable(b);
        saveMenuItem.setDisable(b);
    }

    public void setStatusMsg(String msg) {
        statusBar.setText(String.format("Status: %s", msg));
    }

    public void fileContentChanged() {
        disableSaving(false);
        hasEditedFile = true;
    }

    private void registerLineColUpdater() {
        inputTextArea.caretPositionProperty().addListener((observableValue, integer, t1) -> {
            int line = inputTextArea.getCurrentParagraph();
            int col = inputTextArea.getCaretColumn();
            setLineColLabel(line + 1, col + 1);
        });
    }

    private void setLineColLabel(int line, int col) {
        lineColLabel.setText(String.format("Linha/Coluna: %d:%d", line, col));
    }

    public void registerWindowClose() {
        this.stage.setOnCloseRequest(new ExitButtonListener());
    }

    public void setStage(Stage primaryStage) {
        setLineColLabel(1, 1);;
        this.stage = primaryStage;
        inputTextArea.setParagraphGraphicFactory(LineNumberFactory.get(inputTextArea));
        registerWindowClose();
        registerLineColUpdater();
    }

    @FXML
    private Operation handleOpenUnsavedFile() {
        Operation op = Operation.CANCELED;
        Alert alert;
        if (hasEditedFile) {
            alert = AlertFactory.AlertYesNoCancel(Alert.AlertType.CONFIRMATION,
                    "Confirmacao",
                    "Arquivo nao salvo",
                    "Voce editou um arquivo aberto e nao salvou, deseja salva-lo?");
        } else {
            return Operation.SUCCESS;
        }
        Optional<ButtonType> optional = alert.showAndWait();
        if (optional.isEmpty()) {
            return Operation.CANCELED;
        }
        var buttonData = optional.get().getButtonData();
        if (buttonData.equals(ButtonType.YES.getButtonData()) && !hasOpenFile) {
            return saveAsDialog(new ActionEvent());
        }
        if (buttonData.equals(ButtonType.YES.getButtonData())) {
            return saveFileDialog(new ActionEvent());
        }
        if (buttonData.equals(ButtonType.NO.getButtonData())) {
            return Operation.SUCCESS;
        }
        return op;
    }

    private void clearMessageArea() {
        this.messageTextArea.clear();
    }

    public void exitProgram(ActionEvent actionEvent) {
        if (handleOpenUnsavedFile() == Operation.SUCCESS) {
            Platform.exit();
        } else {
            actionEvent.consume();
        }
    }

    public class ExitButtonListener implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent windowEvent) {
            if (handleOpenUnsavedFile() == Operation.SUCCESS) {
                Platform.exit();
            } else {
                windowEvent.consume();
            }
        }
    }

    public void compileProgram(ActionEvent actionEvent) throws SemanticException, ParseException {
        if (this.inputTextArea.getText().length() == 0) {
            return;
        }

        // Realiza a análise léxica
        analisadorLexico();

        // Verifica se há erros léxicos
        if (hasLexicalErrors()) {
            return;
        }

        // Realiza a análise sintática
        analisadorSintatico();

        // Verifica se há erros sintáticos
        if (hasSyntaxErrors()) {
            return;
        }

        // Realiza a análise semântica
        analisarSemantica(this.inputTextArea.getText());

        // Verifica se há erros semânticos
        if (hasSemanticErrors()) {
            return;
        }
        
    }

    private boolean hasSemanticErrors() {
        String messageContent = this.messageTextArea.getText();
        return messageContent.contains("Erro Semantico encontrado");
    }

    private boolean hasSyntaxErrors() {
        String messageContent = this.messageTextArea.getText();
        return messageContent.contains("Erro(s) sintaticos encontrados.");
    }

    private boolean hasLexicalErrors() {
        String messageContent = this.messageTextArea.getText();
        return messageContent.contains("Erro(s) lexicos encontrados.");
    }


    private void analisadorLexico() {
        this.messageTextArea.clear();
        ArrayList<Token> tokens = (ArrayList<Token>) LanguageParser.getTokens(this.inputTextArea.getText());
        int counter = 0;
        for (Token token : tokens) {
            if (token.kind == LanguageParserConstants.SIMBOLO_INVALIDO
                    || token.kind == LanguageParserConstants.CONSTANTE_INTEIRA_INVALIDA
                    || token.kind == LanguageParserConstants.CONSTANTE_REAL_INVALIDA
                    || token.kind == LanguageParserConstants.CONSTANTE_LITERAL_INVALIDA
                    || token.kind == LanguageParserConstants.IDENTIFICADOR_INVALIDO) {
                counter++;
            }
            switch (token.kind) {
                case 54:
                    this.messageTextArea.appendText("\n|Erro: " + token.kind + " - Simbolo Invalido, linha: " + token.beginLine + "- coluna: " + token.endColumn + "|");
                    break;
                case 55:
                    this.messageTextArea.appendText("\n|Erro: " + token.kind + " - Constante Inteira Invalida, linha: " + token.beginLine + "- coluna: " + token.endColumn + "|");
                    break;
                case 56:
                    this.messageTextArea.appendText("\n|Erro: " + token.kind + " - Constante Real Invalida, linha: " + token.beginLine + "- coluna: " + token.endColumn + "|");
                    break;
                case 57:
                    this.messageTextArea.appendText("\n|Erro: " + token.kind + " - Constante Literal Invalida, linha: " + token.beginLine + "- coluna: " + token.endColumn + "|");
                    break;
                case 58:
                    this.messageTextArea.appendText("\n|Erro: " + token.kind + " - Identificador Invalido, linha: " + token.beginLine + "- coluna: " + token.endColumn + "|");
                    break;
            }
        }
        if (counter == 0) {
            this.messageTextArea.appendText("\nNão há erro(s) lexicos.");
            this.messageTextArea.appendText("\n--------------------------");
        } else {
            System.err.println("Erro léxico: ");
            this.messageTextArea.appendText("\nErro(s) lexicos encontrados.");
        }
    }

    private void analisadorSintatico() throws SemanticException {
        ArrayList<AErrorStruct> output = LanguageParser.analisadorSintatico(this.inputTextArea.getText());
        if (output.size() == 0) {
            this.messageTextArea.appendText("\nNão há erro(s) Sintatico\n");
            this.messageTextArea.appendText("--------------------------");
        } else {
            this.messageTextArea.appendText("\nErro(s) sintaticos encontrados.\n");
            clearAIntermediateCodeTable();
            output.forEach(error -> {
                if (error.getError() != null) {
                    this.messageTextArea.appendText("\n" + error.getMsg() + "\n");
                } else {
                    this.messageTextArea.appendText("\n" + error.getMsg() + "\n");
                }
            });
        }
    }

    private void analisarSemantica(String codigo) throws ParseException, SemanticException {
        List<String> semanticErrors = new ArrayList<>();
        List<AIntermediateCode> AIntermediateCodeList = LanguageParser.analisadorSemantico(codigo, semanticErrors);
        clearAIntermediateCodeTable();
    
        if (semanticErrors.isEmpty()) {
            this.messageTextArea.appendText("\nNão há erro(s) Semantico(s)\n");
            updateAIntermediateCodeTable(AIntermediateCodeList);
        } else {
            for (String error : semanticErrors) {
                this.messageTextArea.appendText("\n" + error + "\n");
            }
        }
    }

    // atualiza a tabela de códigos intermediários
    private void updateAIntermediateCodeTable(List<AIntermediateCode> AIntermediateCodeList) {
        AIntermediateCodeTable.getItems().setAll(AIntermediateCodeList);
    }

    // limpa a tabela de códigos intermediários
    public void clearAIntermediateCodeTable() {
        AIntermediateCodeTable.getItems().clear();
    }

    public String copySelection() {
        String selection = inputTextArea.getSelectedText();
        inputTextArea.copy();
        return selection;
    }

    public String cutSelection() {
        String selection = inputTextArea.getSelectedText();
        inputTextArea.cut();
        return selection;
    }

    public void pasteFromClipboard() {
        inputTextArea.paste();
    }
}