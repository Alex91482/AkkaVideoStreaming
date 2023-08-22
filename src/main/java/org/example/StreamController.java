package org.example;

import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.FileIO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;


public class StreamController extends AllDirectives {

    public StreamController() {
        var path = "./src/main/resources/videos/video1.mp4";
        System.out.println("File path: " + path + "\nExist: " + Files.exists(Path.of(path)));
    }

    public Route endpointStreamVideo(){
        return concat(
                get(() ->
                        path("index", () ->
                                getFromResource("templates/test.html"))
                ),
                get(() ->
                        path(PathMatchers.segment("api").slash("files").slash("default"), () ->
                                optionalHeaderValueByName("Range", header -> {
                                    System.out.println("header is present: " + header.isPresent());
                                    if(header.isPresent()){
                                        return complete(createStream(header.get())); //200
                                    }else{
                                        return complete(StatusCodes.RANGE_NOT_SATISFIABLE); //416
                                    }
                                })
                        )
                )
        );
    }

    private HttpResponse createStream(String rangeHeader){
        var path = "./src/main/resources/videos/video1.mp4";

        System.out.println(">>> File exist: " + Files.exists(Path.of(path)));

        var file = new File(path);
        var fileSize = file.length();
        var range = Optional.ofNullable(rangeHeader.trim().replaceAll("bytes=", "").split("-")[0]);
        var start = Long.parseLong(range.orElse("0"));
        var end = fileSize - 1;

        var headerList = new ArrayList<HttpHeader>();
        headerList.add(RawHeader.create("Content-Range", "bytes " + (start - end / fileSize)));
        headerList.add(RawHeader.create("Accept-Ranges", "bytes"));

        var fileSource = FileIO.fromPath(file.toPath(), 1024, start); //производитель

        var responseEntity = HttpEntities.create(ContentTypes.APPLICATION_OCTET_STREAM, fileSource);
        return HttpResponse.create().withStatus(StatusCodes.PARTIAL_CONTENT).addHeaders(headerList).withEntity(responseEntity);
    }

}
