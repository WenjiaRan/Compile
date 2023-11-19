package llvmir.value;

import llvmir.type.Type;

public class Argument extends Value {
    public int index;

    public Argument(String name, Type type, int index) {
        super(name, type);
        this.index = index;
    }
    public Argument(Type type, int index, int isLibraryFunction) {
        super(isLibraryFunction ==1? "" : "%" + reg_num++, type);
        this.index = index;
    }
}
