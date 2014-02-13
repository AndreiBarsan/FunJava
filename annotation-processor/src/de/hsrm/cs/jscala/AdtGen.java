package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.annotations.Ctor;
import de.hsrm.cs.jscala.annotations.Data;
import de.hsrm.cs.jscala.helpers.Dbg;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 * Heart of the annotation processor.
 * Requires Java 8.
 */
@SupportedAnnotationTypes("Data")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AdtGen extends AbstractProcessor {

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processor init called.");
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager m = processingEnv.getMessager();
        m.printMessage(Diagnostic.Kind.NOTE, "Called process!");

        Set<? extends Element> els = roundEnv.getElementsAnnotatedWith(Data.class);
        for(Element e : els) {
            m.printMessage(Diagnostic.Kind.NOTE, "Found element implementing Data: " + e.getSimpleName());
            if(!(e instanceof  TypeElement)) {
                m.printMessage(Diagnostic.Kind.ERROR, "@Data can only be used on classes.");
                System.exit(-1);
            }

            try {
                TypeElement te = (TypeElement) e;
                ADT adt = new ADT(te, processingEnv);
                for(Element el : te.getEnclosedElements()) {
                    if(el instanceof ExecutableElement) {
                        ExecutableElement method = (ExecutableElement) el;
                        m.printMessage(Diagnostic.Kind.NOTE, "Found method \"" + method.getSimpleName() + "\".");
                        if (null != el.getAnnotation(Ctor.class)) {
                            adt.addCtor(method.getSimpleName().toString(), method.getParameters());
                        }
                    }
                }

                adt.generateClasses();
            } catch(Exception exception) {
                Dbg.printException(processingEnv, exception);
            }
        }

        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return null;
    }
}
