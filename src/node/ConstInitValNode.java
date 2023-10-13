package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstInitValNode {
//    常量初值 ConstInitVal → ConstExp
//| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
//维数组初值
    private ConstExpNode constExpNode;

    private Token leftBraceToken;
    private List<ConstInitValNode> constInitValNodes;
    private List<Token> commaTokens;
    private Token rightBraceToken;

    public ConstInitValNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
    }

    public ConstInitValNode(Token leftBraceToken, List<ConstInitValNode> constInitValNodes, List<Token> commaTokens, Token rightBraceToken) {
        this.leftBraceToken = leftBraceToken;
        this.constInitValNodes = constInitValNodes;
        this.commaTokens = commaTokens;
        this.rightBraceToken = rightBraceToken;
    }

    void print(){
        if (constExpNode!=null){
            constExpNode.print();
        }
        else {
            IOUtils.write(leftBraceToken.toString());
            if(constInitValNodes.size()>0){
                constInitValNodes.get(0).print();
                for(int i=0;i<constInitValNodes.size();i++){
                   IOUtils.write(commaTokens.get(i).toString());
                   constInitValNodes.get(i+1).print();
                }
            }
            IOUtils.write(rightBraceToken.toString());
        }
        IOUtils.write(Parser.nodeType.get(NodeType.ConstInitVal));
    }
}
