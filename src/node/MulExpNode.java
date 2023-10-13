package node;
import  frontend.Parser;
import token.Token;
import utils.IOUtils;
import java.util.List;

public class MulExpNode {
    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 消除左递归
    // MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
    public List<UnaryExpNode> unaryExpNodes;
    public List<Token> operations;

    public MulExpNode(List<UnaryExpNode> unaryExpNodes,List<Token> operations) {
        this.operations = operations;
        this.unaryExpNodes = unaryExpNodes;
    }

    public void print() {
        for (int i=0;i< unaryExpNodes.size();i++) {
            unaryExpNodes.get(i).print();
            if(i<operations.size()) {
                IOUtils.write(operations.get(i).toString());
            }
        }
        IOUtils.write(Parser.nodeType.get(NodeType.MulExp));
    }
}














