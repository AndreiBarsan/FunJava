package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.annotations.*;
import de.hsrm.cs.jscala.helpers.Dbg;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;

import org.apache.velocity.app.Velocity;

import java.util.Set;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 * Heart of the annotation processor.
 * Requires Java 8.
 */
@SupportedAnnotationTypes(value = { "*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AdtGen extends AbstractProcessor {

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Processor init called.");
        
        Velocity.init();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager m = processingEnv.getMessager();
        m.printMessage(Diagnostic.Kind.WARNING, "Called process!");

        Set<? extends Element> els;
        try {
        	els = roundEnv.getElementsAnnotatedWith(Data.class);
        }
        catch(Exception e) {
        	// This is meant to catch errors related to Data not being on the classpath
        	// They happen when the annotation-processor jar doesn't get generated properly
        	Dbg.printException(processingEnv, e);
        	return true;
        }
        for(Element e : els) {
            m.printMessage(Diagnostic.Kind.WARNING, "Found element implementing Data: " + e.getSimpleName());
            if(!(e instanceof TypeElement)) {
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
