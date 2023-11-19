package llvmir.type;

public class IntegerType extends Type{
    public int bit;
    public static IntegerType i1=new IntegerType(1);
    public static IntegerType i8=new IntegerType(8);
    public static IntegerType i32=new IntegerType(32);

    public IntegerType(int bit) {
        this.bit = bit;
    }
}
