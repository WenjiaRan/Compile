package llvmir.value;

import llvmir.IRModule;
import llvmir.Use;
import llvmir.type.Type;

import java.util.ArrayList;
import java.util.List;

public class Value {
    public IRModule module = IRModule.module;
    public String name;
    public Type type;
    public List<Use> usesList;//对应 def-use 关系
    public static int reg_num=0;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.usesList = new ArrayList<>();
    }
    public void removeUseByUser(User user) {
        usesList.removeIf(use -> use.user == user);
    }
}
