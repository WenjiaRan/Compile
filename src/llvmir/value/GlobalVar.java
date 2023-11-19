package llvmir.value;

import llvmir.IRModule;
import llvmir.type.PointerType;
import llvmir.type.Type;

import java.util.List;

public class GlobalVar extends User{
    public Value value;
    public int isConst;

    public GlobalVar(String name, Type type,  Value value, int isConst) {
        super("@"+name, new PointerType(type));
        this.value = value;
        this.isConst = isConst;
        IRModule.module.globalVars.add(this);
    }
}
