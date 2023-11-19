package llvmir.value;

import llvmir.IRModule;
import llvmir.type.FunctionType;
import llvmir.type.Type;
import utils.IList;
import utils.INode;

import java.util.ArrayList;
import java.util.List;

public class Function extends Value {
    //函数内的基本块列表、函数的参数列表、
    // 函数的前驱（哪些函数调用这个函数）后继（这个函数调用哪些函数）、
    // 以及是否是库函数
    public IList<BasicBlock,Function> list;
    public INode<Function, IRModule> node;
    public List<Function> pre;
    public List<Argument> arguments;
    public List<Function> successors;
    public int isLibFunc;

    public Function(String name, Type type, int isLibFunc) {
        super(name, type);
        this.isLibFunc = isLibFunc;
        reg_num=0;
        this.list = new IList<>(this);
        this.node=new INode<>(this);
        this.arguments = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.pre=new ArrayList<>();
        for (Type t : ((FunctionType) type).parametersType) {
            arguments.add(new Argument
                    (t, ((FunctionType) type).parametersType.indexOf(t), isLibFunc));
        }
        this.node.insertAtEnd(IRModule.module.functions);
    }
    public void refreshArgReg() {
        for (Argument arg : arguments) {
            arg.name="%" + reg_num++;
        }
    }
}
