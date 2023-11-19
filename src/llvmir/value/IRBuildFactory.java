package llvmir.value;

import llvmir.type.FunctionType;
import llvmir.type.IntegerType;
import llvmir.type.Type;
import llvmir.value.Instructions.BinaryInst;
import llvmir.value.Instructions.Operator;
import llvmir.value.Instructions.mem.AllocaInst;
import llvmir.value.Instructions.terminator.RetInst;
import llvmir.value.Instructions.terminator.BrInst;
import java.util.List;

public class IRBuildFactory {
    public static IRBuildFactory instance = new IRBuildFactory();

    public IRBuildFactory() {
    }

    public BasicBlock buildBasicBlock(Function parentFunc) {
        return new BasicBlock(parentFunc);
    }
    public void checkBlockEnd(BasicBlock block) {
        Type returnType=((FunctionType)block.node.parent.value.type).returnType;
        if(block.instructions.isEmpty()) {

        }
        else {
            if (block.instructions.end.value instanceof BrInst ||
                    block.instructions.end.value instanceof RetInst) {
                return;
            }
        }
        if (returnType instanceof IntegerType) {
            buildRet(block, ConstInteger.zero);
        }else {
            buildRet(block,null);
        }
    }
    public RetInst buildRet(BasicBlock basicBlock,Value ret){
        RetInst retInst;
        if(ret==null){
            retInst=new RetInst(basicBlock);
        }
        else{
            retInst=new RetInst(basicBlock,ret  );
        }
        retInst.addInstToBlock(basicBlock);
        return retInst;
    }

    public Function buildFunction(String name, Type type,
                                  List<Type> paramTypes) {
        return new Function(name, new FunctionType(type, paramTypes),0);
    }
    public Function buildLibFunction(String name, Type type,
                                     List<Type> paramTypes) {
        return new Function(name,new FunctionType(type,paramTypes),1);
    }

//    public BinaryInst buildBinary(BasicBlock basicBlock,
//                                  Operator op, Value left, Value right) {
//        BinaryInst tmp = new BinaryInst(basicBlock, op, left, right);
//        if (op == Operator.And || op == Operator.Or) {
//            tmp = buildBinary(basicBlock, Operator.Ne, tmp, ConstInteger.zero);
//        }
//        tmp.addInstToBlock(basicBlock);
//        return tmp;
//    }
//    public BinaryInst buildNot(BasicBlock basicBlock, Value value) {
//        return buildBinary(basicBlock, Operator.Eq, value,ConstInteger.zero);
//    }
//    public GlobalVar buildGlobalVar(String name, Type type, int isConst, Value value) {
//        boolean ha=isConst==1?true:false;
//        return new GlobalVar(name, type,  value,isConst);
//    }
//    public AllocaInst buildVar(BasicBlock basicBlock, Value value, boolean isConst, Type allocaType) {
//        AllocaInst tmp = new AllocaInst(basicBlock, isConst, allocaType);
//        tmp.addInstToBlock(basicBlock);
//        if (value != null) {
//            buildStore(basicBlock, tmp, value);
//        }
//        return tmp;
//    }

    public ConstInteger getConstInt(int value) {
        return new ConstInteger(value);
    }
}














