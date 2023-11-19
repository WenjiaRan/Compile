package llvmir.value;

import llvmir.Use;
import llvmir.type.Type;

import java.util.ArrayList;
import java.util.List;

public class User extends Value {
    public List<Value> ops;//use-def 关系

    public User(String name, Type type) {
        super(name, type);
        this.ops = new ArrayList<>();
    }
    public void addOperand(int posOfOperand, Value operand) {
        if (posOfOperand >= ops.size()) {
            return;
        }
        this.ops.set(posOfOperand, operand);
        if (operand != null) {
            operand.usesList.add(new Use(operand, this, posOfOperand));
        }
    }

    public void addOperand(Value operand) {
        this.ops.add(operand);
        if (operand != null) {
            operand.usesList.add(new Use(operand, this, ops.size()-1));
        }
    }

    public void removeUseFromOperands() {
        if (ops == null) {
            return;
        }
        for (Value operand : ops) {
            if (operand != null) {
                operand.removeUseByUser(this);
            }
        }
    }
}
