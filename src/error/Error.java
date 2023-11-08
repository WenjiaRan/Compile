package error;

import javax.lang.model.type.ErrorType;

public class Error {


    public enum ErrorType {
        a, b, c, d, e, f, g, h, i, j, k, l, m
    }
    public int lineNumber;
    public ErrorType type;

    public Error(int lineNumber, ErrorType type) {
        this.lineNumber = lineNumber;
        this.type = type;
    }
    public  int getLineNumber() {
        return lineNumber;
    }
//    @Override
    public boolean equals(Error o) {
        return lineNumber == o.lineNumber;
    }
    public String toString() {
        return lineNumber+ " " + type.toString()+"\n";
    }
}
