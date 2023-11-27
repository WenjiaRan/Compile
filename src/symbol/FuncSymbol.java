package symbol;

import java.util.List;

public class FuncSymbol  extends Symbol { // 函数符号
    public static enum ReturnType {
        VOID,
        INT,
        FOR,
        IF,
        ELSE,
    }
    public String name;
    public ReturnType type; // 函数返回类型，有VOID和INT两种情况
    public List<FuncParam> funcParams; // 函数参数表

    public FuncSymbol(String name, ReturnType type, List<FuncParam> funcParams) {
        this.name = name;
        this.type = type;
        this.funcParams = funcParams;
    }
}
