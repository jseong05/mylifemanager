package com.mlm;

import com.google.gson.annotations.SerializedName;

// 가계부 내역(지출/수입) 하나를 나타내는 데이터 모델 클래스 (ID, 제목, 금액, 지출여부, 낭비여부, 작성시각).
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