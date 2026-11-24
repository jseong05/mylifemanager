package com.mlm;

import com.google.gson.annotations.SerializedName;

/**
 * [기술 설명 / 면접 준비 주석]
 * - 가계부 내역(지출 또는 수입)을 나타내는 데이터 모델 클래스입니다.
 * - 고유 ID, 항목 제목(memo), 금액(money), 지출/수입 여부(isMinus), 낭비 챌린지 여부(isWaste), 작성 타임스탬프 정보를 보관합니다.
 */
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