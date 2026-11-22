package com.robloxcloudly.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Kolejka budowania – stawia bloki partiami, żeby serwer nie lagował. */
public class BuildQueue {
    private interface Op {
        int run(ServerLevel level, int budget);
        boolean done();
    }

    private static final int FLAGS = 2;
    private final ArrayDeque<Op> ops = new ArrayDeque<>();

    private static final class FillOp implements Op {
        final int x1, y1, z1, sx, sy, sz;
        final long total;
        final BlockState state;
        long i = 0;

        FillOp(int ax, int ay, int az, int bx, int by, int bz, BlockState st) {
            x1 = Math.min(ax, bx); y1 = Math.min(ay, by); z1 = Math.min(az, bz);
            sx = Math.abs(bx - ax) + 1; sy = Math.abs(by - ay) + 1; sz = Math.abs(bz - az) + 1;
            total = (long) sx * sy * sz;
            state = st;
        }

        public int run(ServerLevel level, int budget) {
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int used = 0;
            while (i < total && used < budget) {
                long k = i;
                int y = (int) (k % sy); k /= sy;
                int z = (int) (k % sz); k /= sz;
                int x = (int) k;
                p.set(x1 + x, y1 + y, z1 + z);
                level.setBlock(p, state, FLAGS);
                i++;
                used++;
            }
            return Math.max(used, 1);
        }

        public boolean done() { return i >= total; }
    }

    private static final class SetOp implements Op {
        final List<long[]> pos = new ArrayList<>();
        final List<BlockState> states = new ArrayList<>();
        int i = 0;

        public int run(ServerLevel level, int budget) {
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int used = 0;
            while (i < pos.size() && used < budget) {
                long[] a = pos.get(i);
                p.set((int) a[0], (int) a[1], (int) a[2]);
                level.setBlock(p, states.get(i), FLAGS);
                i++;
                used++;
            }
            return Math.max(used, 1);
        }

        public boolean done() { return i >= pos.size(); }
    }

    private static final class CmdOp implements Op {
        final String cmd;
        boolean ran = false;

        CmdOp(String c) { cmd = c; }

        public int run(ServerLevel level, int budget) {
            GameManager.command(cmd);
            ran = true;
            return 20;
        }

        public boolean done() { return ran; }
    }

    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        ops.add(new FillOp(x1, y1, z1, x2, y2, z2, state));
    }

    public void set(int x, int y, int z, BlockState state) {
        Op last = ops.peekLast();
        SetOp s;
        if (last instanceof SetOp so && so.pos.size() < 4096) s = so;
        else { s = new SetOp(); ops.add(s); }
        s.pos.add(new long[]{x, y, z});
        s.states.add(state);
    }

    public void cmd(String command) {
        ops.add(new CmdOp(command));
    }

    /** @return true gdy wszystko zbudowane */
    public boolean step(ServerLevel level, int budget) {
        while (budget > 0 && !ops.isEmpty()) {
            Op o = ops.peek();
            budget -= o.run(level, budget);
            if (o.done()) ops.poll();
        }
        return ops.isEmpty();
    }
}
