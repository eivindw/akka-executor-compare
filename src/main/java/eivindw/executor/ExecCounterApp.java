package eivindw.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecCounterApp {

   public static void main(String[] args) throws Exception {
      final ExecutorService executorService = Executors.newFixedThreadPool(10);

      final Counter counter = new Counter();

      for (int i = 0; i < 1_000; i++) {
         executorService.execute(counter::count);
      }

      System.out.println("Startet jobber!");

      executorService.shutdown();
      executorService.awaitTermination(1, TimeUnit.SECONDS);

      System.out.println("Resultat: " + counter.counter);
   }

   static class Counter {
      private int counter = 0;

      public void count() {
         counter++;
      }
   }
}
