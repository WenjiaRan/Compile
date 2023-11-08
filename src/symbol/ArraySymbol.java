package symbol;

public class ArraySymbol extends Symbol{
    public String name;
    public boolean isConst; // 是否是常量
    public int dimension; // 0 变量，1 数组，2 二维数组

    public ArraySymbol(String name, boolean isConst, int dimension) {
        this.name = name;
        this.isConst = isConst;
        this.dimension = dimension;
    }
}
