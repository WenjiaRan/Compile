package llvmir.value;

import llvmir.type.IntegerType;
import llvmir.type.Type;

public class ConstInteger extends Const {
    public int value;
    public static ConstInteger zero = new ConstInteger(0);

    public ConstInteger( int value) {
        super(String.valueOf(value), IntegerType.i32);
        this.value = value;
    }
}