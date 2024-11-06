package classes;

import java.util.HashSet;
import java.util.Iterator;

public class ARecoverySet extends HashSet {
    public ARecoverySet() {
        super();
    }

    public ARecoverySet(int t){
        this.add(t);
    }

    public boolean contains(int t){
        return super.contains(t);
    }

    public ARecoverySet union(ARecoverySet s){
        ARecoverySet t = null;
        if (s != null){
            t = (ARecoverySet) this.clone();
            this.addAll(s);
        }
        return t;
    }

    public ARecoverySet remove(int n){
        ARecoverySet t = (ARecoverySet) this.clone();
        t.remove(n);
        return t;
    }

    public String toString(){
        Iterator iter = this.iterator();
        String s = "";
        int k;

        while (iter.hasNext()){
            k = (int) iter.next();
            s += LanguageParser.im(k) + " ";
        }
        return s;

    }

}
