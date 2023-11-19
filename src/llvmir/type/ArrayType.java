package llvmir.type;

public class ArrayType extends Type{
    public Type elementType;
    public Integer length;

    public ArrayType(Type elementType, Integer length) {
        this.elementType = elementType;
        this.length = length;
    }
}
