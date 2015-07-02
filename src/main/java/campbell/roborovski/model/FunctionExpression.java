package campbell.roborovski.model;

import sprockell.SprockellCompute;
import sprockell.SprockellEmitter;
import sprockell.SprockellRegister;

import java.io.IOException;

public class FunctionExpression extends Expression {
    private Function func;

    public FunctionExpression(Function func) {
        this.func = func;
    }

    @Override
    public void compile(SprockellEmitter emitter, Block block) throws IOException {
        start(emitter);

        // Allocate three int64 in a
        emitter.load(Program.NEW, SprockellRegister.a);
        emitter.emitConst(3, SprockellRegister.b);
        emitter.compute(SprockellCompute.Add, SprockellRegister.a, SprockellRegister.b, SprockellRegister.b);
        emitter.store(SprockellRegister.b, Program.NEW);

        // Push the frame to the stack
        emitter.push(SprockellRegister.a);

        // Store the argument count (0) into the frame
        emitter.emitConst(0, SprockellRegister.b);
        emitter.store(SprockellRegister.b, SprockellRegister.a);

        emitter.emitConst(1, SprockellRegister.b);
        emitter.compute(SprockellCompute.Add, SprockellRegister.a, SprockellRegister.b, SprockellRegister.a);

        // Store the function address into the frame
        emitter.emitConst(func.getOffset(), SprockellRegister.b);
        emitter.store(SprockellRegister.b, SprockellRegister.a);

        emitter.emitConst(1, SprockellRegister.b);
        emitter.compute(SprockellCompute.Add, SprockellRegister.a, SprockellRegister.b, SprockellRegister.a);

        // Calculate the AL

        int count = 0;
        Block currentBlock = block;

        while(func.getBlock() != currentBlock) {
            currentBlock = currentBlock.superBlock;
            count++;
        }

        emitter.push(SprockellRegister.sp);
        emitter.pop(SprockellRegister.c);
        emitter.emitConst(stackOffset, SprockellRegister.d);
        emitter.compute(SprockellCompute.Add, SprockellRegister.c, SprockellRegister.d, SprockellRegister.c);

        emitter.emitConst(count, SprockellRegister.b);
        emitter.branchAbsolute(SprockellRegister.b, getOffset() + 20);
        emitter.jumpAbsolute(getOffset() + 24);
        emitter.load(SprockellRegister.c, SprockellRegister.c);
        emitter.emitConst(1, SprockellRegister.d);
        emitter.compute(SprockellCompute.Sub, SprockellRegister.b, SprockellRegister.d, SprockellRegister.b);
        emitter.jumpAbsolute(getOffset() + 18);

        // Store the AL

        emitter.store(SprockellRegister.c, SprockellRegister.a);

        end(emitter);
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getSize() {
        return 25;
    }

    @Override
    public int calcSpill() {
        return 1;
    }
}
