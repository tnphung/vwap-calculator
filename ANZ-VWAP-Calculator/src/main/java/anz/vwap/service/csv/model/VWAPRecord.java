package anz.vwap.service.csv.model;


import anz.vwap.util.Utils;

public class VWAPRecord {

    String timeWindow;
    String currencyPair;
    double cumulativePriceVolume;
    long cumulativeVolume;

    public VWAPRecord(String timeWindow, String currencyPair, double cumulativePriceVolume, long cumulativeVolume) {
        this.timeWindow = timeWindow;
        this.currencyPair = currencyPair;
        this.cumulativePriceVolume = cumulativePriceVolume;
        this.cumulativeVolume = cumulativeVolume;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }

    public double getCumulativePriceVolume() {
        return cumulativePriceVolume;
    }

    public VWAPRecord addPriceVolume(double priceVolume) {
        this.cumulativePriceVolume += priceVolume;
        return this;
    }

    public long getCumulativeVolume() {
        return cumulativeVolume;
    }

    public VWAPRecord addVolume(long volume) {
        this.cumulativeVolume += volume;
        return this;
    }

    public double getVwap() {

        double result = cumulativePriceVolume / cumulativeVolume;
        return Utils.roundToDecimalPlaces(result, 4);
    }
}
