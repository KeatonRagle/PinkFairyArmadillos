package com.pink.pfa.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatabaseBackupService {

    @Value("${spring.datasource.username:Pink}")
    private String dbUser;

    @Value("${spring.datasource.password:Test}")
    private String dbPassword;

    @Value("${db.name:PFA_DB_TEST}")
    private String dbName;

    private final String backupDir = "/backups";

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupService.class);

    public void backup(String label) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = backupDir + "/" + label + "_" + timestamp + ".sql.gz";

            ProcessBuilder pb = new ProcessBuilder(
                "sh", "-c",
                "mysqldump -u " + dbUser + " -p" + dbPassword + " " + dbName + " | gzip > " + fileName
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Database backup successful: {}", fileName);
            } else {
                log.error("Database backup failed with exit code: {} output: {}", exitCode, output);
            }
        } catch (Exception e) {
            log.error("Database backup failed: {}", e.getMessage());
        }
    }
}
