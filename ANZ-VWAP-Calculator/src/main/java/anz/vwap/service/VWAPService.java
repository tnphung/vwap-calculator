package anz.vwap.service;

import anz.vwap.service.csv.model.VWAPRecord;

import java.io.IOException;
import java.util.List;

public interface VWAPService {

    List<VWAPRecord> calculateVWAP(long timeWindowMilliseconds, String csvFile) throws Exception;
    void writeVWAPRecords(String csvFilePath, List<VWAPRecord> vwapRecords) throws IOException;
}
