package thread;

import classwork.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InvoiceCalculator calculator = new InvoiceCalculator();

        Product monitor = new Product("M01", "Монитор", new BigDecimal("2200.00"));
        Product keyboard = new Product("K02", "Клавиатура", new BigDecimal("300.00"));
        Product mouse = new Product("MS03", "Мышь", new BigDecimal("150.00"));
        Product headphones = new Product("H04", "Наушники", new BigDecimal("450.00"));
        Product laptop = new Product("L05", "Ноутбук", new BigDecimal("75000.00"));

        List<InvoiceLine> lines = List.of(
                new InvoiceLine(monitor, 1),
                new InvoiceLine(keyboard, 1)
        );

        System.out.println("=== СЧЁТ №1 (Скидка 10%) ===");
        Invoice invoice1 = new Invoice(BigInteger.ONE, lines, new PercentDiscount(new BigDecimal("10")));
        InvoiceResult result1 = calculator.calculate(invoice1);
        printResult(result1);

        System.out.println("\n=== СЧЁТ №2 (Фиксированная скидка 300 руб.) ===");
        Invoice invoice2 = new Invoice(BigInteger.TWO, lines, new FixedDiscount(new BigDecimal("300.00")));
        InvoiceResult result2 = calculator.calculate(invoice2);
        printResult(result2);

        System.out.println("\n=== СЧЁТ №3 (ID = Long.MAX_VALUE) ===");
        BigInteger maxLongId = BigInteger.valueOf(Long.MAX_VALUE);
        List<InvoiceLine> lines3 = List.of(new InvoiceLine(laptop, 1));
        Invoice invoice3 = new Invoice(maxLongId, lines3, new FixedDiscount(BigDecimal.ZERO));
        InvoiceResult result3 = calculator.calculate(invoice3);
        printResult(result3);

        System.out.println("\n=== ПРОВЕРКА ПОГРАНИЧНОГО СЛУЧАЯ (Скидка больше стоимости) ===");
        List<InvoiceLine> cheapLines = List.of(new InvoiceLine(mouse, 1));
        Invoice edgeInvoice = new Invoice(BigInteger.valueOf(4), cheapLines, new FixedDiscount(new BigDecimal("500.00")));
        InvoiceResult edgeResult = calculator.calculate(edgeInvoice);
        printResult(edgeResult);

        System.out.println("\n=== УСЛОЖНЕНИЕ: ПРОВЕРКА DEFENSIVE COPYING ===");
        List<InvoiceLine> mutableLines = new ArrayList<>();
        mutableLines.add(new InvoiceLine(headphones, 1));

        Invoice secureInvoice = new Invoice(BigInteger.valueOf(5), mutableLines, new FixedDiscount(BigDecimal.ZERO));

        mutableLines.clear();
        mutableLines.add(new InvoiceLine(laptop, 10));


        System.out.println("Количество строк в Invoice после попытки изменить внешний список: " + secureInvoice.getInvoiceLines().size());
        InvoiceResult secureResult = calculator.calculate(secureInvoice);
        System.out.println("Итоговая сумма (должна остаться за 1 наушники = 540.00): " + secureResult.total());
    }

    private static void printResult(InvoiceResult r) {
        System.out.println("ID счета: " + r.invoiceId());
        System.out.println("Стоимость товаров (subtotal): " + r.subtotal());
        System.out.println("Скидка (discountAmount):      " + r.discountAmount());
        System.out.println("Сумма после скидки (taxable): " + r.taxableBase());
        System.out.println("Налог 20% (tax):              " + r.tax());
        System.out.println("Итого (total):                " + r.total());
    }
}