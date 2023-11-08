package error;

import node.*;
import symbol.ArraySymbol;
import symbol.FuncParam;
import symbol.FuncSymbol;
import symbol.Symbol;
import symbol.FuncSymbol.ReturnType;

import java.util.*;

import utils.IOUtils;
import utils.Triple;

public class ErrorHandler {
    public static final ErrorHandler instance = new ErrorHandler();
    public static int loopCount;
    public List<Triple<Map<String, Symbol>, Boolean, ReturnType>>
            symbolTables = new ArrayList<>(); // 符号表栈 + 作用域
    //Second表示这个基本块是否是函数的最外层基本块
    //Third表示如果是函数的基本块，那么函数是否有返回值
    public List<Error> errors = new ArrayList<>();

    public void addSymbolTable(boolean isFunc, ReturnType funcType) {
        symbolTables.add(new Triple<>(new HashMap<>(), isFunc, funcType));
    }

    public void removeSymbolTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    public void printErrors() {
        // 使用匿名Comparator进行排序
        Collections.sort(errors, (e1, e2) -> Integer.compare(e1.getLineNumber(), e2.getLineNumber()));
      // 打印排序后的错误
        for (Error error : errors) {
            IOUtils.error(error.toString());
        }
    }
    public int isInCurTable(String ident) { // 变量是否在当前的基本块符号表
        if(symbolTables.get(symbolTables.size() - 1).first.containsKey(ident)){
            return 1;
        }
        return 0;
    }
//    public int isDeclared(S)

    public void addError(Error newError) {
        for (Error error : errors) {
            if (error.equals(newError)) {
                return;
            }
        }
        errors.add(newError);
    }

    public void compUnitError(CompUnitNode compUnitNode) {
        addSymbolTable(false, null);
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        for (DeclNode decl : compUnitNode.declNodes) {
            declError(decl);
        }
        for (FuncDefNode funcDef : compUnitNode.funcDefNodes) {
            funcDefError(funcDef);
        }
        mainFuncDefError(compUnitNode.mainFuncDefNode);
    }

    public void declError(DeclNode declNode){
        //声明  Decl → ConstDecl | VarDecl
        if(declNode.constDeclNode!=null){
            constDeclError(declNode.constDeclNode);
        }
        else {
            varDeclError(declNode.varDeclNode);
        }
    }

    public void constDeclError(ConstDeclNode constDeclNode){
//        常量声明    ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
        for (ConstDefNode constDef : constDeclNode.constDefNodes) {
            constDefError(constDef);
        }
    }

