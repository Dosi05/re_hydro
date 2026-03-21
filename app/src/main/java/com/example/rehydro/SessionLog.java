package com.example.rehydro;

public class SessionLog {
    public int id;
    public long startTimestamp;
    public long endTimestamp;
    public int waterNeededMl;
    public int waterConsumedMl;
    public float hydrationPercent;
    public int drinkCount;

    public SessionLog(int id, long startTimestamp, long endTimestamp,
                      int waterNeededMl, int waterConsumedMl,
                      float hydrationPercent, int drinkCount) {
        this.id               = id;
        this.startTimestamp   = startTimestamp;
        this.endTimestamp     = endTimestamp;
        this.waterNeededMl    = waterNeededMl;
        this.waterConsumedMl  = waterConsumedMl;
        this.hydrationPercent = hydrationPercent;
        this.drinkCount       = drinkCount;
    }
}