package node;

import token.Token;
import utils.IOUtils;

import java.util.List;

public class ConstDefNode {
    //常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
    //数组、二维数组共三种情况
    private Token identToken;
    private List<Token> leftBracketsTokens;
    private List<ConstExpNode> constExpNodes;
    private List<Token> rightBracketsTokens;
    private Token equalToken;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token identToken, List<Token> leftBracketsTokens, List<ConstExpNode> constExpNodes, List<Token> rightBracketsTokens, Token equalToken, ConstInitValNode constInitValNode) {
        this.identToken = identToken;
        this.leftBracketsTokens = leftBracketsTokens;
        this.constExpNodes = constExpNodes;
        this.rightBracketsTokens = rightBracketsTokens;
        this.equalToken = equalToken;
        this.constInitValNode = constInitValNode;
    }

    void print(){
        IOUtils.write(identToken.toString());
        for(int i=0;i<leftBracketsTokens.size();i++){
            IOUtils.write(leftBracketsTokens.get(i).toString());
            ConstExpNode.print();

        }
    }












}
