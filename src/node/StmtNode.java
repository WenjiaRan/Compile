package node;

import frontend.Parser;
import token.Token;
import utils.IOUtils;

import java.util.List;



public class StmtNode {
    // Stmt → LVal '=' Exp ';'
    // | [Exp] ';'
    // | Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public enum StmtType {
        LValAssignExp,Exp,Block,If,For,Break,Continue,Return,LValGetint,Printf
    }
    public StmtType stmtType;
    public LValNode lValNode;
    public Token assginToken;
    public ExpNode expNode;
    public List<ExpNode> expNodes;
    public Token semicolonToken;

    public BlockNode blockNode;

    public Token ifToken;
    public Token leftParenToken;
    public CondNode condNode;
    public Token rightParenToken;
    public List<StmtNode> stmtNodes;
    public Token elseToken;

    public Token forToken;
    public List<ForStmtNode> forStmtNodes;
    public List<Token> semicolonTokens;

    public Token breakToken;

    public Token continueToken;

    public Token returnToken;

    public Token getintToken;

    public Token printfToken;
    public Token formatString;
    public Token commaToken;
    public List<Token> commaTokens;



    public StmtNode(StmtType stmtType, List<ExpNode> expNodes, Token semicolonToken, Token leftParenToken, Token rightParenToken, Token printfToken, Token formatString, List<Token> commaTokens) {
        this.stmtType = stmtType;
        this.expNodes = expNodes;
        this.semicolonToken = semicolonToken;
        this.leftParenToken = leftParenToken;
        this.rightParenToken = rightParenToken;
        this.printfToken = printfToken;
        this.formatString = formatString;
        this.commaTokens=commaTokens;
    }

    public StmtNode(StmtType stmtType, LValNode lValNode, Token assginToken, ExpNode expNode, Token semicolonToken) {
        this.stmtType = stmtType;
        this.lValNode = lValNode;
        this.assginToken = assginToken;
        this.expNode = expNode;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType stmtType, ExpNode expNode) {
        this.stmtType = stmtType;
        this.expNode = expNode;
    }

    public StmtNode(StmtType stmtType, BlockNode blockNode) {
        this.stmtType = stmtType;
        this.blockNode = blockNode;
    }

    public StmtNode(StmtType stmtType, Token ifToken, Token leftParenToken, CondNode condNode, Token rightParenToken, List<StmtNode> stmtNodes, Token elseToken) {
        this.stmtType = stmtType;
        this.ifToken = ifToken;
        this.leftParenToken = leftParenToken;
        this.condNode = condNode;
        this.rightParenToken = rightParenToken;
        this.stmtNodes = stmtNodes;
        this.elseToken = elseToken;
    }

    public StmtNode(StmtType stmtType, Token leftParenToken, CondNode condNode, Token rightParenToken, Token forToken, List<ForStmtNode> forStmtNodes, List<Token> semicolonTokens, List<StmtNode> stmtNodes) {
        this.stmtType = stmtType;
        this.leftParenToken = leftParenToken;
        this.condNode = condNode;
        this.rightParenToken = rightParenToken;
        this.forToken = forToken;
        this.forStmtNodes = forStmtNodes;
        this.semicolonTokens = semicolonTokens;
        this.stmtNodes = stmtNodes;
    }

    public StmtNode(StmtType stmtType, Token breakToken, Token semicolonToken) {
        this.stmtType = stmtType;
        this.breakToken = breakToken;
        this.semicolonToken = semicolonToken;
    }

    public StmtNode(StmtType stmtType, ExpNode expNode, Token semicolonToken, Token returnToken) {
        this.stmtType = stmtType;
        this.expNode = expNode;
        this.semicolonToken = semicolonToken;
        this.returnToken = returnToken;
    }

    public StmtNode(StmtType stmtType, LValNode lValNode, Token assginToken, Token semicolonToken, Token leftParenToken, Token rightParenToken, Token getintToken) {
        this.stmtType = stmtType;
        this.lValNode = lValNode;
        this.assginToken = assginToken;
        this.semicolonToken = semicolonToken;
        this.leftParenToken = leftParenToken;
        this.rightParenToken = rightParenToken;
        this.getintToken = getintToken;
    }

    public void print(){
        switch (stmtType) {
            case Block:
                printBlock();
                break;
            case Break:
                printBreak();
                break;
            case Continue:
                printContinue();
                break;
            case Exp:
                printExp();
                break;
            case For:
                printFor();
                break;
            case If:
                printIf();
                break;
            case LValAssignExp:
                printLValAssignExp();
                break;
            case LValGetint:
                printLValGetint();
                break;
            case Printf:
                printPrintf();
                break;
            case Return:
                printReturn();
                break;
        }
        IOUtils.write(Parser.nodeType.get(NodeType.Stmt));
    }


    public void printLValAssignExp() {
        lValNode.print();
        IOUtils.write(assginToken.toString());
        expNode.print();
        IOUtils.write(semicolonToken.toString());
    }

    public void printExp() {
        if (expNode!=null) {
            expNode.print();
        }
        IOUtils.write(semicolonToken.toString());
    }

    public void printBlock() {
        blockNode.print();
    }

    public void printIf() {
        IOUtils.write(ifToken.toString());
        IOUtils.write(leftParenToken.toString());
        condNode.print();
        IOUtils.write(rightParenToken.toString());
        stmtNodes.get(0).print();
        if (elseToken!=null){
            IOUtils.write(elseToken.toString());
            stmtNodes.get(1).print();
        }
    }

    public void printFor() {
        IOUtils.write(forToken.toString());
        IOUtils.write(leftParenToken.toString());
        if (forStmtNodes.size()>0){
            forStmtNodes.get(0).print();
        }
        IOUtils.write(semicolonTokens.get(0).toString());
        if (condNode!=null){
            condNode.print();
        }
        IOUtils.write(semicolonTokens.get(1).toString());
        if (forStmtNodes.size()>1){
            forStmtNodes.get(1).print();
        }
        IOUtils.write(rightParenToken.toString());
        stmtNodes.get(0).print();
    }

    public void printBreak() {
        IOUtils.write(breakToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    public void printContinue() {
        IOUtils.write(continueToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    public void printReturn() {
        IOUtils.write(returnToken.toString());
        if (expNode!=null){
            expNode.print();
        }
        IOUtils.write(semicolonToken.toString());
    }

    public void printLValGetint() {
        lValNode.print();
        IOUtils.write(assginToken.toString());
        IOUtils.write(getintToken.toString());
        IOUtils.write(leftParenToken.toString());
        IOUtils.write(rightParenToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    public void printPrintf() {
        IOUtils.write(printfToken.toString());
        IOUtils.write(leftParenToken.toString());
        IOUtils.write(formatString.toString());
        for(int i=0;i<commaTokens.size();i++) {
            IOUtils.write(commaTokens.get(i).toString());
            expNodes.get(i).print();
        }
        IOUtils.write(rightParenToken.toString());
        IOUtils.write(semicolonToken.toString());
    }
}




















