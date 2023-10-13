package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;

public class BlockNode {
    public List<BlockItemNode> blockItemNodes = null;
    public Token leftBracket = null;
    public Token rightBracket = null;

    public BlockNode(Token leftBracket, List<BlockItemNode> blockItemNodes, Token rightBracket) {
        this.blockItemNodes = blockItemNodes;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
    }

    public void print() {
        IOUtils.write(leftBracket.toString());

        int index = 0;
        while (index < blockItemNodes.size()) {
            blockItemNodes.get(index).print();
            index++;
        }

        IOUtils.write(rightBracket.toString());
        IOUtils.write(Parser.nodeType.get(NodeType.Block));
    }
}














