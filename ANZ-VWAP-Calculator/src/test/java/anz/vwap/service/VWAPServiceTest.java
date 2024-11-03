package anz.vwap.service;

import anz.vwap.service.csv.model.VWAPRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
public class VWAPServiceTest {

    @Autowired
    private VWAPService vwapService;

    @Test
    public void calculateVWAP_WILL_return_6_VWAPRecords_WHEN_noError() throws Exception {

        // Given
        String filename = getClass().getClassLoader().getResource("currency_pairs_6.csv").getPath().replaceFirst("/", "");
        assertNotNull(filename);
        long timeWindow = 60*60*1000; // in milliseconds

        // Run test
        List<VWAPRecord> records = vwapService.calculateVWAP(timeWindow, filename);

        // Verify result
        assertTrue(!CollectionUtils.isEmpty(records));
        assertEquals(6, records.size());
    }

    @Test
    public void calculateVWAP_WILL_return_4_VWAPRecords_WHEN_noError() throws Exception {

        // Given
        String filename = getClass().getClassLoader().getResource("currency_pairs_4_rec.csv").getPath().replaceFirst("/", "");
        assertNotNull(filename);
        long timeWindow = 60*60*1000; // in milliseconds

        // Run test
        List<VWAPRecord> records = vwapService.calculateVWAP(timeWindow, filename);

        // Verify result
        assertTrue(!CollectionUtils.isEmpty(records));
        assertEquals(4, records.size());

        // The expected VWAP for AUD/USD and USD/JPY From 9:00 AM - 10:00 AM are as followings:
        double vwapAudUsd_9_10 = 0.655;
        double vwapUsdJpy_9_10 = 155;

        // The expected VWAP for AUD/USD and USD/JPY From 10:00 AM - 11:00 AM are as followings:
        double vwapAudUsd_10_11 = 0.78;
        double vwapUsdJpy_10_11 = 154.2857;

        records.forEach(vwapRecord -> {
            if (vwapRecord.getCurrencyPair().equalsIgnoreCase("AUD/USD")) {
                double vwap = vwapRecord.getVwap();
                if (vwap != vwapAudUsd_9_10 && vwap != vwapAudUsd_10_11) {
                    assertFalse("Incorrect VWAP value for currency pair " + vwapRecord.getCurrencyPair(), true);
                }
            } else if (vwapRecord.getCurrencyPair().equalsIgnoreCase("USD/JPY")) {
                double vwap = vwapRecord.getVwap();
                if (vwap != vwapUsdJpy_9_10 && vwap != vwapUsdJpy_10_11) {
                    assertFalse("Incorrect VWAP value for currency pair " + vwapRecord.getCurrencyPair(), true);
                }
            }
        });
    }
    @Test
    public void calculateVWAP_WILL_throwException_WHEN_timeWindowIsMissing() {

        // Given
        String filename = getClass().getClassLoader().getResource("currency_pairs_6.csv").getPath().replaceFirst("/", "");
        assertNotNull(filename);
        long timeWindow = 0;

        // Run test
        Exception exception = assertThrows(Exception.class, () -> {
            vwapService.calculateVWAP(timeWindow, filename);
        });

        assertTrue(StringUtils.hasText(exception.getMessage()));
    }

    @Test
    public void calculateVWAP_WILL_throwException_WHEN_cvsFilenameDoesNotExist() {

        // Given
        String filename = "test.csv";
        long timeWindow = 60*60*1000; // in milliseconds;

        // Run test
        Exception exception = assertThrows(Exception.class, () -> {
            vwapService.calculateVWAP(timeWindow, filename);
        });

        assertTrue(StringUtils.hasText(exception.getMessage()));
    }

    @Test
    public void writeVWAPRecords_WILL_writeVWAPRecordsToCSVFile_WHEN_noError() throws Exception {

        // Given
        String inputCsvFilePath = getClass().getClassLoader().getResource("currency_pairs_6.csv").getPath().replaceFirst("/", "");
        assertNotNull(inputCsvFilePath);
        long timeWindow = 60*60*1000; // in milliseconds
        List<VWAPRecord> records = vwapService.calculateVWAP(timeWindow, inputCsvFilePath);
        String vwapFile = buildVWAPFilePath(inputCsvFilePath, "vwap.csv");

        // Run test
        vwapService.writeVWAPRecords(vwapFile, records);

        // Verify
        File csvFile = new File(vwapFile);
        assertTrue(csvFile.exists());
        int fileSize = 252; // Expected file size when there are 6 records
        assertEquals(fileSize, csvFile.length());
    }

    @Test
    public void writeVWAPRecords_WILL_onlyWriteHeadersToCSVFile_WHEN_noRecords() throws Exception {

        // Given
        String filename = getClass().getClassLoader().getResource("currency_pairs_6.csv").getPath().replaceFirst("/", "");
        assertNotNull(filename);
        List<VWAPRecord> records = new ArrayList<>(); // Empty records
        String vwapFile = buildVWAPFilePath(filename,"vwap-header.csv");

        // Run test
        vwapService.writeVWAPRecords(vwapFile, records);

        // Verify
        File csvFile = new File(vwapFile);
        assertTrue(csvFile.exists());
        int fileSize = 32; // Expected file size when there is only headers
        assertEquals(fileSize, csvFile.length());

    }

    private String buildVWAPFilePath(String inputFilePath, String newFilename) {
        int lastIndex = inputFilePath.lastIndexOf("/");
        String vwapFile = inputFilePath.substring(0, lastIndex) + "/" + newFilename;
        File csvFile = new File(vwapFile);
        if (csvFile.exists()) {
            csvFile.delete();
        }
        return vwapFile;
    }
}
