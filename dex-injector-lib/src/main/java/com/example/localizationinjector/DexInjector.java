
package com.example.localizationinjector;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;

public class DexInjector {

    public void inject(File dexFile, File outputDexFile) throws IOException {
        DexFile originalDexFile = DexFileFactory.loadDexFile(dexFile, 19 /*api*/);

        DexRewriter dexRewriter = new DexRewriter(new Rewriters() {
            @Nonnull
            @Override
            public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
                return new BitmapTextRewriter(this);
            }
        });

        DexFile rewrittenDexFile = dexRewriter.rewriteDexFile(originalDexFile);

        DexPool.writeTo(new FileDataStore(outputDexFile), rewrittenDexFile);
    }
}
