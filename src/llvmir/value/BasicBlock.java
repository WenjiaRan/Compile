package llvmir.value;

import llvmir.value.Instructions.Instruction;
import utils.IList;
import utils.INode;
import llvmir.type.LabelType;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Value {
    //需要存储基本块内的指令列表、
    // 基本块的前驱（哪些基本块能跳转到这个基本块）后继（这个基本块能跳转到哪些基本块）
    // 这个 BasicBlock 所属的 Function
    public INode<BasicBlock,Function> node;
    //该basicblock的指令列表
    public IList<Instruction,BasicBlock> instructions;
    public List<BasicBlock> predecessors;
    public List<BasicBlock> successors;

    public BasicBlock(Function function) {
        super(";<label>:" + reg_num++, new LabelType());
        this.instructions = new IList<>(this);
        this.node = new INode<>(this);
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.node.insertAtEnd(function.list);
    }
//    public void refreshReg() {
//        for (INode<Instruction, BasicBlock> inode : this.instructions) {
//            Instruction inst = inode.value;
//            if (!(inst instanceof StoreInst || inst instanceof BrInst || inst instanceof RetInst ||
//                    (inst instanceof CallInst && ((FunctionType) inst.getOperands().get(0).getType()).getReturnType() instanceof VoidType))) {
//                inst.setName("%" + REG_NUMBER++);
//            }
//        }
//    }
}
