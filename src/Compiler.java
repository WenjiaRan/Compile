
import config.Config;
import frontend.Lexer;
import utils.IOUtils;

import java.io.IOException;


public class Compiler {
    public static void main(String[] args) throws IOException {
        Config.init();
        Lexer.getInstance().analyze(IOUtils.read(Config.fileInPath));
        if (Config.lexer) {
            Lexer.getInstance().printLexAns();
        }

    }
}

//import java.nio.file.*;
//
//public class Compiler {
//    public static void main(String[] args) {
//        String filePath = "testfile.txt";
//        String currentDirectory = System.getProperty("user.dir");
//        System.out.println("当前工作目录: " + currentDirectory);
//
//        try {
//            // 读取文件内容
//            String content = Files.readString(Paths.get(filePath));
//            System.out.println(content);
//        } catch (NoSuchFileException e) {
//            System.out.println("文件不存在: " + filePath);
//        } catch (Exception e) {
//            System.out.println("发生异常: " + e.getMessage());
//        }
//    }
//}