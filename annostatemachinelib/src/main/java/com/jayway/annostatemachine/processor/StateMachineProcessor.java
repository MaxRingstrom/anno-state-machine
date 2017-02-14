package com.jayway.annostatemachine.processor;


import com.jayway.annostatemachine.Constants;
import com.jayway.annostatemachine.annotations.State;
import com.jayway.annostatemachine.annotations.StateMachine;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.jayway.annostatemachine.annotations.StateMachine")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StateMachineProcessor extends AbstractProcessor {
    private static final String TAG = StateMachineProcessor.class.getSimpleName();
    private static final String NEWLINE = "\n\n";
    private static final String GENERATED_FILE_SUFFIX = "Impl";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StateMachine.class)) {
            if (element.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Statemachine found: " + element.getSimpleName());
                generateStateMachine(element, roundEnv);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non class using " + StateMachine.class.getSimpleName() + " annotation");
            }
        }
        return true;
    }

    private void generateStateMachine(Element element, RoundEnvironment roundEnv) {
        String packageOfStateMachineSource = ((TypeElement)element).getQualifiedName().toString();

        String generatedPackage = Constants.libPackageName + ".generated";
        String generatedClassName = element.getSimpleName() + GENERATED_FILE_SUFFIX;
        String generatedClassFullPath = generatedPackage + "." + generatedClassName;

        JavaFileObject source = null;
        try {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generating state machine implementation for " + element.getSimpleName());
            source = processingEnv.getFiler().createSourceFile(generatedClassFullPath);
            try (Writer writer = source.openWriter(); JavaWriter javaWriter = new JavaWriter(writer)) {

                StringBuilder sb = new StringBuilder();

                javaWriter.emitPackage(generatedPackage);
                javaWriter.emitImports(packageOfStateMachineSource);
                javaWriter.emitEmptyLine();
                javaWriter.beginType(generatedClassName, "class", EnumSet.of(Modifier.PUBLIC), element.getSimpleName().toString());

                generateStates(element, writer, javaWriter);

                // End class
                javaWriter.emitEmptyLine();
                javaWriter.endType();

                writer.close();
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Couldn't create generated state machine class: " + generatedClassName);
            e.printStackTrace();
        }
    }

    private void generateStates(Element element, Writer writer, JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitSingleLineComment("--- State list ---");
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getAnnotation(State.class) == null) {
                // Not a state
            } else if (!(enclosedElement.getKind() == ElementKind.METHOD)) {
                // State annotation on something other than a method
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non method " + enclosedElement.getSimpleName() + " using annotation " + State.class.getSimpleName());
            } else {
                javaWriter.emitSingleLineComment(enclosedElement.getSimpleName().toString());
            }
        }
        javaWriter.emitSingleLineComment("--- End of state list ---");
    }
}