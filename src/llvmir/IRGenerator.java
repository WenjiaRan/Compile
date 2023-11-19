package llvmir;

import node.*;

import java.util.*;

import symbol.ArraySymbol;
import symbol.FuncSymbol;
import symbol.Symbol;
import token.TokenType;
import utils.Triple;

public class IRGenerator {
    public static boolean isConst;
    public static boolean isGlobal=false;
    public static int regNum=1;
    public static Integer saveValue=null;
    public static String tmpValue=null;
    public static String saveOp=null;
    public static IRGenerator irGenerator=new IRGenerator();
    public static StringBuilder llvmir = new StringBuilder();
    public List<Triple<Map<String, Symbol>, Boolean, FuncSymbol.ReturnType>>
            symbolTables = new ArrayList<>();
    //TODO:var2RegMap,reg2ValueMap没用!
    public LinkedHashMap<String,String> var2RegMap=new LinkedHashMap<>();//变量到寄存器的映射
    public LinkedHashMap<String,String> reg2ValueMap=new LinkedHashMap<>();//寄存器到值的映射
    public String findIndentation() {//获取代码缩进
//        int size=1;
//        StringBuilder sb = new StringBuilder();
//        for(int i=0; i<size*4; i++) {
//            sb.append(" ");
//        }
//        return sb.toString();
        return "";
    }
    public String getValueFromVar(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol=symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    return ((ArraySymbol) symbol).value;
                }

            }
        }
        return null;
    }

    public void changeValueFromVar(String ident, Integer newValue) {//值变了,寄存器也要变!
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol=symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    ((ArraySymbol) symbol).value= String.valueOf(newValue);
//                    ((ArraySymbol) symbol).regiName= String.valueOf(regNum);
                }

            }
        }
    }
    public String getRegiNameFromVar(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).first.containsKey(ident)) {
                Symbol symbol=symbolTables.get(i).first.get(ident);
                if (symbol instanceof ArraySymbol) {
                    return ((ArraySymbol) symbol).regiName;
                }

            }
        }
        return null;
    }
    public String getValueFromRegiName(String regiName) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            Map<String, Symbol> symbol=symbolTables.get(i).first;
            for (Map.Entry<String, Symbol> entry : symbol.entrySet()) {
                String key = entry.getKey();
                ArraySymbol value = (ArraySymbol) entry.getValue();
                if (Objects.equals(value.regiName, regiName)) {
                    if (Objects.equals(value.value, "null")) {
                        return null;
                    }
                    return value.value;
                }

            }
        }
        return null;
    }
    public void addSymbolTable(boolean isFunc, FuncSymbol.ReturnType funcType) {
        symbolTables.add(new Triple<>(new HashMap<>(), isFunc, funcType));
    }
    public int isInCurTable(String ident) { // 变量是否在当前的基本块符号表
        if(symbolTables.get(symbolTables.size() - 1).first.containsKey(ident)){
            return 1;
        }
        return 0;
    }
    public FuncSymbol.ReturnType findCurFunc() {//在函数体内时, 获得符号表关于该函数的内容
        for (int i=symbolTables.size()-1; i>=0; i--) {
            if (symbolTables.get(i).third != null) {
                return symbolTables.get(i).third;
            }
        }
        return null;
    }
    public void removeSymbolTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }
    // 计算方法
    public int calculate(String op, int a, int b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            default -> 0;
        };
    }



    public void visitCompUnit(CompUnitNode compUnitNode) {
        //CompUnit     → {Decl} MainFuncDef
        addSymbolTable(false, null);
        llvmir.append("""
                declare i32 @getint()
                declare void @putint(i32)
                declare void @putch(i32)
                declare void @putstr(i8*)
                
                """);
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        for (DeclNode declNode : compUnitNode.declNodes) {
            isGlobal=true;
            visitDecl(declNode);
            isGlobal=false;
        }
//        for (FuncDefNode funcDefNode : compUnitNode.funcDefNodes) {
//            visitFuncDef(funcDefNode);
//        }
        visitMainFuncDef(compUnitNode.mainFuncDefNode);
    }

    private void visitDecl(DeclNode declNode) {
        //Decl         → ConstDecl | VarDecl
        if (declNode.constDeclNode != null) {
            visitConstDecl(declNode.constDeclNode);
        }else{
            visitVarDecl(declNode.varDeclNode);
        }
    }

    private void visitVarDecl(VarDeclNode varDeclNode) {
        //VarDecl      → BType VarDef { ',' VarDef } ';'
        for (VarDefNode varDefNode : varDeclNode.varDefNodes) {
            visitVarDef(varDefNode);
        }
    }

    private void visitVarDef(VarDefNode varDefNode) {
        //VarDef       → Ident | Ident '=' InitVal
        String ident=varDefNode.identToken.content;


        if (isGlobal) {
            var2RegMap.put(varDefNode.identToken.content, "@" + ident);
            if (varDefNode.initValNode == null) {
                put(varDefNode.identToken.content,
                        new ArraySymbol(varDefNode.identToken.content,
                                false, varDefNode.constExpNodes.size(),
                                "* @" + ident,"0")
                );
                reg2ValueMap.put("@" + ident,"0");
                llvmir.append("@").append(ident).append(" = dso_local global i32 ").append("0\n");
            } else {

                visitInitVal(varDefNode.initValNode);
                put(varDefNode.identToken.content,
                        new ArraySymbol(varDefNode.identToken.content,
                                false, varDefNode.constExpNodes.size(),
                                "* @" + ident,saveValue.toString())
                );
                reg2ValueMap.put("@" + ident,saveValue.toString());
                llvmir.append("@").append(ident).append(" = dso_local global i32 ").append(saveValue).append("\n");
            }
        } else {
            var2RegMap.put(varDefNode.identToken.content, "%" + regNum);
            if (varDefNode.initValNode == null) {
                put(varDefNode.identToken.content,
                        new ArraySymbol(varDefNode.identToken.content,
                                false, varDefNode.constExpNodes.size(),
                                "* %" + regNum,"0")
                );
                reg2ValueMap.put("%" + regNum,"0");
                llvmir.append("%").append(regNum).append(" = alloca i32\n");
                llvmir.append(findIndentation());
                llvmir.append("store i32 ").append(0).append(", i32* ").append("%").append(regNum++).append("\n");
            } else {
                //TODO: 对于8+a这种式子也吸收进去了! 要不判断addExpNode.addExpNode是否为空???然后Mul也是一样...
//                int flag=0,var=0;
//                if(varDefNode.initValNode.expNode.addExpNode.mulExpNode.unaryExpNode.primaryExpNode.numberNode!=null){
//                    flag=1;
//                    var= Integer.parseInt(varDefNode.initValNode.expNode.addExpNode.mulExpNode.unaryExpNode.primaryExpNode.numberNode.getToken().content);
//                }
//                else{
                    visitInitVal(varDefNode.initValNode);
//                }
                put(varDefNode.identToken.content,
                        new ArraySymbol(varDefNode.identToken.content,
                                false, varDefNode.constExpNodes.size(),
                                "* %" + regNum,null)
                );
                reg2ValueMap.put("%" + regNum,saveValue.toString());
                llvmir.append(findIndentation());
                llvmir.append("%").append(regNum).append(" = alloca i32\n");
                llvmir.append(findIndentation());
                int prevReg=regNum-1;
//                if (flag == 1) {
//                    llvmir.append("store i32 ").append(var).append(", i32* ").append("%").append(regNum++).append("\n");
//                } else {
                    llvmir.append("store i32 ").append("%"+prevReg).append(", i32* ").append("%").append(regNum++).append("\n");
//                }

            }
        }
    }

    private void visitInitVal(InitValNode initValNode) {
        //InitVal      → Exp
        visitExp(initValNode.expNode);
    }

    private void visitConstDecl(ConstDeclNode constDeclNode) {
        //ConstDecl    → 'const' BType ConstDef { ',' ConstDef } ';'
        for (ConstDefNode constDef : constDeclNode.constDefNodes) {
            visitConstDef(constDef);
        }
    }
    public void put(String ident, Symbol symbol) {//加入符号表
        symbolTables.get(symbolTables.size() - 1).first.put(ident, symbol);
    }
    private void visitConstDef(ConstDefNode constDef) {
        //ConstDef     → Ident  '=' ConstInitVal
        String ident=constDef.identToken.content;

        //const int a=5;
        //@a = dso_local constant i32 5
        if (isGlobal) {
            visitConstInitVal(constDef.constInitValNode);
            put(constDef.identToken.content, new ArraySymbol(
                    constDef.identToken.content, true,
                    constDef.constExpNodes.size(),"* @" + ident,saveValue.toString()
            ));
            var2RegMap.put(constDef.identToken.content, "@" + ident);//全局变量也加进去map
            reg2ValueMap.put("@" + ident,saveValue.toString());
            llvmir.append("@").append(ident).append(" = dso_local constant i32 ").append(saveValue).append("\n");
        } else {
//            int flag=0,var=0;
//            //TODO: 对于8+a这种式子也吸收进去了!
//            if(constDef.constInitValNode.constExpNode.addExpNode.mulExpNode.unaryExpNode.primaryExpNode.numberNode!=null){
//                flag=1;
//                var= Integer.parseInt(constDef.constInitValNode.constExpNode.addExpNode.mulExpNode.unaryExpNode.primaryExpNode.numberNode.getToken().content);
//            }
//            else{
                visitConstInitVal(constDef.constInitValNode);
//            }

            put(constDef.identToken.content, new ArraySymbol(
                    constDef.identToken.content, true,
                    constDef.constExpNodes.size(),"* %" + regNum,null
            ));
            var2RegMap.put(constDef.identToken.content, "%" + regNum);
            reg2ValueMap.put("%" + regNum,saveValue.toString());

            llvmir.append(findIndentation());
            llvmir.append("%").append(regNum).append(" = alloca i32\n");
            llvmir.append(findIndentation());
            int prevReg=regNum-1;
//            if (flag == 1) {
//                llvmir.append("store i32 ").append(var).append(", i32* ").append("%").append(regNum++).append("\n");
//            }
//            else{
                llvmir.append("store i32 ").append("%"+prevReg).append(", i32* ").append("%").append(regNum++).append("\n");
//            }
        }
    }

    private void visitConstInitVal(ConstInitValNode constInitValNode) {
        //ConstInitVal → ConstExp
        visitConstExp(constInitValNode.constExpNode);
    }

    private void visitConstExp(ConstExpNode constExpNode) {
        //ConstExp     → AddExp
        isConst=true;
        visitAdd(constExpNode.addExpNode);
    }

    public void visitMainFuncDef(MainFuncDefNode mainFuncDefNode) {
        //MainFuncDef → 'int' 'main' '(' ')' Block
        llvmir.append("define dso_local i32 @main() ");
        llvmir.append("{\n");
        visitBlock(mainFuncDefNode.blockNode);
        llvmir.append("\n}\n");
    }


    private void visitBlock(BlockNode blockNode) {
        //Block       → '{' { BlockItem } '}'

        for(BlockItemNode blockItemNode:blockNode.blockItemNodes){
            llvmir.append(findIndentation());
            visitBlockItem(blockItemNode);
        }


    }

    private void visitBlockItem(BlockItemNode blockItemNode) {
        //BlockItem → Decl | Stmt
        if (blockItemNode.declNode != null) {
            visitDecl(blockItemNode.declNode);
        } else {
            visitStmt(blockItemNode.stmtNode);
        }

    }

    private void visitStmt(StmtNode stmtNode) {
        //Stmt       → LVal '=' Exp ';'
        //           | [Exp] ';'
        //           | 'return' Exp ';'
        // | '      | Block'
        //LVal '=' 'getint''('')'';'
        //     | 'printf''('FormatString{','Exp}')'';'
        if (stmtNode.stmtType== StmtNode.StmtType.Return) {
            visitExp(stmtNode.expNode);
            llvmir.append("ret i32 ");
            int curReg=regNum-1;
            llvmir.append("%"+curReg).append("\n");
            saveValue=null;//使用了要归零
        } else if (stmtNode.stmtType== StmtNode.StmtType.LValAssignExp) {
            visitExp(stmtNode.expNode);
            int newValue = saveValue;
            changeValueFromVar(stmtNode.lValNode.ident.content, null);

//            llvmir.append("%").append(regNum).append(" = add i32 0, ").append(regNum-1).append("\n");
//            llvmir.append(findIndentation());
//            regNum++;
            int prevReg=regNum-1;
//            llvmir.append("")
            llvmir.append("store i32 ").append("%"+prevReg+", i32").append(getRegiNameFromVar(stmtNode.lValNode.ident.content)).append("\n");

//            llvmir.append("store i32 ").append(saveValue).append(", i32* ").append("%").append(regNum++).append("\n");
            saveValue = null;
        } else if(stmtNode.stmtType== StmtNode.StmtType.Block) {
            visitBlock(stmtNode.blockNode);
        } else if (stmtNode.stmtType == StmtNode.StmtType.LValGetint) {
            llvmir.append("%").append(regNum).append(" = call i32 @getint()\n");
            String regName=getRegiNameFromVar(stmtNode.lValNode.ident.content);
            llvmir.append(findIndentation());
            llvmir.append("store i32 ").append("%"+regNum).append(", i32* ").append(regName).append("\n");

        }
        else if (stmtNode.stmtType == StmtNode.StmtType.Printf) {
            //     | 'printf''('FormatString{','Exp}')'';'
            String[] formatStrings = stmtNode.formatString.getContent().replace("\\n", "\n").replace("\"", "").split("%d");
            List<Integer> args = new ArrayList<>();
            int i = 0;
            for (ExpNode expNode : stmtNode.expNodes) {
                saveValue=null;
                visitExp(expNode);
                args.add(saveValue);
                saveValue=null;
            }
            for (String formatString : formatStrings) {
                for (char c : formatString.toCharArray()) {
                    llvmir.append("call void @putch(i32 ").append((int) c).append(")\n");
                    llvmir.append(findIndentation());
                }
                if (!args.isEmpty()) {
                    llvmir.append("call void @putint(i32 ").append(args.get(i++)).append(")\n");
                    llvmir.append(findIndentation());
                }
            }
        } else {
            //TODO:[Exp] ';'不用处理?
        }


    }

    private void visitExp(ExpNode expNode) {
        //Exp         → AddExp

        visitAdd(expNode.addExpNode);
    }

    private void visitAdd(AddExpNode addExpNode) {
        if (isGlobal) {
            this.visitMulExp(addExpNode.mulExpNode);
            if (addExpNode.addExpNode != null) {
                Integer tmpValue = saveValue;
                saveValue = null;
                this.visitAdd(addExpNode.addExpNode);
                if (saveValue != null) {
                    saveOp = addExpNode.operation.content;
                    saveValue = this.calculate(saveOp, tmpValue, saveValue);
                }
            }
        }
        else {
            // 处理左侧的乘法表达式
            visitMulExp(addExpNode.mulExpNode);
            String leftValue = tmpValue; // 保存左侧表达式的结果

            // 如果有右侧加法表达式，递归处理
            if (addExpNode.addExpNode != null) {
                visitAdd(addExpNode.addExpNode);
                String rightValue = tmpValue; // 保存右侧表达式的结果

                // 生成LLVM IR代码
                String llvmOp = addExpNode.operation.getType() == TokenType.PLUS ? "add" : "sub";
                // 如果要运算的是全局变量, 直接取出值, 全局变量肯定知道值
                // 如果直接是数, 就不做处理!
                if(leftValue.matches("^-?\\d+$")){

                }else{
                    String haha=getValueFromRegiName(leftValue);
                    if (haha!=null) {
                        leftValue=haha;
                    }else{
                        // 指针才需要另开变量赋值!
                        if (leftValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                            leftValue="%"+regNum;
                            regNum++;
                        }

                    }
                }
                if (rightValue.matches("^-?\\d+$")) {

                }else{
                    String ahah=getValueFromRegiName(rightValue);

                    if(ahah!=null) {
                        rightValue=ahah;
                    }else{
                        // 指针才需要另开变量赋值!
                        if (rightValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                            rightValue="%"+regNum;
                            regNum++;
                        }
                    }
                }

                int tmpVarNum = getNextTmpVarNum(); // 获取下一个临时变量编号
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append(llvmOp).append(" i32 ")
                        .append(leftValue).append(", ").append(rightValue)
                        .append("\n");
                tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量
            } else {
                tmpValue = leftValue; // 没有右侧表达式，直接使用左侧结果
                //TODO: 我日你妈!!!
//                llvmir.append("%").append(regNum++).append(" = ")
//                        .append("add").append(" i32 ")
//                        .append(leftValue).append(", ").append("0")
//                        .append("\n");
            }
        }
    }

    // 辅助方法：获取下一个临时变量编号
    private int getNextTmpVarNum() {
        // 假设有一个全局变量来跟踪当前的临时变量编号
        return regNum++;
    }


    private void visitMulExp(MulExpNode mulExpNode) {
        //        MulExp     → UnaryExp |UnaryExp ('*' | '/' | '%') MulExp
        if (isGlobal) {
            this.visitUnaryExp(mulExpNode.unaryExpNode);
            if (mulExpNode.mulExpNode != null) {
                Integer tmpValue = saveValue;
                saveValue = null;
                this.visitMulExp(mulExpNode.mulExpNode);
                if (saveValue != null) {
                    saveOp = mulExpNode.operation.content;
                    saveValue = this.calculate(saveOp, tmpValue, saveValue);
                    saveOp = null;
                }
            }
        }else {
            // 处理左侧的一元表达式
            visitUnaryExp(mulExpNode.unaryExpNode);
            String leftValue = tmpValue; // 保存左侧表达式的结果

            // 如果有右侧乘法表达式，递归处理
            if (mulExpNode.mulExpNode != null) {
                visitMulExp(mulExpNode.mulExpNode);
                String rightValue = tmpValue; // 保存右侧表达式的结果

                // 生成LLVM IR代码
                String llvmOp;
                if (mulExpNode.operation.getType() == TokenType.MULT) {
                    llvmOp = "mul";
                } else if (mulExpNode.operation.getType() == TokenType.DIV) {
                    llvmOp = "div";
                } else {
                    llvmOp = "rem";
                }
                // 如果直接是数, 就不做处理!
                if(leftValue.matches("^-?\\d+$")){

                }else{
                    String haha=getValueFromRegiName(leftValue);
                    if (haha!=null) {
                        leftValue=haha;
                    }else{
                        // 指针才需要另开变量赋值!
                        if (leftValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(leftValue).append("\n");
                            leftValue="%"+regNum;
                            regNum++;
                        }

                    }
                }
                if (rightValue.matches("^-?\\d+$")) {

                }else{
                    String ahah=getValueFromRegiName(rightValue);

                    if(ahah!=null) {
                        rightValue=ahah;
                    }else{
                        // 指针才需要另开变量赋值!
                        if (rightValue.charAt(0) == '*') {
                            llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(rightValue).append("\n");
                            rightValue="%"+regNum;
                            regNum++;
                        }
                    }
                }

                int tmpVarNum = getNextTmpVarNum(); // 获取下一个临时变量编号
                llvmir.append("%").append(tmpVarNum).append(" = ")
                        .append(llvmOp).append(" i32 ")
                        .append(leftValue).append(", ").append(rightValue)
                        .append("\n");
                tmpValue = "%" + tmpVarNum; // 更新tmpValue为新的临时变量
            } else {
                tmpValue = leftValue; // 没有右侧表达式，直接使用左侧结果
            }
        }
    }


    private void visitUnaryExp(UnaryExpNode unaryExpNode) {
        // UnaryExp -> PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (isGlobal) {
            if (unaryExpNode.primaryExpNode != null) {
                this.visitPrimaryExp(unaryExpNode.primaryExpNode);
            } else if (unaryExpNode.unaryExpNode != null) {
                this.visitUnaryExp(unaryExpNode.unaryExpNode);
                if (Objects.equals(unaryExpNode.unaryOpNode.token.content, "-")) {
                    saveValue = -saveValue;
                }
            }
        }else {
            if (unaryExpNode.primaryExpNode != null) {
                // 处理主表达式
                visitPrimaryExp(unaryExpNode.primaryExpNode);
//        } else if (unaryExpNode.ident != null) {
//            // 处理函数调用或变量
//            // 你可能需要根据你的实现细节调整这里的代码
//            visitFunctionOrVariable(unaryExpNode.ident, unaryExpNode.funcRParamsNode);
            } else {
                // 处理一元操作符
                visitUnaryExp(unaryExpNode.unaryExpNode);
                String value = tmpValue;

                // 生成LLVM IR代码
                String llvmOp;
                if (unaryExpNode.unaryOpNode.token.getType() == TokenType.MINU) {
                    llvmOp = "sub";
                    // 如果直接是数, 就不做处理!
                    if(value.matches("^-?\\d+$")){

                    }else{
                        String haha=getValueFromRegiName(value);
                        if (haha!=null) {
                            value=haha;
                        }else{
                            if (value.charAt(0) == '*') {
                                llvmir.append("%").append(regNum).append(" = ").append("load i32, i32").append(value).append("\n");
                                value="%"+regNum;
                                regNum++;
                            }

                        }
                    }


                    int tmpVarNum = getNextTmpVarNum();
                    llvmir.append("%").append(tmpVarNum).append(" = ")
                            .append(llvmOp).append(" i32 0, ").append(value)
                            .append("\n");
                    tmpValue = "%" + tmpVarNum;
                } else {
                    // 其他一元操作，如 NOT
                    // 根据需求添加相应的代码
                }
            }
        }
    }

    private void visitFunctionOrVariable(IdentNode ident, FuncRParamsNode funcRParamsNode) {
        // 根据你的程序的具体实现，这里可能需要处理函数调用或变量访问
        // 生成相应的LLVM IR代码
    }


    private void visitPrimaryExp(PrimaryExpNode primaryExpNode) {
        //PrimaryExp → '(' Exp ')' | LVal | Number
        if (isGlobal) {
            if (primaryExpNode.numberNode != null) {
                saveValue = Integer.valueOf(primaryExpNode.numberNode.getToken().content);
            } else if (primaryExpNode.lValNode != null) {
                saveValue = Integer.valueOf(this.getValueFromVar(primaryExpNode.lValNode.ident.content));
            } else if (primaryExpNode.expNode != null) {
                this.visitExp(primaryExpNode.expNode);
            }
        }
        else {
            if (primaryExpNode.expNode != null) {
                // 处理括号内的表达式
                visitExp(primaryExpNode.expNode);
            } else if (primaryExpNode.lValNode != null) {
                // 处理变量
                visitLval(primaryExpNode.lValNode);
            } else if (primaryExpNode.numberNode != null) {
                // 处理数字
                visitNumber(primaryExpNode.numberNode);
            }
        }
    }

    private void visitNumber(NumberNode numberNode) {
        tmpValue=numberNode.getToken().content;
        saveValue= Integer.valueOf(tmpValue);
        if (!isGlobal) {
            //TODO!!!这里或许可以改!
            llvmir.append("%").append(regNum).append(" = add i32 0, ").append(tmpValue).append("\n");
            regNum++;
        }
            }

    private void visitLval(LValNode lValNode) {
        // 先暂时不考虑全局变量!
//        llvmir.append("%").append(regNum).append(" = alloca i32\n");
//        String value = getRegiNameFromVar(lValNode.ident.content);
//        llvmir.append("store i32 "+);
        tmpValue=getRegiNameFromVar(lValNode.ident.content);
//        llvmir.append("%").append(regNum).append(" = add i32 0, ").append("")
    }


    public String toString() {

        return llvmir.toString();
    }

}








