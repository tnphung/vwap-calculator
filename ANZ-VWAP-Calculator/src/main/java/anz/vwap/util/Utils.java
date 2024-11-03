package anz.vwap.util;

import org.springframework.util.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Utils {

    public static String DATE_TIME_FORMAT = "dd/MM/yyyy h:mm a";
    private static String today;
    private static NumberFormat numberFormat;

    public static double roundToDecimalPlaces(double value, int maxDecimalPlaces) {
        if (numberFormat == null) {
            numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
            // for trailing zeros:
            numberFormat.setMinimumFractionDigits(2);
        }
        numberFormat.setMaximumFractionDigits(maxDecimalPlaces);
        return Double.valueOf(numberFormat.format(value));
    }

    /**
     * Converts timestamp which is represented by string to long value.
     * @param timestampAsString Timestamp as string.
     * @return long value.
     * @throws ParseException
     */
    public static long convertTimestampToLong(String timestampAsString) throws Exception {

        if (!isValidate(timestampAsString)) {
            throw new Exception("Timestamp is not valid");
        }
        if (!StringUtils.hasText(today)) {
            today = getTodayAsString();
        }
        StringBuilder strBuilder = new StringBuilder(today);
        String dateTime = strBuilder.append(" ").append(timestampAsString).toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date today = dateFormat.parse(dateTime);

        return today.getTime();
    }

    private static boolean isValidate(String timestamp) {

        if (!StringUtils.hasText(timestamp)) {
            return false;
        }
        String regexPattern = "(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)";

        // Compile the ReGex
        Pattern compiledPattern = Pattern.compile(regexPattern);
        return compiledPattern.matcher(timestamp).matches();
    }
    private static String getTodayAsString() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Add 1 since January starts at 0
        int year = calendar.get(Calendar.YEAR);
        StringBuilder dateAsStr = new StringBuilder();
        return dateAsStr.append(day).append("/").append(month).append("/").append(year).toString();
    }
}
