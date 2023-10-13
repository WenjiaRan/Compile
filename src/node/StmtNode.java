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
    private StmtType stmtType;
    private LValNode lValNode;
    private Token assginToken;
    private ExpNode expNode;
    private List<ExpNode> expNodes;
    private Token semicolonToken;

    private BlockNode blockNode;

    private Token ifToken;
    private Token leftParenToken;
    private CondNode condNode;
    private Token rightParenToken;
    private List<StmtNode> stmtNodes;
    private Token elseToken;

    private Token forToken;
    private List<ForStmtNode> forStmtNodes;
    private List<Token> semicolonTokens;

    private Token breakToken;

    private Token continueToken;

    private Token returnToken;

    private Token getintToken;

    private Token printfToken;
    private FormatStringNode formatString;
    private Token commaToken;
    private List<Token> commaTokens;



    public StmtNode(StmtType stmtType, List<ExpNode> expNodes, Token semicolonToken, Token leftParenToken, Token rightParenToken, Token printfToken, FormatStringNode formatString, List<Token> commaTokens) {
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
            case LValAssignExp:
                printLValAssignExp();
                break;
            case Exp:
                printExp();
                break;
            case Block:
                printBlock();
                break;
            case If:
                printIf();
                break;
            case For:
                printFor();
                break;
            case Break:
                printBreak();
                break;
            case Continue:
                printContinue();
                break;
            case Return:
                printReturn();
                break;
            case LValGetint:
                printLValGetint();
                break;
            case Printf:
                printPrintf();
                break;
        }
        IOUtils.write(Parser.nodeType.get(NodeType.Stmt));
    }

    private void printLValAssignExp() {
        lValNode.print();
        IOUtils.write(assginToken.toString());
        expNode.print();
        IOUtils.write(semicolonToken.toString());
    }

    private void printExp() {
        if (expNode!=null) {
            expNode.print();
        }
        IOUtils.write(semicolonToken.toString());
    }

    private void printBlock() {
        blockNode.print();
    }

    private void printIf() {
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

    private void printFor() {
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

    private void printBreak() {
        IOUtils.write(breakToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    private void printContinue() {
        IOUtils.write(continueToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    private void printReturn() {
        IOUtils.write(returnToken.toString());
        if (expNode!=null){
            expNode.print();
        }
        IOUtils.write(semicolonToken.toString());
    }

    private void printLValGetint() {
        lValNode.print();
        IOUtils.write(assginToken.toString());
        IOUtils.write(getintToken.toString());
        IOUtils.write(leftParenToken.toString());
        IOUtils.write(rightParenToken.toString());
        IOUtils.write(semicolonToken.toString());
    }

    private void printPrintf() {
        IOUtils.write(printfToken.toString());
        IOUtils.write(leftParenToken.toString());
        formatString.print();
        for(int i=0;i<commaTokens.size();i++) {
            IOUtils.write(commaTokens.get(i).toString());
            expNodes.get(i).print();
        }
        IOUtils.write(rightParenToken.toString());
        IOUtils.write(semicolonToken.toString());
    }
}




















