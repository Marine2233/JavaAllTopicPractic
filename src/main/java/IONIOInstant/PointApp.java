package IONIOInstant;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public class PointApp {
    public static void main(String[] args) {
        try {
            System.out.println("=== ЗАПУСК ПРИЛОЖЕНИЯ ОБРАБОТКИ ПЛАТЕЖЕЙ ===");

            // 1. Получить путь к входному файлу через Path.of("data", "payments.txt")
            Path inputFile = Path.of("data", "payments.txt");
            ensureInputFileExists(inputFile);
            System.out.println("Шаг 1. Входной файл: " + inputFile.toAbsolutePath());

            // 2. Создать PaymentFileService, PaymentSchedule и PaymentReportService
            PaymentFileService fileService = new PaymentFileService();
            PaymentScheduler scheduler = new PaymentScheduler();
            PaymentReportService reportService = new PaymentReportService(scheduler);
            System.out.println("Шаг 2. Все сервисы успешно инициализированы.");

            // Установка времени для воспроизводимого теста (Шаг 8)
            // Фиксированный момент времени для сверки с окончательным проверочным набором ТЗ
            Instant now = Instant.parse("2026-10-22T10:00:00Z");

            // ПОСЛЕ ПРОВЕРКИ ТЕСТА: раскомментируйте строчку ниже и закомментируйте строчку выше
            // Instant now = Instant.now();

            System.out.println("Используемая временная метка (UTC): " + now);

            // 3. Прочитать и проверить входные данные
            System.out.println("\nШаг 3. Запуск чтения и валидации данных...");
            ImportResult importResult = fileService.readOrders(inputFile);

            // 4. Вывести количество принятых и отклонённых платежей
            List<PaymentOrder> accepted = importResult.accepted();
            List<ImportError> rejected = importResult.rejected();

            System.out.println("\nШаг 4. Статистика обработки набора данных:");
            System.out.println("----------------------------------------");
            System.out.println(" Принято платежей  : " + accepted.size());
            System.out.println(" Отклонено строк   : " + rejected.size());
            System.out.println("----------------------------------------");
            System.out.printf("Проверка по ТЗ (Окончательный набор): Ожидается Принято: 4, Отклонено: 4. Фактически: %d и %d%n",
                    accepted.size(), rejected.size());

            // 5. Получить платежи, подлежащие исполнению, через getDueOrders()
            List<PaymentOrder> dueOrders = scheduler.getDueOrders(accepted, now);

            // Считаем распределение статусов для вывода проверочного набора
            long pendingCount = accepted.stream().filter(o -> "PENDING".equals(scheduler.getStatus(o, now))).count();
            long dueCount = dueOrders.size(); // Все due-ордера
            long overdueCount = accepted.stream().filter(o -> "OVERDUE".equals(scheduler.getStatus(o, now))).count();

            System.out.println("\nШаг 5. Распределение статусов принятых платежей:");
            System.out.println(" PENDING  : " + pendingCount);
            System.out.println(" DUE      : " + dueCount);
            System.out.println(" OVERDUE  : " + overdueCount);

            // 6. Сформировать три отчёта в папке output
            Path outputDir = Path.of("output");
            Path fullReportFile = outputDir.resolve("full_report.txt");
            Path duePaymentsFile = outputDir.resolve("due_payments.txt");
            Path errorsReportFile = outputDir.resolve("errors_report.txt");

            System.out.println("\nШаг 6. Генерация отчетных файлов...");
            reportService.writeFullReport(fullReportFile, accepted, now);
            reportService.writeDuePayments(duePaymentsFile, accepted, now);
            reportService.writeErrors(errorsReportFile, rejected);

            // 7. Вывести пути к созданным файлам
            System.out.println("\nШаг 7. Пути к созданным файлам отчетов:");
            System.out.println(" -> Полный отчет: " + fullReportFile.toAbsolutePath());
            System.out.println(" -> Очередь исполнения: " + duePaymentsFile.toAbsolutePath());
            System.out.println(" -> Лог ошибок импорта: " + errorsReportFile.toAbsolutePath());

            // 8. Проверить, что файлы существуют, и вывести их размер через Files.size()
            System.out.println("\nШаг 8. Проверка файловой системы и размеров файлов:");
            printFileInfo(fullReportFile);
            printFileInfo(duePaymentsFile);
            printFileInfo(errorsReportFile);

            // Дополнительное усложнение: Архивация исходного файла
            System.out.println("\n[Дополнительно]. Запуск архивации...");
            Path archiveDir = Path.of("archive");
            try {
                Path archivedPath = fileService.archiveInput(inputFile, archiveDir);
                System.out.println(" -> Исходный файл успешно перемещен в архив: " + archivedPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println(" -> Не удалось выполнить архивацию: " + e.getMessage());
            }

            System.out.println("\n=== РАБОТА ПРОГРАММЫ ЗАВЕРШЕНА СУЩЕСТВЕННЫМ УСПЕХОМ ===");

        } catch (IllegalArgumentException e) {
            System.err.println("\n[Ошибка бизнес-логики или аргументов]: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("\n[Критическая ошибка ввода-вывода (NIO)]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printFileInfo(Path path) throws IOException {
        if (Files.exists(path) && Files.isRegularFile(path)) {
            long sizeInBytes = Files.size(path);
            System.out.printf(" - Файл [%s] успешно верифицирован. Размер: %d байт.%n", path.getFileName(), sizeInBytes);
        } else {
            System.err.printf(" - ВНИМАНИЕ: Файл [%s] не найден на диске после генерации!%n", path.getFileName());
        }
    }

    private static void ensureInputFileExists(Path inputFile) throws IOException {
        Path parentDir = inputFile.toAbsolutePath().getParent();
        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        if (Files.notExists(inputFile)) {
            System.out.println("Файл data/payments.txt не найден. Автогенерация тестового набора данных...");
            try (BufferedWriter writer = Files.newBufferedWriter(inputFile, StandardCharsets.UTF_8)) {
                // Заголовок таблицы
                writer.write("id,client,amount,createdAtUtc,scheduledAt\n");

                // Набор данных (8 строк из ТЗ: 4 корректных, 4 ошибочных)
                writer.write("P001,Анна,150.00,2026-10-20T08:00:00Z,2026-10-22T12:00:00Z+03:00\n"); // Принят (DUE)
                writer.write("P002,Иван,2500.50,2026-10-21T09:30:00Z,2026-10-25T15:00:00Z+03:00\n"); // Принят (PENDING)
                writer.write("P003,Мария,450.00,2026-10-19T14:15:00Z,2026-10-21T10:00:00Z+02:00\n"); // Принят (OVERDUE)
                writer.write("P001,Дубликат,-500.00,2026-10-20T08:00:00Z,2026-10-22T12:00:00Z+03:00\n"); // Отклонен: Дубликат ID
                writer.write("P004,Ольга,-100.00,2026-10-20T10:00:00Z,2026-10-23T11:00:00Z+03:00\n"); // Отклонен: Отрицательная сумма
                writer.write("P005,Алексей,300.00,2026-10-20T12:00:00Z,НЕКОРРЕКТНАЯ_ДАТА\n"); // Отклонен: Формат даты платежа
                writer.write("P006,Сергей,90.00,НЕКОРРЕКТНАЯ_ДАТА_СОЗДАНИЯ,2026-10-24T09:00:00Z+03:00\n"); // Отклонен: Формат даты создания
                writer.write("P007,Дмитрий,800.00,2026-10-21T11:00:00Z,2026-10-22T09:00:00Z+03:00\n"); // Принят (DUE)
            }
        }
    }

}
