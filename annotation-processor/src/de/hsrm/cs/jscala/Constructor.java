package de.hsrm.cs.jscala;

import javax.annotation.processing.Filer;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Responsible for the generation of individual classes for each case.
 */
class Constructor {

    String name;
    List<? extends VariableElement> params;

    public Constructor(String name, List<? extends VariableElement> params) {
        this.name = name;
        this.params = params;
    }

    public void generateClass(ADT theType, Filer filer) throws IOException {
        Writer out = filer.createSourceFile(theType.thePackage + "." + name).openWriter();
        out.write(theType.getPackageDef());
        out.write("public class ");
        out.write(name);
        out.write(theType.getParamList());
        out.write(" extends ");
        out.write(theType.getSimpleName() + "Adt" + theType.getParamList());
        out.write("{\n");

        mkFields(out);
        mkConstructor(out);
        mkGetterMethods(out);
        mkSetterMethods(out);
        mkWelcomeMethod(theType, out);
        mkToStringMethod(out);
        mkEqualsMethod(out);
        mkIteratorMethod(out);
        out.write("}\n");
        out.close();
    }

    private void mkFields(Writer out) throws IOException {
        for (VariableElement param : params) {
            out.write("  private ");
            out.write(param.asType().toString() + " ");
            out.write(param.getSimpleName().toString());
            out.write(";\n");
        }
    }

    private void mkConstructor(Writer out) throws IOException {
        out.write("\n  public " + name + "(");
        boolean first = true;
        for (VariableElement p : params) {
            if (!first) {
                out.write(",");
            }
            out.write(p.asType().toString() + " ");
            out.write(p.getSimpleName().toString());
            first = false;
        }
        out.write("){\n");
        for (VariableElement p : params) {
            out.write("    this." + p.getSimpleName() + " = ");
            out.write(p.getSimpleName() + ";\n");
        }
        out.write("  }\n\n");
    }

    private void mkGetterMethods(Writer out) throws IOException {
        for (VariableElement p : params) {
            out.write("  public ");
            out.write(p.asType().toString());
            out.write(" get");
            out.write(Character.toUpperCase(p.getSimpleName().charAt(0)));
            out.write(p.getSimpleName().toString().substring(1));
            out.write("(){return " + p.getSimpleName() + ";}\n");
        }
    }

    private void mkSetterMethods(Writer out) throws IOException {
        for (VariableElement p : params) {
            out.write("  public void set");
            out.write(Character.toUpperCase(p.getSimpleName().charAt(0)));
            out.write(p.getSimpleName().toString().substring(1));
            out.write("(");
            out.write(p.asType().toString());
            out.write(" ");
            out.write(p.getSimpleName().toString());
            out.write("){this." + p.getSimpleName());
            out.write("= " + p.getSimpleName() + ";}\n");
        }
    }

    private void mkWelcomeMethod(ADT theType, Writer out)
            throws IOException {
        out.write("  public <_b> _b welcome("
                + theType.simpleName + "Visitor<" + theType.commaSeparatedTypeParams()
                + (theType.commaSeparatedTypeParams().length() == 0 ? "" : ",")
                + "_b> visitor){"
                + "\n    return visitor.visit(this);\n  }\n");
    }

    private void mkToStringMethod(Writer out) throws IOException {
        out.write("  public String toString(){\n");
        out.write("    return \"" + name + "(\"");
        boolean first = true;
        for (VariableElement p : params) {
            if (first) {
                first = false;
            } else out.write("+\",\"");
            out.write("+" + p.getSimpleName().toString());
        }
        out.write("+\")\";\n  }\n");
    }

    private void mkEqualsMethod(Writer out) throws IOException {
        out.write("  public boolean equals(Object other){\n");
        out.write("    if (!(other instanceof " + name + ")) ");
        out.write("return false;\n");
        out.write("    final " + name + " o= (" + name + ") other;\n");
        out.write("    return true  ");
        for (VariableElement p : params) {
            out.write("&& " + p.getSimpleName()
                    + ".equals(o." + p.getSimpleName() + ")");
        }
        out.write(";\n  }\n");
    }

    private void mkIteratorMethod(Writer out) throws IOException {
        out.write("  public java.util.Iterator<Object> iterator(){");
        out.write("\n    java.util.List<Object> res\n");
        out.write("         =new java.util.ArrayList<Object>();\n");
        for (VariableElement p : params) {
            out.write("    res.add(" + p.getSimpleName() + ");\n");
        }
        out.write("    return res.iterator();\n");
        out.write("  }\n");
    }

    public String mkVisitMethod(ADT theType) {
        return "public abstract result visit("
                + name + theType.getParamList() + " _);";
    }
}