    private void constDefError(ConstDefNode constDefNode) {
        // ConstDef -> Ident { '[' ConstExp ']' } '=' ConstInitVal
        // 重复定义
        if (isInCurTable(constDefNode.identToken.content)==1) {
            ErrorHandler.instance.addError(new Error
                    (constDefNode.identToken.lineNumber,
                            Error.ErrorType.b));
            return;
        }

        for (ConstExpNode constExpNode : constDefNode.constExpNodes) {
            constExpError(constExpNode);
        }
        // 加入当前符号表
        put(constDefNode.identToken.content, new ArraySymbol(
                constDefNode.identToken.content, true,
                constDefNode.constExpNodes.size()));
        constInitValError(constDefNode.constInitValNode);
    }
    public void put(String ident, Symbol symbol) {//加入符号表
        symbolTables.get(symbolTables.size() - 1).first.put(ident, symbol);
    }
    public void constInitValError(ConstInitValNode constInitValNode) {
        // ConstInitVal -> ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (constInitValNode.constExpNode != null) {
            constExpError(constInitValNode.constExpNode);
        }
        else {
            for (ConstInitValNode constInitVal :
                    constInitValNode.constInitValNodes) {
                constInitValError(constInitVal);
            }
        }
    }
    public void varDeclError(VarDeclNode varDeclNode) {
        // VarDecl -> BType VarDef { ',' VarDef } ';'
        for (VarDefNode varDef : varDeclNode.varDefNodes) {
            varDefError(varDef);
        }
    }
    private void varDefError(VarDefNode varDefNode) {
        // VarDef -> Ident { '[' ConstExp ']' } [ '=' InitVal ]
        if (isInCurTable(varDefNode.identToken.content)==1) {
            ErrorHandler.instance.addError(new Error(
                    varDefNode.identToken.lineNumber, Error.ErrorType.b));
            return;
        }
        for (ConstExpNode constExpNode : varDefNode.constExpNodes) {
            constExpError(constExpNode);
        }
        put(varDefNode.identToken.content,
                new ArraySymbol(varDefNode.identToken.content,
                        false, varDefNode.constExpNodes.size()));
        if (varDefNode.initValNode != null) {
            initValError(varDefNode.initValNode);
        }
    }
    public void initValError(InitValNode initValNode) {
        // InitVal -> Exp | '{' [ InitVal { ',' InitVal } ] '}'
        if (initValNode.expNode != null) {
            expError(initValNode.expNode);
        } else {
            for (InitValNode initVal : initValNode.initValNodes) {
                initValError(initVal);
            }
        }
    }
    public void funcDefError(FuncDefNode funcDefNode) {
        // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
        if (isInCurTable(funcDefNode.ident.content)==1) {
            ErrorHandler.instance.addError(new Error
                    (funcDefNode.ident.lineNumber, Error.ErrorType.b));
            return;
        }
        // 函数名进符号表
        ReturnType returnType=Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                ReturnType.VOID:ReturnType.INT;
        if (funcDefNode.funcFParamsNode == null) {
            put(funcDefNode.ident.content, new FuncSymbol
                    (funcDefNode.ident.content,
                            Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                                    ReturnType.VOID:ReturnType.INT, new ArrayList<>()));
        } else {
            List<FuncParam> params = new ArrayList<>();
            for (FuncFParamNode funcFParamNode :
                    funcDefNode.funcFParamsNode.funcFParamNodes) {
                params.add(new FuncParam(funcFParamNode.ident.content,
                        funcFParamNode.leftSqareBrack.size()));
            }
            put(funcDefNode.ident.content, new FuncSymbol
                    (funcDefNode.ident.content, Objects.equals(funcDefNode.funcTypeNode.token.content, "void") ?
                            ReturnType.VOID:ReturnType.INT, params));
        }

        addSymbolTable(true, returnType);
        if (funcDefNode.funcFParamsNode != null) {
            funcFParamsError(funcDefNode.funcFParamsNode);
        }
        blockError(funcDefNode.blockNode);
        removeSymbolTable();
    }
    public void mainFuncDefError(MainFuncDefNode mainFuncDefNode){
        //主函数定义   MainFuncDef → 'int' 'main' '(' ')' Block // g j
        put("main", new FuncSymbol("main", ReturnType.INT, null));
        addSymbolTable(true, ReturnType.INT);
        blockError(mainFuncDefNode.blockNode);
        removeSymbolTable();
    }
    public void funcFParamsError(FuncFParamsNode funcFParamsNode){
        //函数形参表   FuncFParams → FuncFParam { ',' FuncFParam }
        for(FuncFParamNode funcFParamNode:
        funcFParamsNode.funcFParamNodes){
            funcFParamError(funcFParamNode);
        }
    }
    public void funcFParamError(FuncFParamNode funcFParamNode){
        //函数形参    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
        String name=funcFParamNode.ident.content;
        if(isInCurTable(name)==1){
            ErrorHandler.instance.addError(
                    new Error(funcFParamNode.ident.lineNumber, Error.ErrorType.b));
        }
        put(name, new ArraySymbol(name,false,funcFParamNode.leftSqareBrack.size()));

    }
    public void blockError(BlockNode blockNode) {
        //语句块     Block → '{' { BlockItem } '}'
        for (BlockItemNode blockItemNode : blockNode.blockItemNodes) {
            blockItemNodeError(blockItemNode);
        }
        // 这个基本块是否是函数的最外层基本块
        if (symbolTables.get(symbolTables.size() - 1).second) {
            List<BlockItemNode> blockItemNodes =blockNode.blockItemNodes;
            if (symbolTables.get(symbolTables.size() - 1).third == ReturnType.INT) {// 有返回值
                // 看看最后一个语句
                if (blockItemNodes.isEmpty() ||
                        blockItemNodes.get(blockItemNodes.size() - 1).stmtNode == null ||
                        blockItemNodes.get(blockItemNodes.size() - 1).stmtNode.returnToken == null) {
                    ErrorHandler.instance.addError(new Error(blockNode.rightBracket.getLineNumber(), Error.ErrorType.g));
                }
            }
        }
    }
    public void blockItemNodeError(BlockItemNode blockItemNode){
        //语句块项    BlockItem → Decl | Stmt
        if(blockItemNode.declNode!=null){
            declError(blockItemNode.declNode);
            return;
        }
        stmtError(blockItemNode.stmtNode);
    }
    public void stmtError(StmtNode stmtNode){
//        语句  Stmt → LVal '=' Exp ';' | [Exp] ';' | Block // h i
//                | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
//                | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
//                | 'break' ';' | 'continue' ';' // i m
//                | 'return' [Exp] ';' // f i
//                | LVal '=' 'getint''('')'';' // h i j
//                | 'printf''('FormatString{,Exp}')'';' // i j l
        if(stmtNode.stmtType== StmtNode.StmtType.Exp){
            if (stmtNode.expNode != null) expError(stmtNode.expNode);
        }
        else if(stmtNode.stmtType== StmtNode.StmtType.Block){
            addSymbolTable(false, null);
            blockError(stmtNode.blockNode);
            removeSymbolTable();
        }
        else if(stmtNode.stmtType==StmtNode.StmtType.If){
            condError(stmtNode.condNode);
            stmtError(stmtNode.stmtNodes.get(0));
            if (stmtNode.stmtNodes.size() > 1) {
                stmtError(stmtNode.stmtNodes.get(1));
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.For) {
            if (stmtNode.forStmtNodes.get(0) != null) {
                forStmtError(stmtNode.forStmtNodes.get(0));
            }
            if(stmtNode.condNode!=null) {
                condError(stmtNode.condNode);
            }
            if (stmtNode.forStmtNodes.get(1) != null) {
                forStmtError(stmtNode.forStmtNodes.get(1));
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.Break) {
            if (ErrorHandler.loopCount == 0) {
                ErrorHandler.instance.addError(new Error
                        (stmtNode.breakToken.
                                lineNumber, Error.ErrorType.m));
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.Continue) {
            if (ErrorHandler.loopCount == 0) {
                ErrorHandler.instance.addError(new Error
                        (stmtNode.breakToken.
                                lineNumber, Error.ErrorType.m));
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.Return) {
            if (stmtNode.expNode !=null) {
                expError(stmtNode.expNode);
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.LValAssignExp) {

            expError(stmtNode.expNode);
            // TODO: 为什么lValError在后面???
            lValError(stmtNode.lValNode);
            //TODO: 要是lVal没有定义?
            if (findIfDeclared(stmtNode.lValNode.ident.content) instanceof ArraySymbol) {
                ArraySymbol arraySymbol = (ArraySymbol) findIfDeclared
                        ((stmtNode.lValNode.ident.content));
                if (arraySymbol.isConst) {
                    ErrorHandler.instance.addError(new Error(
                            stmtNode.lValNode.ident.lineNumber, Error.ErrorType.h));
                }
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.LValGetint) {
            lValError(stmtNode.lValNode);
            //TODO: 要是lVal没有定义?
            if (findIfDeclared(stmtNode.lValNode.ident.content) instanceof ArraySymbol) {
                ArraySymbol arraySymbol = (ArraySymbol) findIfDeclared
                        ((stmtNode.lValNode.ident.content));
                if (arraySymbol.isConst) {
                    ErrorHandler.instance.addError(new Error(
                            stmtNode.lValNode.ident.lineNumber, Error.ErrorType.h));
                }
            }
        } else if (stmtNode.stmtType == StmtNode.StmtType.Printf) {
            String formatString = stmtNode.formatString.content;
            int numOfFormat=0;
            for (int i = 0; i < formatString.length(); i++) {
                if(formatString.charAt(i) == '%'&& i+1 < formatString.length()
                && formatString.charAt(i+1)=='d') {numOfFormat++;}
            }
            if (numOfFormat != stmtNode.expNodes.size()) {
                ErrorHandler.instance.addError(new Error(
                        stmtNode.formatString.lineNumber, Error.ErrorType.l
                ));
            }
            for (int i = 0; i < stmtNode.expNodes.size(); i++) {
                expError(stmtNode.expNodes.get(i));
            }
        }


    }
    public Symbol findIfDeclared(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                return symbolTables.get(i).first.get(ident);
            }
        }
        return null;
    }

    public void forStmtError(ForStmtNode forStmtNode) {
        expError(forStmtNode.expNode);
        // TODO: 为什么lValError在后面???
        lValError(forStmtNode.lValNode);
        //TODO: 要是lVal没有定义?
        if (findIfDeclared(forStmtNode.lValNode.ident.content) instanceof ArraySymbol) {
            ArraySymbol arraySymbol = (ArraySymbol) findIfDeclared
                    ((forStmtNode.lValNode.ident.content));
            if (arraySymbol.isConst) {
                ErrorHandler.instance.addError(new Error(
                        forStmtNode.lValNode.ident.lineNumber, Error.ErrorType.h));
            }
        }
    }

    public void expError(ExpNode expNode) {
        //表达式 Exp → AddExp 注：SysY 表达式是int 型表达式
        addExpError(expNode.addExpNode);
    }

    public void condError(CondNode condNode) {
        //条件表达式   Cond → LOrExp
        lOrExpError(condNode.lOrExpNode);
    }

    public void lValError(LValNode lValNode) {
        //左值表达式   LVal → Ident {'[' Exp ']'} // c k
        if (findIfDeclared(lValNode.ident.content) == null) {
            ErrorHandler.instance.addError(new Error(lValNode.ident.lineNumber
            , Error.ErrorType.c));
        }
        else if(((ArraySymbol) findIfDeclared(lValNode.ident.content)).isConst) {
            ErrorHandler.instance.addError(new Error(lValNode.ident.lineNumber
                    , Error.ErrorType.h));
        }
        else {
            for (ExpNode expNode : lValNode.expNodes) {
                expError(expNode);
            }
        }
    }

    public void primaryExpError(PrimaryExpNode primaryExpNode) {
        //基本表达式   PrimaryExp → '(' Exp ')' | LVal | Number
        if (primaryExpNode.expNode != null) {
            expError(primaryExpNode.expNode);
        } else if (primaryExpNode.lValNode != null) {
            lValError(primaryExpNode.lValNode);

        } else {

        }
    }
    public void unaryExpError(UnaryExpNode unaryExpNode) {
//        一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
//                | UnaryOp UnaryExp
        if (unaryExpNode.primaryExpNode != null) {
            primaryExpError(unaryExpNode.primaryExpNode);
        } else if (unaryExpNode.unaryExpNode != null) {
            unaryExpError(unaryExpNode.unaryExpNode);
        } else{
//            确保函数名存在于符号表中。
            Symbol symbol=findIfDeclared(unaryExpNode.ident.content);
            if ( symbol== null) {
                ErrorHandler.instance.addError(new Error(
                        unaryExpNode.ident.lineNumber, Error.ErrorType.c));
                return;
                // 每一行最多一个错误, 这里已经错了, 后面就不会错
            }
            //            确保该名称对应的符号是一个函数符号。
            // 即使之前已经定义, 需要检查是FuncType还是ArrayType!变量名和函数名不能混淆
            else if(!(symbol instanceof FuncSymbol)){
                //TODO: 这里的报错类型为何是e?
                ErrorHandler.instance.addError(new Error(
                        unaryExpNode.ident.lineNumber, Error.ErrorType.e));
            }
            // 参数. hello(x1,x2)
            assert symbol instanceof FuncSymbol;
            FuncSymbol funcSymbol=(FuncSymbol) symbol;
            int errorFlag;
            //            如果没有提供FuncRParams，但是函数定义需要参数，报告错误。
            if(unaryExpNode.funcRParamsNode==null) {
                if (funcSymbol.funcParams.size() != 0) {
                    errorFlag=1;
                    ErrorHandler.instance.addError(
                            new Error(unaryExpNode.ident.lineNumber, Error.ErrorType.d));
                }
            }
            else {
                //            如果提供了FuncRParams，则确保它们的数量与函数定义中的参数数量一致。

                if(unaryExpNode.funcRParamsNode.expNodes.size()!=funcSymbol.funcParams.size()){
                    errorFlag=1;
                    ErrorHandler.instance.addError(
                            new Error(unaryExpNode.ident.lineNumber, Error.ErrorType.d));
                }
                //检查函数定义参数（FuncFParams）和传递的实参（FuncRParams）的维数是否匹配。
                //参数的个数匹配了, 检查实参参数的变量是否已经定义
                List<ExpNode> expNodes=unaryExpNode.funcRParamsNode.expNodes;
                for (ExpNode expNode : expNodes) {
                    expError(expNode);
                }
                // 实参错误检查
                funcRParamsError(unaryExpNode.funcRParamsNode);
                // 维度检查
                for (ExpNode expNode : unaryExpNode.funcRParamsNode.expNodes){

                }
            }
        }
    }
    public FuncParam getFuncParamInExp(ExpNode expNode) {
        // Exp -> AddExp
        return getFuncParamInAddExp(expNode.addExpNode);
    }
    public FuncParam getFuncParamInAddExp(AddExpNode addExpNode) {
        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
        return getFuncParamInMulExp(addExpNode.mulExpNode);
    }
    public FuncParam getFuncParamInMulExp(MulExpNode mulExpNode) {
        return getFuncParamInUnaryExp(mulExpNode.unaryExpNode);
    }

    public FuncParam getFuncParamInUnaryExp(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.primaryExpNode != null) {
            return getFuncParamInPrimaryExp(unaryExpNode.primaryExpNode);
        } else if (unaryExpNode.ident != null) {
            Symbol symbol=findIfDeclared(unaryExpNode.ident.content);
            if(symbol instanceof FuncSymbol){
                FuncParam funcParam=new FuncParam(unaryExpNode.ident.content, 0);
                return  funcParam;
            }
            else return null;

        } else {
            return getFuncParamInUnaryExp(unaryExpNode.unaryExpNode);
        }
    }
    private FuncParam getFuncParamInPrimaryExp(PrimaryExpNode primaryExpNode) {
        // PrimaryExp -> '(' Exp ')' | LVal | Number
        if (primaryExpNode.expNode != null) {
            return getFuncParamInExp(primaryExpNode.expNode);
        } else if (primaryExpNode.lValNode != null) {
            return getFuncParamInLVal(primaryExpNode.lValNode);
        } else {
            return new FuncParam(null, 0);
        }
    }
    public FuncParam getFuncParamInLVal(LValNode lValNode) {
        return new FuncParam(lValNode.ident.content, lValNode.expNodes.size());
    }
    public void funcRParamsError(FuncRParamsNode funcRParamsNode) {
        // FuncRParams -> Exp { ',' Exp }
        for (ExpNode expNode : funcRParamsNode.expNodes) {
            expError(expNode);
        }
    }
    public void mulExpError(MulExpNode mulExpNode) {
        // MulExp -> UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
        unaryExpError(mulExpNode.unaryExpNode);
        if (mulExpNode.mulExpNode != null) {
            mulExpError(mulExpNode.mulExpNode);
        }
    }
//    private FuncParam getFuncParamInMulExp(MulExpNode mulExpNode) {
//        return getFuncParamInUnaryExp(mulExpNode.getUnaryExpNode());
//    }

    private void addExpError(AddExpNode addExpNode) {
        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
        mulExpError(addExpNode.mulExpNode);
        if (addExpNode.addExpNode != null) {
            addExpError(addExpNode.addExpNode);
        }
    }

//    private FuncParam getFuncParamInAddExp(AddExpNode addExpNode) {
//        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
//        return getFuncParamInMulExp(addExpNode.getMulExpNode());
//    }

    private void relExpError(RelExpNode relExpNode) {
        // RelExp -> AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
        addExpError(relExpNode.addExpNode);
        if (relExpNode.relExpNode != null) {
            relExpError(relExpNode.relExpNode);
        }
    }

    private void eqExpError(EqExpNode eqExpNode) {
        // EqExp -> RelExp | RelExp ('==' | '!=') EqExp
        relExpError(eqExpNode.relExpNode);
        if (eqExpNode.relExpNode != null) {
            eqExpError(eqExpNode.eqExpNode);
        }
    }

    private void lAndExpError(LAndExpNode lAndExpNode) {
        // LAndExp -> EqExp | EqExp '&&' LAndExp
        eqExpError(lAndExpNode.eqExpNode);
        if (lAndExpNode.lAndExpNode != null) {
            lAndExpError(lAndExpNode.lAndExpNode);
        }
    }

    private void lOrExpError(LOrExpNode lOrExpNode) {
        // LOrExp -> LAndExp | LAndExp '||' LOrExp
        lAndExpError(lOrExpNode.lAndExpNode);
        if (lOrExpNode.lOrExpNode != null) {
            lOrExpError(lOrExpNode.lOrExpNode);
        }
    }

    private void constExpError(ConstExpNode constExpNode) {
        // ConstExp -> AddExp
        addExpError(constExpNode.addExpNode);
    }

}





















