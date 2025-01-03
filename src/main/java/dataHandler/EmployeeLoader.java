package dataHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EmployeeLoader {
    private static final Logger logger = Logger.getLogger(EmployeeLoader.class.getName());
    private static final String FILE_PATH = "data/employee.csv";

    // 사용자 목록을 불러오는 메서드
    public List<String> loadEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    employeeNames.add(line.trim());
                }
            }
        } catch (IOException e) {
            logger.severe("직원 데이터를 읽는 중 오류 발생: " + e.getMessage());
        }
        return employeeNames;
    }
}
