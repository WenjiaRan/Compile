package llvmir.value.Instructions;
import llvmir.type.Type;
import llvmir.value.BasicBlock;
import llvmir.value.Instructions.terminator.BrInst;
import llvmir.value.Instructions.terminator.RetInst;
import llvmir.value.User;
import utils.INode;

public class Instruction extends User {
    public Operator op;
    public INode<Instruction, BasicBlock> node;
    public int handle;
    public static int handler=0;

    public Instruction( Type type, Operator op,  BasicBlock basicBlock) {
        super("", type);
        this.op = op;
        handler+=1;
        this.handle = handler;
        this.module.instructions.put(handle,this);
    }

    public void addInstToBlock(BasicBlock basicBlock) {
        if (basicBlock.instructions.end == null ||
                (!(basicBlock.instructions.end.value instanceof BrInst) &&
                        !(basicBlock.instructions.end.value instanceof RetInst))) {
            this.node.insertAtEnd(basicBlock.instructions);
        } else {
            this.removeUseFromOperands();
        }
    }

    public void addInstToBlockBegin(BasicBlock basicBlock) {
        this.node.insertAtBegin(basicBlock.instructions);
    }
}
