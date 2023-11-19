package llvmir.type;

public class PointerType extends Type {
    public Type targetType;

    public PointerType(Type targetType) {
        this.targetType = targetType;
    }
}
