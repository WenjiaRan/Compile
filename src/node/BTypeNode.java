package node;

import token.Token;
import utils.IOUtils;

public class BTypeNode {
    // 基本类型 BType → 'int'
    private Token bTypeToken;

    public BTypeNode(Token bTypeToken) {
        this.bTypeToken = bTypeToken;
    }

    public void print(){
        IOUtils.write(bTypeToken.toString());
    }
}
