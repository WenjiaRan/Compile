package node;
import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;
public class AddExpNode {
    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    // AddExp → MulExp AddExp'
    // AddExp' ->('+' | '−') MulExp AddExp' | E
    // AddExp → MulExp AddExp' -> MulExp ('+' | '−')  AddExp | MulExp
    // AddExp → MulExp[('+' | '−')  AddExp] 左递归变为右递归!
    // 没有回溯
    private MulExpNode mulExpNode;
    private Token operation;
    private AddExpNode addExpNode;

    public AddExpNode(MulExpNode mulExpNode, Token operation, AddExpNode addExpNode) {
        this.mulExpNode = mulExpNode;
        this.operation = operation;
        this.addExpNode = addExpNode;
    }

    public void print() {
        mulExpNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.AddExp));
        if (operation != null) {
            IOUtils.write(operation.toString());
            addExpNode.print();
        }
    }
}
