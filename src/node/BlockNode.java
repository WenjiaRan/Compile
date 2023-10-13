package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class BlockNode {
    // Block → '{' { BlockItem } '}'
    private Token leftBracket;
    private List<BlockItemNode> blockItemNodes;
    private Token rightBracket;

    public BlockNode(Token leftBracket, List<BlockItemNode> blockItemNodes, Token rightBracket) {
        this.leftBracket = leftBracket;
        this.blockItemNodes = blockItemNodes;
        this.rightBracket = rightBracket;
    }

    public void print(){
        IOUtils.write(leftBracket.toString());
        for(BlockItemNode node : blockItemNodes){
            node.print();
        }
        IOUtils.write(rightBracket.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.Block));
    }













}
