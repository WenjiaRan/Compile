package symbol;

import java.util.GregorianCalendar;
import java.util.List;

public class ArraySymbol extends Symbol{
    public String name;
    public boolean isConst; // 是否是常量
    public int dimension; // 0 变量，1 数组，2 二维数组
    public int size;// TODO: 二维变量的形参, 或许用得到?
    //在这里增加变量!!!
    public String regiName;
    public String value;
    public List<Integer> value1d;
    public List<List<Integer>> value2d;

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
    public ArraySymbol(String name, boolean isConst, int dimension, String regiName, Object value) {
        this.name = name;
        this.isConst = isConst;
        this.dimension = dimension;
        this.regiName = regiName;

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            if (!list.isEmpty() && list.get(0) instanceof Integer) {
                this.value1d = (List<Integer>) value;
            } else if (!list.isEmpty() && list.get(0) instanceof List) {
                this.value2d = (List<List<Integer>>) value;
            }
        }
    }
}
