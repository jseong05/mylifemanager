package com.mlm;

import com.google.gson.annotations.SerializedName;

public class Expense {
    private String id;
    private String memo;
    private int money;
    private boolean isMinus;
    private boolean isWaste;

    @SerializedName("timestamp")
    private long timestamp;

    public Expense(String id, String memo, int money, boolean isMinus, boolean isWaste) {
        this(id, memo, money, isMinus, isWaste, System.currentTimeMillis());
    }

    public Expense(String id, String memo, int money, boolean isMinus, boolean isWaste, long timestamp) {
        this.id = id;
        this.memo = memo;
        this.money = money;
        this.isMinus = isMinus;
        this.isWaste = isWaste;
        this.timestamp = (timestamp == 0) ? System.currentTimeMillis() : timestamp;
    }

    public String getId() { return id; }
    public String getMemo() { return memo; }
    public int getMoney() { return money; }
    public boolean isMinus() { return isMinus; }
    public boolean isWaste() { return isWaste; }
    public long getTimestamp() {
        return timestamp; // 0이면 날짜 미정(옛 데이터)이라는 뜻
    }

}