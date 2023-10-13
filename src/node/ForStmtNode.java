package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

public class ForStmtNode {
    // ForStmt → LVal '=' Exp // 存在即可
    public LValNode lValNode;
    public Token assginToken;
    public ExpNode expNode;

    public ForStmtNode(LValNode lValNode, Token assginToken, ExpNode expNode) {
        this.lValNode = lValNode;
        this.assginToken = assginToken;
        this.expNode = expNode;
    }

    public void print(){
        lValNode.print();
        IOUtils.write(assginToken.toString());
        expNode.print();
        IOUtils.write(Parser.nodeType.get(NodeType.ForStmt));
    }
}
