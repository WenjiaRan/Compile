package llvmir.type;

public class LabelType extends Type {
    public int handle;
    public static int handler=0;

    public LabelType() {
        handler+=1;
        this.handle = handler;
    }
}
