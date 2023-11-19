package llvmir.value.Instructions.terminator;

import llvmir.type.Type;
import llvmir.value.BasicBlock;
import llvmir.value.Instructions.Instruction;
import llvmir.value.Instructions.Operator;
import utils.INode;

public class TerminatorInst extends Instruction {
    public TerminatorInst(Type type, Operator op,
                          BasicBlock basicBlock) {
        super(type, op, basicBlock);
    }
}
