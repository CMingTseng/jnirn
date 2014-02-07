package com.natpryce.jnirn.analysis;

import com.google.common.collect.ImmutableSet;
import com.natpryce.jnirn.Codebase;
import com.natpryce.jnirn.ParsedClass;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.util.Enumeration;
import java.util.SortedMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.google.common.collect.Maps.newTreeMap;

public class JavaBytecodeParser {
    private final ImmutableSet<String> callbackAnnotations;
    private final ImmutableSet<String> instantiatedAnnotations;
    private final ImmutableSet<String> instantiatedClasses;
    private final SortedMap<String, ParsedClass> classesByName = newTreeMap();

    public JavaBytecodeParser(ImmutableSet<String> callbackAnnotations, ImmutableSet<String> instantiatedAnnotations, ImmutableSet<String> instantiatedClasses) {
        this.callbackAnnotations = callbackAnnotations;
        this.instantiatedAnnotations = instantiatedAnnotations;
        this.instantiatedClasses = instantiatedClasses;
    }

    public void parse(File file) throws IOException {
        if (file.isDirectory()) {
            parseDirectoryContents(file);
        }
        else if (file.getName().endsWith(".jar")) {
            parseJAR(file);
        }
        else {
            throw new IOException("cannot parse " + file);
        }
    }

    private void parseDirectoryContents(File dir) throws IOException {
        for (File entry : dir.listFiles(onlyPackageOrClass())) {
            if (entry.isDirectory()) {
                parseDirectoryContents(entry);
            }
            else {
                parseClassFile(entry);
            }
        }
    }

    private FileFilter onlyPackageOrClass() {
        return new FileFilter() {
            @Override
            public boolean accept(File child) {
                return child.isDirectory() || child.getName().endsWith(".class");
            }
        };
    }

    public void parseJAR(File file) throws IOException {
        JarFile jarFile = new JarFile(file);

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                InputStream in = jarFile.getInputStream(jarEntry);
                try {
                    parseClassBytecode(file, in);
                } finally {
                    in.close();
                }
            }
        }
    }

    private void parseClassFile(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            parseClassBytecode(file, in);
        }
        finally {
            in.close();
        }
    }

    private void parseClassBytecode(File file, InputStream in) throws IOException {
        ClassReader classReader = new ClassReader(in);
        ClassAnalysis analysis = new ClassAnalysis(callbackAnnotations, instantiatedClasses, instantiatedAnnotations);

        classReader.accept(analysis, ClassReader.SKIP_CODE);

        if (analysis.classInterfacesWithNativeCode()) {
            String className = classReader.getClassName();

            classesByName.put(className, new ParsedClass(className, file,
                    analysis.isInstantiatedFromNativeCode,
                    analysis.nativeMethodsByName,
                    analysis.callbackMethodsByName));
        }
    }

    public Codebase parseAll(Iterable<File> inputFiles) throws IOException {
        for (File inputFile : inputFiles) {
            parse(inputFile);
        }
        return new Codebase(classesByName.values());
    }
}
