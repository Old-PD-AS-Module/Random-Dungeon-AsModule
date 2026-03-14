
package com.example.localizationinjector;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.rewriter.ClassDefRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BitmapTextRewriter extends ClassDefRewriter {

    public BitmapTextRewriter(Rewriters rewriters) {
        super(rewriters);
    }

    @Nonnull
    @Override
    public ClassDef rewrite(@Nonnull ClassDef classDef) {
        if (classDef.getType().equals("Lcom/lh64/noosa/BitmapText;")) {
            List<Method> newMethods = new ArrayList<>();
            for (Method method : classDef.getMethods()) {
                if (method.getName().equals("text") && method.getParameters().size() == 1) {
                    newMethods.add(rewriteTextMethod(method));
                } else {
                    newMethods.add(method);
                }
            }
            return new org.jf.dexlib2.immutable.ImmutableClassDef(
                    classDef.getType(),
                    classDef.getAccessFlags(),
                    classDef.getSuperclass(),
                    classDef.getInterfaces(),
                    classDef.getSourceFile(),
                    classDef.getAnnotations(),
                    classDef.getFields(),
                    newMethods
            );
        }
        return super.rewrite(classDef);
    }

    private Method rewriteTextMethod(Method method) {
        MutableMethodImplementation newImplementation = new MutableMethodImplementation(method.getImplementation());

        // Create the instructions to call LocalizationManager.getInstance().getString(str)
        List<Instruction> newInstructions = new ArrayList<>();

        // new-instance v0, Lcom/lh64/noosa/LocalizationManager;
        newInstructions.add(new BuilderInstruction21c(Opcode.NEW_INSTANCE, 0, new ImmutableMethodReference(
                "Lcom/lh64/noosa/LocalizationManager;",
                "<init>",
                new ImmutableMethodProtoReference(null, null),
                0, // accessFlags
                null // annotations
        )));

        // invoke-direct {v0}, Lcom/lh64/noosa/LocalizationManager;-><init>()V
        newInstructions.add(new org.jf.dexlib2.builder.instruction.BuilderInstruction35c(
                Opcode.INVOKE_DIRECT,
                1, // registerCount
                0, // registerC
                0, // registerD
                0, // registerE
                0, // registerF
                0, // registerG
                new ImmutableMethodReference(
                        "Lcom/lh64/noosa/LocalizationManager;",
                        "<init>",
                        new ImmutableMethodProtoReference(null, null),
                        0, // accessFlags
                        null // annotations
                )
        ));

        // invoke-virtual {v0, p0}, Lcom/lh64/noosa/LocalizationManager;->getString(Ljava/lang/String;)Ljava/lang/String;
        newInstructions.add(new org.jf.dexlib2.builder.instruction.BuilderInstruction35c(
                Opcode.INVOKE_VIRTUAL,
                2, // registerCount
                0, // registerC
                1, // registerD (p0)
                0, // registerE
                0, // registerF
                0, // registerG
                new ImmutableMethodReference(
                        "Lcom/lh64/noosa/LocalizationManager;",
                        "getString",
                        new ImmutableMethodProtoReference(
                                new StringReference("Ljava/lang/String;"),
                                new StringReference("Ljava/lang/String;")
                        ),
                        0, // accessFlags
                        null // annotations
                )
        ));

        // move-result-object p0
        newInstructions.add(new org.jf.dexlib2.builder.instruction.BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));

        newImplementation.addInstructions(0, newInstructions);

        return new ImmutableMethod(
                method.getDefiningClass(),
                method.getName(),
                method.getParameters(),
                method.getReturnType(),
                method.getAccessFlags(),
                method.getAnnotations(),
                newImplementation
        );
    }
}
