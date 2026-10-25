package IONIOInstant;

import lombok.AllArgsConstructor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class PaymentReportService {

    private final PaymentScheduler scheduler;


    public void writeFullReport(Path outputFile, List<PaymentOrder> orders, Instant now) throws IOException {

        if (outputFile == null || orders == null || now == null) {
            throw new NullPointerException("Параметры не могут быть null.");
        }

        Path parentDir = outputFile.toAbsolutePath().getParent();
        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {

            if (orders != null) {
                for (PaymentOrder order : orders) {

                    writer.write("ID: " + order.id() + "\n");
                    writer.write("Клиент: " + order.client() + "\n");
                    writer.write("Сумма: " + order.amount() + "\n");
                    writer.write("Создан UTC: " + order.createAtUtc() + "\n");
                    writer.write("Локальное время платежа: " + order.scheduledAt().toLocalDateTime() + "\n");
                    writer.write("Момент платежа UTC: " + order.sheduledInstant() + "\n");
                    writer.write("Статус: " + scheduler.getStatus(order, now) + "\n");
                    writer.write("До исполнения: " + scheduler.timeUntilPayment(order, now) + "\n");
                    writer.write("Календарный период ожидания: " + scheduler.calendarWaitingPeriod(order) + "\n");
                    writer.newLine();

                }
            }
        }
    }

    public void writeDuePayments(Path outputFile,List<PaymentOrder> orders, Instant now)throws IOException{

        if (outputFile == null || orders == null || now == null){
            return;
        }

        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null && Files.notExists(parent)){
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile,StandardCharsets.UTF_8)) {
            List<PaymentOrder> orderList = scheduler.getDueOrders(orders, now);
            if (orderList != null) {
                for (PaymentOrder order : orderList) {
                    writer.write("Id: " + order.id() + "\n");
                    writer.write("Клиент: " + order.client() + "\n");
                    writer.write("Сумма: " + order.amount() + "\n");
                    writer.write("Момент исполнения: " + order.sheduledInstant() + "\n");
                    writer.newLine();
                }
            }
        }
    }

    public void writeErrors(Path outputFile, List<ImportError> errors) throws IOException{

        if (outputFile == null || errors == null) return;

        if (errors.isEmpty())return;

        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null && Files.notExists(parent)){
            Files.createDirectories(parent);
        }

        try(BufferedWriter writer = Files.newBufferedWriter(outputFile,StandardCharsets.UTF_8)){
            for (ImportError error : errors){
                writer.write("Строка: " + error.lineNumber() + "\n");
                writer.write("Исходные данные: " + error.rawLine() + "\n");
                writer.write("Причина: " + error.reason() + "\n");
                writer.newLine();

            }
        }
    }
}
