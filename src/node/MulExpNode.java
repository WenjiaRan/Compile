package node;
import  frontend.Parser;
import token.Token;
import utils.IOUtils;
import java.util.List;

public class MulExpNode {
    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 消除左递归
    // MulExp → UnaryExp [('*' | '/' | '%') MulExp]
    public UnaryExpNode unaryExpNode;
    public Token operation;
    public MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token operation, MulExpNode mulExpNode) {
        this.unaryExpNode = unaryExpNode;
        this.operation = operation;
        this.mulExpNode = mulExpNode;
    }

    public void print() {
        unaryExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.MulExp));
        if(operation!=null){
            IOUtils.write(operation.toString());
            mulExpNode.print();
        }
    }
}














