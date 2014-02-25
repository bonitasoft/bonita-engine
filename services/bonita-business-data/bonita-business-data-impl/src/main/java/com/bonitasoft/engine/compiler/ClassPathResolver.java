package com.bonitasoft.engine.compiler;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;


public class ClassPathResolver {

    public String[] getJarsPath(Class<?>... classes) {
        Set<String> list = new HashSet<String>();
       for (Class<?> clas : classes) {
           list.add(findJarPath(clas));
       }
        return list.toArray(new String[list.size()]);
    }

    private String findJarPath(Class<?> clazzToFind) {
        URL jarUrl = clazzToFind.getResource(clazzToFind.getSimpleName()+".class");
        String jarPath = jarUrl.getFile();
        if(jarPath.indexOf("!") != -1){
            jarPath = jarPath.split("!")[0];
        }
        return jarPath;
    }
}
