package com.jdbc.util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SelfClassLoader extends ClassLoader{

    private String projectBase;

    public SelfClassLoader(String projectBase) {
        this.projectBase = projectBase;
    }

    @Override
    protected Class<?> findClass(String completeClassName) throws ClassNotFoundException {
        String realPath = "file:///" + projectBase.replace("\\", "/")
                .replace(" ", "%20") + "/" + completeClassName
                .replace(".", "/") + ".class";
        System.out.println(realPath);
        byte[] classBytes = null;
        try {
            Path path = Paths.get(new URI(realPath));
            classBytes = Files.readAllBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert classBytes != null;
        return defineClass(completeClassName, classBytes, 0, classBytes.length);
    }
}
