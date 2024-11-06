package classes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AIntermediateCode {
    private final StringProperty col1;
    private final StringProperty col2;
    private final StringProperty col3;

    public AIntermediateCode(String col1, String col2, String col3) {
        this.col1 = new SimpleStringProperty(col1);
        this.col2 = new SimpleStringProperty(col2);
        this.col3 = new SimpleStringProperty(col3);
    }

    public String getCol1() {
        return col1.get();
    }

    public StringProperty col1Property() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1.set(col1);
    }

    public String getCol2() {
        return col2.get();
    }

    public StringProperty col2Property() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2.set(col2);
    }

    public String getCol3() {
        return col3.get();
    }

    public StringProperty col3Property() {
        return col3;
    }

    public void setCol3(String col3) {
        this.col3.set(col3);
    }
}