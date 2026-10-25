package IONIOInstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PaymentFileService {

    public Path archiveInput(Path inputFile, Path archiveDirectory) throws IOException {
        if (inputFile == null || archiveDirectory == null) {
            throw new NullPointerException("null параметры");
        }

        if (Files.notExists(inputFile) || !Files.isRegularFile(inputFile)){
            throw new IllegalArgumentException("Файл не существует");
        }

        if (Files.notExists(archiveDirectory)){
            Files.createDirectories(archiveDirectory);
        }

        int toDot = inputFile.getFileName().toString().lastIndexOf(".");
        String typeFile = inputFile.getFileName().toString().substring(toDot);
        String fileName = inputFile.getFileName().toString().substring(0,toDot)+"-"
                + Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                +typeFile ;
        Path archive= archiveDirectory.resolve(fileName);

        if (Files.exists(archive)) {
            throw new FileAlreadyExistsException("Архивный файл уже существует: " + archive.toAbsolutePath());
        }

        return Files.copy(inputFile,archive);
    }

    public ImportResult readOrders(Path inputFile) throws IOException {

        HashSet<String>uniqueIds = new HashSet<>();
        List<PaymentOrder> accepted = new ArrayList<>();
        List<ImportError> rejected = new ArrayList<>();

       if (inputFile == null || !Files.exists(inputFile) || !Files.isRegularFile(inputFile)){
           throw  new IllegalArgumentException("Файл не существует.");
       }

        try(BufferedReader reader = Files.newBufferedReader(inputFile,StandardCharsets.UTF_8)){

            String readLine = "";
            reader.readLine();
            PaymentOrder order = null;

            for (int i = 2; (readLine = reader.readLine()) != null  ; i++) {

                if (readLine.trim().isEmpty()){
                    continue;
                }

                try {
                    order = parseLine(readLine);

                    if (order != null &&
                            order.id() != null &&
                            !uniqueIds.contains(order.id()) &&
                            order.amount().compareTo(BigDecimal.ZERO) >= 0 &&
                            order.createAtUtc().isBefore(Instant.now())) {

                        uniqueIds.add(order.id());
                        accepted.add(order);

                    }else {

                        ImportError error = new ImportError(i,readLine,"Поврежденные данные.",new ArrayList<>());
                        rejected.add(error);

                    }
                }catch (IllegalArgumentException e){

                    String reason = (order == null) ? "Некорректное количество полей." : "Дубликат ID или неверные параметры платежа.";
                    ImportError error = new ImportError(i, readLine, reason, new ArrayList<>());
                    rejected.add(error);
                }
            }

        }
        return new  ImportResult(accepted,rejected);
    }

    private PaymentOrder parseLine(String line) {

        String[] parts = line.split(";", -1);
        if (parts.length != 6) {
            return null;
        }

        String id = parts[0];
        String account = parts[1];
        String amountStr = parts[2];
        String createdAtStr = parts[3];
        String localExecutionStr = parts[4];
        String zoneIdStr = parts[5];

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный формат суммы платежа: '" + amountStr + "'", e);
        }

        Instant createdAt;
        LocalDateTime localExecution;
        ZoneId zoneId;

        try {
            createdAt = Instant.parse(createdAtStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат момента создания (Instant): '" + createdAtStr + "'", e);
        }

        try {
            localExecution = LocalDateTime.parse(localExecutionStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат локального времени исполнения: '" + localExecutionStr + "'", e);
        }

        try {
            zoneId = ZoneId.of(zoneIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Неизвестный или некорректный часовой пояс: '" + zoneIdStr + "'", e);
        }

        List<ZoneOffset> validOffsets = zoneId.getRules().getValidOffsets(localExecution);

        if (validOffsets.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Ошибка DST: Локальное время %s в зоне %s не существует (пропуск времени при переводе стрелок вперед).",
                    localExecution, zoneId
            ));
        }

        if (validOffsets.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "Ошибка DST: Локальное время %s в зоне %s повторяется дважды. Допустимые смещения: %s",
                    localExecution, zoneId, validOffsets
            ));
        }

        ZoneOffset singleOffset = validOffsets.getFirst();

        ZonedDateTime scheduledExecution = ZonedDateTime.ofLocal(localExecution, zoneId, singleOffset);

        Duration delay = Duration.between(createdAt, scheduledExecution.toInstant());

        if (delay.toMinutes() < 30) {
            throw new IllegalArgumentException(String.format(
                    "Бизнес-ошибка: Между созданием (%s) и исполнением (%s) должно пройти не менее 30 минут. Фактически: %d мин.",
                    createdAt, scheduledExecution, delay.toMinutes()
            ));
        }

        return new PaymentOrder(id, account, amount, createdAt, scheduledExecution);

    }

}
