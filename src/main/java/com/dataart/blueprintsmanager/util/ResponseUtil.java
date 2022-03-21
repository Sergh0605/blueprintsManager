package com.dataart.blueprintsmanager.util;

import com.dataart.blueprintsmanager.exceptions.CustomApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseUtil {
    public static void getFile(HttpServletResponse response, byte[] documentInPdf, String documentFileName) {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(documentFileName, StandardCharsets.UTF_8)
                .build();
        response.setHeader(headerKey, contentDisposition.toString());
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(documentInPdf);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CustomApplicationException("Broken file for download");
        }
    }
}
