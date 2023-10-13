package node;
import  frontend.Parser;
import token.Token;
import utils.IOUtils;
import java.util.List;
public class UnaryExpNode {
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp
    // 无左递归
    // PrimaryExp → '(' Exp ')' | LVal | Number
    // LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    // Number → IntConst 数字
    // UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中


    // First(PrimaryExp) = {'(',Ident, Number}
    // First(Ident) = {Ident}
    // First(UnaryOp) ={ '+', '-', '!'}
    // 有回溯
    // UnaryExp → Number |'(' Exp ')' | Ident  ( '(' [FuncRParams] ')'| {'[' Exp ']'})| UnaryOp UnaryExp
    public PrimaryExpNode primaryExpNode = null;
    public Token ident = null;
    public Token leftParentToken = null;
    public FuncRParamsNode funcRParamsNode = null;
    public Token rightParentToken = null;
    public UnaryOpNode unaryOpNode = null;
    public UnaryExpNode unaryExpNode = null;

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }

    public UnaryExpNode(Token ident, Token leftParentToken, FuncRParamsNode funcRParamsNode, Token rightParentToken) {
        this.ident = ident;
        this.leftParentToken = leftParentToken;
        this.funcRParamsNode = funcRParamsNode;
        this.rightParentToken = rightParentToken;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    void print() {
        if (primaryExpNode!=null){
            primaryExpNode.print();
        }
        else if (ident!=null){
            IOUtils.write(ident.toString());
            IOUtils.write(leftParentToken.toString());
            if (funcRParamsNode != null) {
                funcRParamsNode.print();
            }
            IOUtils.write(rightParentToken.toString());
        }
        else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        IOUtils.write(Parser.nodeType.get(NodeType.UnaryExp));
    }
}
