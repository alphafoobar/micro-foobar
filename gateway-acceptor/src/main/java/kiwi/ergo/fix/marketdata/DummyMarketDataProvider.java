package kiwi.ergo.fix.marketdata;

public class DummyMarketDataProvider implements MarketDataProvider {

    private final double defaultMarketPrice;

    DummyMarketDataProvider(double defaultMarketPrice) {
        this.defaultMarketPrice = defaultMarketPrice;
    }

    public double getAsk(String symbol) {
        return defaultMarketPrice;
    }

    public double getBid(String symbol) {
        return defaultMarketPrice;
    }

}
