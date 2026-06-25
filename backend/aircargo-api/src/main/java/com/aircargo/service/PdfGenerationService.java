package com.aircargo.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    static {
        XRLog.setLoggingEnabled(false);
    }

    public byte[] generatePdf(String html) {
        List<File> tempFiles = new ArrayList<>();
        try {
            String processed = replaceDataUrisWithFiles(html, tempFiles);
            return renderPdf(processed);
        } finally {
            for (File f : tempFiles) {
                try { f.delete(); } catch (Exception ignored) {}
            }
        }
    }

    private String replaceDataUrisWithFiles(String html, List<File> tempFiles) {
        Pattern pattern = Pattern.compile("src='(data:[^']+)'", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String dataUri = m.group(1);
            try {
                String base64Data = dataUri.substring(dataUri.indexOf(',') + 1);
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                String ext = dataUri.contains("png") ? ".png" : dataUri.contains("jpeg") || dataUri.contains("jpg") ? ".jpg" : ".png";
                File tempFile = Files.createTempFile("pdfimg_", ext).toFile();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(imageBytes);
                }
                tempFiles.add(tempFile);
                String filePath = tempFile.getAbsolutePath();
                if (File.separatorChar == '\\') {
                    filePath = filePath.replace("\\", "/");
                }
                m.appendReplacement(sb, "src='file:///" + filePath + "'");
            } catch (Exception e) {
                log.warn("Could not decode data URI ({} bytes): {}", dataUri.length(), e.getMessage());
                m.appendReplacement(sb, "src=''");
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            byte[] pdf = os.toByteArray();
            if (pdf.length == 0) {
                throw new RuntimeException("PDF generado vacío");
            }
            return pdf;
        } catch (Exception e) {
            log.error("Error generando PDF ({} chars HTML): {}", html.length(), e.getMessage(), e);
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }
}
