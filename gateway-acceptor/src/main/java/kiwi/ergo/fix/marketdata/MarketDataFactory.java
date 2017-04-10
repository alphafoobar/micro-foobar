package kiwi.ergo.fix.marketdata;

import java.math.BigDecimal;
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
        BigDecimal defaultMarketPrice = getDefaultPrice(settings);
        return new DummyMarketDataProvider(defaultMarketPrice);
    }

    private static BigDecimal getDefaultPrice(SessionSettings settings)
        throws ConfigError, FieldConvertError {

        BigDecimal defaultMarketPrice = settings.isSetting(DEFAULT_MARKET_PRICE_KEY)
            ? new BigDecimal(settings.getString(DEFAULT_MARKET_PRICE_KEY)) : BigDecimal.ZERO;

        log.info(DEFAULT_MARKET_PRICE_KEY + " initialised to " + defaultMarketPrice);
        return defaultMarketPrice;
    }
}
