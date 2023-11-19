package llvmir.value;

import llvmir.type.Type;

public class Const extends Value {
    public Const(String name, Type type) {
        super(name, type);
    }
}
