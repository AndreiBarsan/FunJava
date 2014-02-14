package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.helpers.Dbg;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrei Barsan on 12.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 */
public class ADT {

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
    private ProcessingEnvironment env;

    // TODO: move helpers outta here
    /**
     * n == 1 means lastIndexOf
     */
    public static int lastNthIndexOf(String s, char c, int n) {
        int occ = 0;
        for(int i = s.length() - 1; i >= 0; --i) {
            if(s.charAt(i) == c) {
                occ++;
            }

            if(n == occ) {
                return i;
            }
        }

        return -1;
    }

    public static int nthIndexOf(String s, char c, int n) {
        int occ = 0;
        for(int i = 0; i < s.length(); ++i) {
            if(s.charAt(i) == c) {
                occ++;
            }

            if(n == occ) {
                return i;
            }
        }

        return -1;
    }

    public ADT(TypeElement typeElement, ProcessingEnvironment env) {
        fullName = typeElement.getQualifiedName().toString();
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing type: " + fullName);

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
            int psDotIndex = lastNthIndexOf(fullName, '.', enclosingClasses + 1);
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

        this.typeElement = typeElement;
        this.env = env;
        this.filer = env.getFiler();
    }

    String getFullName() {
        return fullName;
    }

    String getSimpleName() {
        return simpleName;
    }

    /**
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
                    sb.append(getFullTypeParamName(e));
                }
                else {
                    sb.append(e.getSimpleName());
                }
            }

            return sb.toString();
        }
    }

    private static String getFullTypeParamName(TypeParameterElement e) {
        StringBuilder sb = new StringBuilder();
        String mainType = e.asType().toString();
        sb.append(mainType);

        List<? extends TypeMirror> bounds = e.getBounds();
        if(bounds.size() == 0) {
            return sb.toString();
        }
        else {
            sb.append(" extends ");
            boolean first = true;
            for(TypeMirror tm : bounds) {
                if(first) {
                    first = false;
                } else {
                    sb.append(" & ");
                }

                sb.append(tm.toString());
            }

            return sb.toString();
        }
    }

    String getParamList(boolean full) {
        String pars = commaSeparatedTypeParams(full);
        return pars.length() == 0 ? "" : "<" + pars + ">";
    }

    String getPackageDef() {
        return thePackage.length() == 0 ? "" : "package " + thePackage + ";\n\n";
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
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Done generating!");
        } catch (Exception e) {
            Dbg.printException(env, e);
        }
    }

    private void generateVisitorClass() throws Exception {
        final String csName = simpleName + "Visitor";
        final String fTypeParams = commaSeparatedTypeParams(true);
        final String fullName = csName + "<" + fTypeParams + (fTypeParams.length() == 0 ? "" : ",") +"result>";

        Writer out = filer.createSourceFile(thePackage+"."+csName).openWriter();
        out.write(getPackageDef());
        out.write("\n\n");
        out.write("public abstract class ");
        out.write(fullName+"{\n");
        for (Constructor c:constructors)
            out.write("  "+c.mkVisitMethod(this)+"\n");

        out.write("  public result visit("+getFullName()+" xs){");
        out.write("\n    throw new RuntimeException(");
        out.write("\"unmatched pattern: \"+xs.getClass());\n");
        out.write("  }");

        out.write("}");
        out.close();
    }

    private void generateClass() throws Exception {
        final String fullName = getFullName();
        String sourceFileName = ((thePackage.length() == 0) ? "" : thePackage + ".") + simpleName + "Adt";
        Writer out = filer.createSourceFile(sourceFileName).openWriter();

        out.write( getPackageDef());
        out.write("public abstract class ");
        out.write(getSimpleName() + "Adt" + getParamList(true));
        out.write(" extends "+fullName+"\n");
        out.write(" implements Iterable<Object>{\n");

        out.write("  abstract public <b_> b_ welcome("
                + simpleName + "Visitor<" + commaSeparatedTypeParams(false)
                +(commaSeparatedTypeParams(false).length()==0?"":",")
                +"b_> visitor);\n");
        out.write("}");
        out.close();
    }

}

