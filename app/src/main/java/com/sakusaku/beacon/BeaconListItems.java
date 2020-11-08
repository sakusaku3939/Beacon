package com.sakusaku.beacon;

import org.altbeacon.beacon.Identifier;

public class BeaconListItems {

    private Identifier mUuid = null;
    private Identifier mMajor = null;
    private Identifier mMinor = null;
    private Integer mRssi = null;
    private Integer mTxPower = null;
    private Double mDistance = null;

    /**
     * 空のコンストラクタ
     */
    public BeaconListItems() {};

    /**
     * コンストラクタ
     * @param uuid UUID
     * @param major major値
     * @param minor minor値
     * @param rssi RSSI
     * @param txPower TxPower
     * @param distance Distance
     */
    public BeaconListItems(Identifier uuid, Identifier major, Identifier minor, Integer rssi, Integer txPower, Double distance) {
        mUuid = uuid;
        mMajor = major;
        mMinor = minor;
        mRssi = rssi;
        mTxPower = txPower;
        mDistance = distance;
    }

    public Identifier getmUuid() {
        return mUuid;
    }

    public void setmUuid(Identifier mUuid) {
        this.mUuid = mUuid;
    }

    public Identifier getmMajor() {
        return mMajor;
    }

    public void setmMajor(Identifier mMajor) {
        this.mMajor = mMajor;
    }

    public Identifier getmMinor() {
        return mMinor;
    }

    public void setmMinor(Identifier mMinor) {
        this.mMinor = mMinor;
    }

    public Integer getmRssi() {
        return mRssi;
    }

    public void setmRssi(Integer mRssi) {
        this.mRssi = mRssi;
    }

    public Integer getmTxPower() {
        return mTxPower;
    }

    public void setmTxPower(Integer mTxPower) {
        this.mTxPower = mTxPower;
    }

    public Double getmDistance() {
        return mDistance;
    }

    public void setmDistance(Double mDistance) {
        this.mDistance = mDistance;
    }

}