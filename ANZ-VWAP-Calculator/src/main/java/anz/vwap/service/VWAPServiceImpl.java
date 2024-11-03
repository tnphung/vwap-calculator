package anz.vwap.service;

import anz.vwap.service.csv.model.VWAPRecord;
import anz.vwap.util.Utils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("vwapService")
public class VWAPServiceImpl implements VWAPService {

    private final String TIMESTAMP = "TIMESTAMP";
    private final String CURRENCY_PAIR = "CURRENCY-PAIR";
    private final String PRICE = "PRICE";
    private final String VOLUME = "VOLUME";
    private final int MAX_FIELD_LEN = 4;

    /**
     * Returns records from a CSV input file within a specified time frame.
     *
     * @param timeWindowMilliseconds The time window in milliseconds for which CSV records is read.
     * @param csvFilePath            Input file.
     * @return
     * @throws Exception
     */
    public List<VWAPRecord> calculateVWAP(long timeWindowMilliseconds, String csvFilePath) throws Exception {

        if (timeWindowMilliseconds == 0) {
            throw new Exception("Time window is missing");
        }
        List<VWAPRecord> returnVwapRecords = new ArrayList<>();
        List<VWAPRecord> tmpVwapRecords = new ArrayList<>();

        try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(Paths.get(csvFilePath)), CSVFormat.DEFAULT.withFirstRecordAsHeader()
                                                                                                                   .withIgnoreHeaderCase()
                                                                                                                   .withTrim())) {
            long endTime = 0;
            long startTime = 0;

            for (CSVRecord csvRecord : csvParser) {

                long timestamp = Utils.convertTimestampToLong(csvRecord.get(TIMESTAMP));
                if (startTime == 0) {
                    startTime = timestamp;
                }
                if (endTime == 0) {
                    endTime = timestamp + timeWindowMilliseconds;
                }
                if (timestamp < endTime) {
                    // Calculate VWAP within the time frame
                    updateVWAPRecordList(tmpVwapRecords, csvRecord, startTime, endTime);
                } else {
                    returnVwapRecords.addAll(tmpVwapRecords);
                    tmpVwapRecords.clear();
                    startTime = timestamp;
                    endTime = timestamp + timeWindowMilliseconds;
                    tmpVwapRecords.add(buildVwapRecord(csvRecord, startTime, endTime));
                }
            }
        }
        if (!CollectionUtils.isEmpty(tmpVwapRecords)) {
            returnVwapRecords.addAll(tmpVwapRecords);
        }
        return returnVwapRecords;

    }

    /**
     * Writes VWAP records to a CSV file.
     * @param csvFilePath Path to a CSV file
     * @param vwapRecords A list of VWAP records to be written to a file.
     * @throws IOException When there is an error in writing records to the CSV file.
     */
    public void writeVWAPRecords(String csvFilePath, List<VWAPRecord> vwapRecords) throws IOException {

        try (CSVPrinter writer  = new CSVPrinter(Files.newBufferedWriter(Paths.get(csvFilePath)),
                                                 CSVFormat.DEFAULT.withHeader("TIME WINDOW", "CURRENCY-PAIR", "VWAP"))) {
            if (!CollectionUtils.isEmpty(vwapRecords)) {
                for (VWAPRecord vwapRecord : vwapRecords) {
                    writer.printRecord(vwapRecord.getTimeWindow(), vwapRecord.getCurrencyPair(), vwapRecord.getVwap());
                }
            }
        }
    }

    private VWAPRecord buildVwapRecord(CSVRecord csvRecord, long startTime, long endTime) {

        long volume = getVolume(csvRecord);
        double price = Double.valueOf(csvRecord.get(PRICE)).doubleValue();
        double priceVolume = price * volume;
        VWAPRecord vwapRecord = new VWAPRecord(createTimeWindow(startTime, endTime),
                                               csvRecord.get(CURRENCY_PAIR),
                                               priceVolume,
                                               volume);
        return vwapRecord;
    }

    private void updateVWAPRecordList(List<VWAPRecord> tmpVwapRecords, CSVRecord csvRecord, long startTime, long endTime) throws Exception {

        long volume = getVolume(csvRecord);
        double price = Double.valueOf(csvRecord.get(PRICE)).doubleValue();
        double priceVolume = price * volume;

        if (tmpVwapRecords.isEmpty()) {
            VWAPRecord vwapRecord = new VWAPRecord(createTimeWindow(startTime, endTime),
                                                   csvRecord.get(CURRENCY_PAIR),
                                                   priceVolume,
                                                   volume);
            tmpVwapRecords.add(vwapRecord);
        } else {
            List<VWAPRecord> duplicateRecords = tmpVwapRecords.stream()
                                                              .filter(vr -> vr.getCurrencyPair().equalsIgnoreCase(csvRecord.get(CURRENCY_PAIR)))
                                                              .collect(Collectors.toList());
            if (duplicateRecords.isEmpty()) { // No duplicate VWAP records
                VWAPRecord vwapRecord = new VWAPRecord(createTimeWindow(startTime, endTime),
                                                       csvRecord.get(CURRENCY_PAIR),
                                                       priceVolume,
                                                       volume);
                tmpVwapRecords.add(vwapRecord);
            } else {
                if (duplicateRecords.size() > 1) {
                    throw new Exception("Two many duplicate VWAP records found!"); // There should only be one currency pair in the VWAP list.
                }
                duplicateRecords.get(0).addPriceVolume(priceVolume).addVolume(volume);
            }
        }
    }

    private long getVolume(CSVRecord csvRecord) {
        long volume;
        if (csvRecord.size() > MAX_FIELD_LEN) {
            StringBuilder volBuilder = new StringBuilder();
            for (int i = 3; i < csvRecord.size(); i++) {
                volBuilder.append(csvRecord.get(i));
            }
            volume = Double.valueOf(volBuilder.toString()).longValue();
        } else {
            volume = Double.valueOf(csvRecord.get(VOLUME)).longValue();
        }
        return volume;
    }

    private String createTimeWindow(long startTime, long endTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        StringBuilder timeWindow = new StringBuilder();
        return timeWindow.append(dateFormat.format(new Date(startTime)))
                         .append(" - ")
                         .append(dateFormat.format(new Date(endTime))).toString().toUpperCase();
    }
}
