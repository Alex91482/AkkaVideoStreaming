package org.example;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;

public class Main {

    public static void main(String[] args) {

        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "akka-video-stream");

        var sc = new StreamController();

        final var binding = Http.get(system).newServerAt("localhost", 8080);
        binding.bind(sc.endpointStreamVideo());

        System.out.println("Server start");
    }
}