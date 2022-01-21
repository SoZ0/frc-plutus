package ca.sozoservers.dev.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

public class FileManager {

    private static Map<String, String> env = new HashMap<>();

    public static URL getResource(String resourceName){
        URL url = FileManager.class.getClassLoader().getResource("resources/"+resourceName);
        if(url == null){
            url = FileManager.class.getClassLoader().getResource(resourceName);
        }
        return url;
    }
    
    public static InputStream getResourceAsStream(String resourceName){
        InputStream stream = FileManager.class.getClassLoader().getResourceAsStream("resources/"+resourceName);
        if(stream == null){
            stream = FileManager.class.getClassLoader().getResourceAsStream(resourceName);
        }
        return stream;
    }

    public static  HashMap<String,File> getFilesInDirectory(String resourceName){
        HashMap<String, File> files = new HashMap<>();
        try {
            URI uri = FileManager.getResource(resourceName).toURI();
            String[] array = uri.toString().split("!");
            Stream<Path> paths;
            if(array[0].contains("jar:")){
                FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
                Path fileSystemPath = fs.getPath(array[1]);
                paths = Files.walk(fileSystemPath).filter(arg -> !Files.isDirectory(arg));
                fs.close();
            }else{
                paths = Files.walk(Paths.get(uri)).filter(arg -> !Files.isDirectory(arg));
            }
            paths.forEach(path -> {
                try {
                    InputStream in = Files.newInputStream(path);
                    String fileName = path.getFileName().toString();
                    String name = fileName.substring(0, fileName.lastIndexOf("."));
                    String type = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                    File tempFile = File.createTempFile(name, type);
                    tempFile.deleteOnExit();
                    try (FileOutputStream out = new FileOutputStream(tempFile))
                    {
                        IOUtils.copy(in, out);
                    }
                    files.put(fileName,tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return files;
    }
}
