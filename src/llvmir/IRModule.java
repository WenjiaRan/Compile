package llvmir;

import llvmir.value.Function;
import llvmir.value.GlobalVar;
import llvmir.value.Instructions.Instruction;
import utils.IList;

import java.util.HashMap;
import java.util.List;

public class IRModule {
    public static IRModule module = new IRModule();
    public IList<Function,IRModule> functions;
    public List<GlobalVar> globalVars;
    public HashMap<Integer, Instruction> instructions ;

}
