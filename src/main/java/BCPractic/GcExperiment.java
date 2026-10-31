package BCPractic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GcExperiment {
    private static volatile byte[] lastAllocated;

    public static void main(String[] args) {

        ZonedDateTime dateTime = ZonedDateTime.now();
        OffsetDateTime oDT= OffsetDateTime.now();

        boolean retainAll = args.length > 0 && args[0].equals("main");

        List<byte[]> retained = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();


            try (BufferedWriter writer =
                         Files.newBufferedWriter(Paths.get("backLog.dock"),
                                 StandardOpenOption.CREATE,
                                 StandardOpenOption.APPEND)) {

                for (int i = 0; i < 4000; i++) {

                    byte[] data = new byte[128 * 1024];

                    data[0] = (byte) i;

                    lastAllocated = data;

                    writer.write("GMT: "+dateTime);
                    writer.newLine();
                    writer.write("UTC: " +oDT);
                    writer.newLine();
                    writer.write("last allocate:" + lastAllocated.length);
                    writer.newLine();


                if (retainAll || i % 40 == 0) {
                    writer.write("retained added element\nsize retained:" + retained.size());
                    writer.newLine();
                    retained.add(data);
                }

                if (i % 250 == 0) {


                    long allocatedHeap = runtime.totalMemory();

                    long usedHeap = allocatedHeap - runtime.freeMemory();

                    writer.write("used heap: "+usedHeap  / 1024 / 1024 + " MiB");
                    writer.newLine();

                    System.out.println("Iteration = " + i);

                    System.out.println("Used Heap = " + usedHeap / 1024 / 1024 + " MiB");

                    System.out.println("Allocated Heap = " + allocatedHeap / 1024 / 1024 + " MiB");

                    writer.write("Allocated Heap = " + allocatedHeap / 1024 / 1024 + " MiB");
                    writer.newLine();

                    System.out.println("Retained arrays = " + retained.size());

                    System.out.println();
                }
            }

            System.out.println("Experiment completed");

        } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }
}
