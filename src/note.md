# 词法分析

**从源程序中识别出单词，记录其单词类别和单词值**

词法分析中，单词的类别码统一定义如下：

| 单词名称     | 类别码     | 单词名称 | 类别码   | 单词名称 | 类别码 | 单词名称 | 类别码  |
| ------------ | ---------- | -------- | -------- | -------- | ------ | -------- | ------- |
| Ident        | IDENFR     | !        | NOT      | *        | MULT   | =        | ASSIGN  |
| IntConst     | INTCON     | &&       | AND      | /        | DIV    | ;        | SEMICN  |
| FormatString | STRCON     | \|\|     | OR       | %        | MOD    | ,        | COMMA   |
| main         | MAINTK     | for      | FORTK    | <        | LSS    | (        | LPARENT |
| const        | CONSTTK    | getint   | GETINTTK | <=       | LEQ    | )        | RPARENT |
| int          | INTTK      | printf   | PRINTFTK | >        | GRE    | [        | LBRACK  |
| break        | BREAKTK    | return   | RETURNTK | >=       | GEQ    | ]        | RBRACK  |
| continue     | CONTINUETK | +        | PLUS     | ==       | EQL    | {        | LBRACE  |
| if           | IFTK       | -        | MINU     | !=       | NEQ    | }        | RBRACE  |
| else         | ELSETK     | void     | VOIDTK   |          |        |          |         |

输入**源程序**; 每行输出**单词类别码 单词的字符/字符串形式**(中间仅用一个空格间隔)

## 流程分析

Lexer.java

`source`: 输入源程序字符串

`handleIdentifierOrKeyword(source, i)`: 处理从index==i开始的一个变量名OR关键字. 接收这个token的第一个索引(i), 返回这个token的最后一个索引. 其它的handle程序都是这样的

# 语法分析

输入词法分析得到的token流, 输出抽象语法树的**左右中**遍历(非终结符节点也要输出, 不过就是输出语法成分了)

```
1）按词法分析识别单词的顺序，按行输出每个单词的信息（要求同词法分析作业，对于预读的情况不能输出）。
   形如： 单词类别码 单词的字符/字符串形式(中间仅用一个空格间隔)
2）在文法中出现（除了<BlockItem>, <Decl>, <BType> 之外）的语法分析成分分析结束前，另起一行输出当前语法成分的名字，形如“<Stmt>”（注：未要求输出的语法成分仍需要进行分析，但无需输出
```

每一个语法成分其实都是一颗树, 所以我们先建立node文件夹, 为每一个语法成分建一个类

## 左递归和回溯

`AddExp -> MulExp | AddExp ('+' | '−') MulExp`

改成

`AddExp -> MulExp [('+' | '-') AddExp]`

<img src="C:\Users\Jagger\AppData\Roaming\Typora\typora-user-images\image-20231022120820705.png" alt="image-20231022120820705" style="zoom:50%;" />

注意输出也有变化, 都是这一个套路, 照着写即可

## Parser.java

`tokens`: 词法分析的token流

`index`: token的索引

`nodeType`: map: (NodeType.CompUnit, "<CompUnit>\n")...

`match(TokenType tokenType)`: 比较当前索引token的类型是否等于tokenType, 等于则返回下一个token, 否则报错

Parser.java中, 为每一个node中的类都创建一个函数, 比如

```java
public CompUnitNode CompUnit() {
        // CompUnit -> {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;

        // 判断是否直接就是main
        for (TokenType nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType();
             tokens.get(index + 1).getType() != TokenType.MAINTK && tokens.get(index + 2).getType() != TokenType.LPARENT;
             nextType = tokens.get(index + 1).getType(), nextNextType = tokens.get(index + 2).getType()) {
            // 有Decl
            DeclNode declNode = Decl();
            declNodes.add(declNode);
        }

        for (TokenType nextType = tokens.get(index + 1).getType();
             tokens.get(index + 1).getType() != TokenType.MAINTK;
             nextType = tokens.get(index + 1).getType()) {
            // FuncDef
            FuncDefNode funcDefNode = FuncDef();
            funcDefNodes.add(funcDefNode);
        }

        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }
```

