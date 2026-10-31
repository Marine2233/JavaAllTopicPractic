package BCPractic;

import java.util.ArrayList;
import java.util.List;

public class GcExperiment {
    private static volatile byte[] lastAllocated;

    public static void main(String[] args) {

        boolean retainAll = args.length > 0 && args[0].equals("main");

        List<byte[]> retained = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();

        for (int i = 0; i < 4000; i++) {

            byte[] data = new byte[128 * 1024];

            data[0] = (byte) i;

            lastAllocated = data;

            if (retainAll || i % 40 == 0) {
                retained.add(data);
            }

            if (i % 250 == 0) {

                long allocatedHeap = runtime.totalMemory();

                long usedHeap = allocatedHeap - runtime.freeMemory();

                System.out.println("Iteration = " + i);

                System.out.println("Used Heap = " + usedHeap / 1024 / 1024 + " MiB");

                System.out.println("Allocated Heap = " + allocatedHeap / 1024 / 1024 + " MiB");

                System.out.println("Retained arrays = " + retained.size());

                System.out.println();
            }
        }

        System.out.println("Experiment completed");
    }
}
