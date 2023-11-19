package llvmir.value.Instructions;

import llvmir.type.IntegerType;
import llvmir.type.VoidType;
import llvmir.value.BasicBlock;
import llvmir.value.IRBuildFactory;
import llvmir.value.Value;

public class BinaryInst  {//extends Instruction
//    public BinaryInst(BasicBlock basicBlock, Operator op, Value left, Value right) {
//        super(VoidType.voidType, op, basicBlock);
//        // todo
//        boolean isLeftI1 = left.type instanceof IntegerType && ((IntegerType) left.type).bit==1;
//        boolean isRightI1 = right.type instanceof IntegerType && ((IntegerType) right.type).bit==1;
//        boolean isLeftI32 = left.type instanceof IntegerType && ((IntegerType) left.type).bit==32;
//        boolean isRightI32 = right.type instanceof IntegerType && ((IntegerType) right.type).bit==32;
//        if (isLeftI1 && isRightI32) {
//            addOperand(IRBuildFactory.instance.buildZext(left, basicBlock));
//            addOperand(right);
//        } else if (isLeftI32 && isRightI1) {
//
//            addOperand(left);
//            addOperand( IRBuildFactory.instance.buildZext(right, basicBlock));
//        } else {
//            addOperand(left);
//            addOperand(right);
//        }
//        this.type=this.ops.get(0).type);
//        if (isCond()) {
//            this.type=IntegerType.i1;
//        }
//        this.name="%" + reg_num++;
//    }
//    public boolean isAdd() {
//        return this.op == Operator.Add;
//    }
//
//    public boolean isSub() {
//        return this.op == Operator.Sub;
//    }
//
//    public boolean isMul() {
//        return this.op == Operator.Mul;
//    }
//
//    public boolean isDiv() {
//        return this.op == Operator.Div;
//    }
//
//    public boolean isMod() {
//        return this.op == Operator.Mod;
//    }
//
//    public boolean isAnd() {
//        return this.op == Operator.And;
//    }
//
//    public boolean isOr() {
//        return this.op == Operator.Or;
//    }
//
//    public boolean isLt() {
//        return this.op == Operator.Lt;
//    }
//
//    public boolean isLe() {
//        return this.op == Operator.Le;
//    }
//
//    public boolean isGe() {
//        return this.op == Operator.Ge;
//    }
//
//    public boolean isGt() {
//        return this.op == Operator.Gt;
//    }
//
//    public boolean isEq() {
//        return this.op == Operator.Eq;
//    }
//
//    public boolean isNe() {
//        return this.op == Operator.Ne;
//    }
//
//    public boolean isCond() {
//        return this.isLt() || this.isLe() || this.isGe() || this.isGt() || this.isEq() || this.isNe();
//    }
//
//    public boolean isNot() {
//        return this.op == Operator.Not;
//    }

}
