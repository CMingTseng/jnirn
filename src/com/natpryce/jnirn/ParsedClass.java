package com.natpryce.jnirn;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ParsedClass {
    private final Obfuscatable<String> className;
    public final File file;
    public final List<ParsedMethod> nativeMethods;
    public final List<ParsedMethod> callbackMethods;

    public ParsedClass(String name, File file, List<ParsedMethod> methods) {
        this.className = Obfuscatable.of(name);
        this.file = file;
        this.nativeMethods = newArrayList(Iterables.filter(methods, isNative()));
        this.callbackMethods = newArrayList(Iterables.filter(methods, isCallback()));
    }

    private Predicate<ParsedMethod> isNative() {
        return new Predicate<ParsedMethod>() {
            @Override
            public boolean apply(ParsedMethod input) {
                return input.isNative;
            }
        };
    }

    private Predicate<ParsedMethod> isCallback() {
        return new Predicate<ParsedMethod>() {
            @Override
            public boolean apply(ParsedMethod input) {
                return input.isCalledBack;
            }
        };
    }


    public static final Function<ParsedClass, File> toFile = new Function<ParsedClass, File>() {
        @Override
        public File apply(ParsedClass input) {
            return input.file;
        }
    };

    public static Predicate<? super ParsedClass> hasNativeMethods = new Predicate<ParsedClass>() {
        @Override
        public boolean apply(ParsedClass c) {
            return !c.nativeMethods.isEmpty();
        }
    };

    public static Predicate<? super ParsedClass> hasCallbackMethods = new Predicate<ParsedClass>() {
        @Override
        public boolean apply(ParsedClass c) {
            return !c.callbackMethods.isEmpty();
        }
    };

    public String cclass() {
        return className.inSource.replace("/", "_");
    }
}
