package kiwi.ergo.fix.marketdata;

import java.math.BigDecimal;

public class DummyMarketDataProvider implements MarketDataProvider {

    private final BigDecimal defaultMarketPrice;

    DummyMarketDataProvider(BigDecimal defaultMarketPrice) {
        this.defaultMarketPrice = defaultMarketPrice;
    }

    public BigDecimal getAsk(String symbol) {
        return defaultMarketPrice;
    }

    public BigDecimal getBid(String symbol) {
        return defaultMarketPrice;
    }

}
