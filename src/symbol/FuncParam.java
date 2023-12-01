package symbol;

public class FuncParam {
    public String name;//参数名
    public int dimension;//参数维度
    public int size;//二维数组的形参int y[][2], 需要记录下2

    public FuncParam(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public FuncParam(String name, int dimension, int size) {
        this.name = name;
        this.dimension = dimension;
        this.size = size;
    }
}

