package llvmir;

import llvmir.value.User;
import llvmir.value.Value;

public class Use {
    public Value value;
    public final User user;
    public final int pos;

    public Use(Value value, User user, int pos) {
        this.value = value;
        this.user = user;
        this.pos = pos;
    }
}
