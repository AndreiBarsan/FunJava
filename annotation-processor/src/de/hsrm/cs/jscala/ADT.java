package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.helpers.Dbg;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
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
    String simpleName;
    String thePackage;

    public List<Constructor> constructors = new ArrayList<>();
    TypeElement typeElement;
    final Filer filer;
    private ProcessingEnvironment env;

    public ADT(TypeElement typeElement, ProcessingEnvironment env) {
        fullName = typeElement.getQualifiedName().toString();
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing type: " + fullName);
        if(fullName.contains(".")) {
            simpleName = fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length());
            // TODO: Will be buggy with nested classes!
            thePackage = fullName.substring(0, fullName.lastIndexOf('.'));
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

