package anz.vwap.util;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static anz.vwap.util.Utils.DATE_TIME_FORMAT;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UtilsTest {


    @Test
    public void convertTimestampToLong_WILL_convertTimestampAsStringToLong_WHEN_timeIsValid() throws Exception {

        // Given
        String time = "9:10 am";

        // Run test
        long timestamp = Utils.convertTimestampToLong(time);

        // Verify test result
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        String dateTime = dateFormat.format(date);
        assertTrue(dateTime.contains(time));
    }

    @Test
    public void convertTimestampToLong_WILL_throwException_WHEN_missingTimeMarker() {

        // Given
        String time = "10:10";

        // Run test
        Exception exception = assertThrows(Exception.class, () -> {
            Utils.convertTimestampToLong(time);
        });

        // Verify test result
        String expected = "Timestamp is not valid"; //"Unparseable date";
        assertTrue(exception.getMessage().contains(expected));
    }

    @Test
    public void convertTimestampToLong_WILL_throwException_WHEN_timeIsNotValid() {

        // Given
        String time = "14:10 am";

        // Run test
        Exception exception = assertThrows(Exception.class, () -> {
            Utils.convertTimestampToLong(time);
        });

        // Verify test result
        String expected = "Timestamp is not valid"; //"Unparseable date";
        assertTrue(exception.getMessage().contains(expected));
    }

    @Test
    public void roundToDecimalPlaces_WILL_returnNumberWith4DecimalPlaces_WHEN_roundUp() {
        double expected = 0.7553;
        double testValue = 0.75528;
        double actual = Utils.roundToDecimalPlaces(testValue, 4);
        assertTrue(expected == actual);
    }

    @Test
    public void roundToDecimalPlaces_WILL_returnNumberWith4DecimalPlaces_WHEN_roundDown() {
        double expected = 0.8004;
        double testValue = 0.80044;
        double actual = Utils.roundToDecimalPlaces(testValue, 4);
        assertTrue(expected == actual);
    }
}
