package br.com.aspenmc.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {

    public static File createFile(String fileName, String path, boolean createIfNotExists) throws Exception {
        File file = new File(path + File.separatorChar + fileName);

        System.out.println(file);

        if (!file.exists()) {
            URL url = FileUtils.class.getClassLoader().getResource(fileName);

            if (url == null) {
                return null;
            }

            System.out.println(url);

            while (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            boolean created = file.createNewFile();

            if (created && createIfNotExists) {
                URI uri = url.toURI();
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                FileSystem zipfs = FileSystems.newFileSystem(uri, env);
                Path resourceFile = Paths.get(uri);
                Files.copy(resourceFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zipfs.close();
            }
        }

        return file;
    }
}
