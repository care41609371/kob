package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer botId;
    private String code;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;

    private boolean checkTailIncreasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    public List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};

        int x = sx, y = sy, step = 0;
        cells.add(new Cell(x, y));
        for (int d : steps) {
            x += dx[d];
            y += dy[d];
            cells.add(new Cell(x, y));
            if (!checkTailIncreasing(++step)) {
                cells.remove(0);
            }
        }

        return cells;
    }

    public String getStepsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int d : steps) {
            stringBuilder.append(d);
        }
        return stringBuilder.toString();
    }
}
