package kiwi.ergo.fix.acceptor;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import quickfix.field.ExecID;
import quickfix.field.OrderID;

public enum Id {
    ;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendValue(YEAR, 4)
        .appendValue(MONTH_OF_YEAR, 2)
        .appendValue(DAY_OF_MONTH, 2)
        .appendLiteral('-')
        .appendValue(HOUR_OF_DAY, 2)
        .appendValue(MINUTE_OF_HOUR, 2)
        .appendValue(SECOND_OF_MINUTE, 2)
        .toFormatter();

    public static OrderID createOrderId() {
        return new OrderID(createId());
    }

    private static String createId() {
        return ZonedDateTime.now(ZoneId.of("UTC")).format(DATE_TIME_FORMATTER) + "-" + UUID
            .randomUUID().toString();
    }

    public static ExecID createExecId() {
        return new ExecID(createId());
    }
}
