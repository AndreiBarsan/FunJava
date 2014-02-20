package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.helpers.Dbg;
import de.hsrm.cs.jscala.helpers.StringUtil;
import org.apache.velocity.VelocityContext;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class ADT {

    /**
     * The full name of this class, complete with packages, enclosing classes and name.
     */
    String fullName;

    /**
     * The simple name of the class, without enclosing types and package.
     */
    String simpleName;
    /**
     * The enclosing type(s) of our class, without the package name. Empty if our class isn't nested.
     */
    String enclosingTypes;
    /**
     * The package our class (and, if it applies, consequently, its enclosing types) reside(s) in.
     */
    String thePackage;

    public List<Constructor> constructors = new ArrayList<>();
    TypeElement typeElement;
    final Filer filer;
    ProcessingEnvironment env;

    public ADT(TypeElement typeElement, ProcessingEnvironment env) {
        fullName = typeElement.getQualifiedName().toString();
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing type: " + fullName);

        this.typeElement = typeElement;
        this.env = env;
        this.filer = env.getFiler();

        resolveEnclosingTypes();
    }

    /**
     * Figures out if our type is nested and takes the necessary precautions in order to
     * generate correct code.
     */
    private void resolveEnclosingTypes() {
        Element enclosing  = typeElement.getEnclosingElement();
        int enclosingClasses = 0;

        if(typeElement.getNestingKind().isNested()) {
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Our type is nested. Nesting kind: " + typeElement.getNestingKind());

            while(enclosing.getKind() != ElementKind.PACKAGE) {
                enclosingClasses++;

                // TODO: check if it works with nested classes in the default package
                enclosing = enclosing.getEnclosingElement();

                if(enclosingClasses > 128) {
                    // wat
                    env.getMessager().printMessage(Diagnostic.Kind.ERROR, "Much nesting. Wow. Such overload. Much error.");
                    break;
                }
            }

            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "We have " + enclosingClasses + " class(es) above us.");
        }

        if(fullName.contains(".")) {
            // The index of the dot that separates the package name and the top wrapper class, in case this class is nested
            int psDotIndex = StringUtil.lastNthIndexOf(fullName, '.', enclosingClasses + 1);
            thePackage = fullName.substring(0, psDotIndex);
            if(typeElement.getNestingKind().isNested()) {
                enclosingTypes = fullName.substring(psDotIndex + 1, fullName.lastIndexOf("."));
            }
            else {
                enclosingTypes = "";
            }
            simpleName = fullName.substring(fullName.lastIndexOf(".") + 1, fullName.length());
        }
        else {
            simpleName = fullName;
            thePackage = "";
        }
    }

    /**
     * Returns a comma-separated list of this type's type parameters.
     * @param full Whether to return the FULL type parameter specification, e.g. <T extends Comparable<T>> or the short one (e.g. <T>).
     */
    String commaSeparatedTypeParams(boolean full){
        List<? extends TypeParameterElement> typeParams = typeElement.getTypeParameters();
        if(typeParams.isEmpty()) {
            return "";
        }
        else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(TypeParameterElement e : typeParams) {
                if(first) {
                    first = false;
                } else {
                    sb.append(",");
                }

                if(full) {
                    sb.append(StringUtil.getFullTypeParamName(e));
                }
                else {
                    sb.append(e.getSimpleName());
                }
            }

            return sb.toString();
        }
    }

    /**
     * Returns the exact name of this class' enclosing element (it's equal to thePackage if the class isn't nested).
     */
    String getEnclosingName() {
        return thePackage + (enclosingTypes.length() > 0 ? "." : "") + enclosingTypes;
    }

    public void addCtor(String name, List<? extends VariableElement> parameters) {
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Adding Ctor named \"" + name + "\".");
        constructors.add(new Constructor(name, parameters));
    }

    public void generateClasses(){
        try{
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating classes...");
            generateClass();

            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating visitor classes...");
            generateVisitorClass();

            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating case classes...");
            for (Constructor c:constructors) {
                c.generateClass(this, filer);
            }

            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating case branch static methods...");
            generateCaseBranches();

            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Done generating!");
        } catch (Exception e) {
            Dbg.printException(env, e);
        }
    }

    private void generateVisitorClass() throws Exception {
        VelocityContext context = getTemplateContext();
        context.put("constructors", constructors.stream().map(c -> c.name).iterator());
        CodeGen.generate(this, filer, context, "Visitor.vm", thePackage + "." + simpleName + "Visitor");
    }

    private void generateClass() throws IOException {
        VelocityContext context = getTemplateContext();
        context.put("fullName", fullName);
        context.put("name", simpleName);
        CodeGen.generate(this, filer, context, "Adt.vm", thePackage + "." + simpleName + "Adt");
    }

    private void generateCaseBranches() throws IOException {
        VelocityContext context = getTemplateContext();
        context.put("constructors", constructors);
        context.put("enclosingName", getEnclosingName());
        CodeGen.generate(this, filer, context, "Cases.vm", thePackage + "." + simpleName + "Cases");
    }

    /**
     * Used when generating code using Apache Velocity templates.
     */
    public VelocityContext getTemplateContext() {
        VelocityContext context = new VelocityContext();
        context.put("package", thePackage);
        context.put("parentName", simpleName);
        context.put("parentFullName", fullName);
        context.put("typeParamDeclaration", commaSeparatedTypeParams(true));
        context.put("typeParamUsage", commaSeparatedTypeParams(false));
        context.put("StringUtil", StringUtil.class);
        return context;
    }
}

