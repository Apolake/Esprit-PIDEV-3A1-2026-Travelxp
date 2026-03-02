package com.travelxp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ImageUtil {

    private static final String UPLOAD_DIR = "uploads";

    static {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String saveImage(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }

        try {
            int nextNumber = getNextImageNumber();
            String extension = getFileExtension(sourceFile);
            String newFileName = "image_" + nextNumber + extension;
            Path destination = Path.of(UPLOAD_DIR, newFileName);

            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString(); // Stores relative path: uploads/image_n.png
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getNextImageNumber() {
        File dir = new File(UPLOAD_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith("image_") && name.contains("."));
        if (files == null || files.length == 0) {
            return 1;
        }

        int max = 0;
        for (File file : files) {
            try {
                String name = file.getName();
                int dotIndex = name.lastIndexOf('.');
                String numPart = name.substring(6, dotIndex);
                int num = Integer.parseInt(numPart);
                if (num > max) {
                    max = num;
                }
            } catch (Exception e) {
                // Ignore malformed names
            }
        }
        return max + 1;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ".png"; // Default
        }
        return name.substring(lastIndexOf);
    }
    
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) return null;
        return new File(relativePath).getAbsolutePath();
    }
}
