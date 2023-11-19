package llvmir.value.Instructions.terminator;

import llvmir.type.Type;
import llvmir.type.VoidType;
import llvmir.value.BasicBlock;
import llvmir.value.Instructions.Operator;
import llvmir.value.Value;

public class RetInst extends TerminatorInst {

    public RetInst(BasicBlock basicBlock) {
        super(VoidType.voidType, Operator.Ret, basicBlock);
    }

    public RetInst(BasicBlock basicBlock, Value ret) {
        super(ret.type, Operator.Ret, basicBlock);
        this.addOperand(ret);
    }
    @Override
    public String toString() {
        return switch (ops.size()) {
            case 1 -> "ret " + ops.get(0).type + " " + ops.get(0).name;
            default -> "ret void ";
        };
    }

}
