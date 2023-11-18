package symbol;

import java.util.GregorianCalendar;

public class ArraySymbol extends Symbol{
    public String name;
    public boolean isConst; // 是否是常量
    public int dimension; // 0 变量，1 数组，2 二维数组
    //在这里增加变量!!!
    public String regiName;
    public String value;
    // TODO: 还有数组, public int[] blabla
    public ArraySymbol(String name, boolean isConst, int dimension) {
        this.name = name;
        this.isConst = isConst;
        this.dimension = dimension;
    }

    public ArraySymbol(String name, boolean isConst, int dimension, String regiName, String value) {
        this.name = name;
        this.isConst = isConst;
        this.dimension = dimension;
        this.regiName = regiName;
        this.value = value;
    }
}
