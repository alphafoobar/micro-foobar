package kiwi.ergo.fix.marketdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionSettings;

public class MarketDataFactory {

    private static final Logger log = LoggerFactory.getLogger(MarketDataFactory.class);
    private static final String DEFAULT_MARKET_PRICE_KEY = "DefaultMarketPrice";

    public static MarketDataProvider createMarketDataProvider(SessionSettings settings)
        throws ConfigError, FieldConvertError {
        double defaultMarketPrice = settings.getDouble(DEFAULT_MARKET_PRICE_KEY);
        log.warn(DEFAULT_MARKET_PRICE_KEY + " initialised to " + defaultMarketPrice);
        return new DummyMarketDataProvider(defaultMarketPrice);
    }
}
