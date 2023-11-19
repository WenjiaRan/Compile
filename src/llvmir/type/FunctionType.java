package llvmir.type;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends Type {
    public Type returnType;
    public List<Type> parametersType;

    public FunctionType(Type returnType, List<Type> parametersType) {
        this.returnType = returnType;
        this.parametersType = parametersType;
        arrayTypeNoLength();
    }

    //TODO???
    private void arrayTypeNoLength() {
        List<Integer> target = new ArrayList<>();
        for (Type type : parametersType) {
            if (type instanceof ArrayType) {
                if (((ArrayType) type).length == -1) {
                    target.add(parametersType.indexOf(type));
                }
            }
        }
        for (int index : target) {
            parametersType.set(index, new PointerType(((ArrayType) parametersType.get(index)).elementType));
        }
    }
}
