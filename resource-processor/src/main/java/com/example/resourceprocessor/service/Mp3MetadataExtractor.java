package com.example.resourceprocessor.service;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class Mp3MetadataExtractor {

    public Map<String, String> extract(byte[] mp3Data) {
        try (InputStream input = new ByteArrayInputStream(mp3Data)) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Mp3Parser parser = new Mp3Parser();
            parser.parse(input, handler, metadata, new ParseContext());

            Map<String, String> result = new HashMap<>();
            result.put("name", metadata.get("dc:title"));
            result.put("artist", metadata.get("xmpDM:artist"));
            result.put("album", metadata.get("xmpDM:album"));
            result.put("length", metadata.get("xmpDM:duration"));
            result.put("year", metadata.get("xmpDM:releaseDate"));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract MP3 metadata", e);
        }
    }
}