package eivindw.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class AkkaCounterApp {

   public static void main(String[] args) throws Exception {
      ActorSystem actorSystem = ActorSystem.create();

      System.out.println("Akka kjører! " + tname());

      final ActorRef counter = actorSystem.actorOf(Props.create(Counter.class));
      final ActorRef client = actorSystem.actorOf(
         new RoundRobinPool(10).props(Props.create(Client.class, counter)),
         "client"
      );

      for(int i = 0; i < 1000; i++) {
         client.tell("doWork", ActorRef.noSender());
      }

      Thread.sleep(50);

      final Future<Object> getCount =
         Patterns.ask(counter, "getCount", 1000);
      final Object count =
         Await.result(getCount, Duration.create(1, TimeUnit.SECONDS));

      System.out.println("Resultat: " + count);

      actorSystem.shutdown();
   }

   static class Client extends AbstractActor {

      Client(ActorRef counter) {
         receive(ReceiveBuilder
            .matchEquals("doWork", msg -> counter.tell("countMe", self()))
            .build()
         );
      }
   }

   static class Counter extends AbstractActor {

      private int counter = 0;

      Counter() {
         receive(ReceiveBuilder
               .matchEquals("getCount", msg -> sender().tell(counter, self()))
               .match(String.class, msg -> counter++)
               .build()
         );
      }
   }

   private static String tname() {
      return Thread.currentThread().getName();
   }
}
