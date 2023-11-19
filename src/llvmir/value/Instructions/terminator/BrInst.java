package llvmir.value.Instructions.terminator;

import llvmir.type.Type;
import llvmir.value.BasicBlock;
import llvmir.value.Instructions.Operator;

public class BrInst extends TerminatorInst {
    public BrInst(Type type, Operator op, BasicBlock basicBlock) {
        super(type, op, basicBlock);
    }
}
