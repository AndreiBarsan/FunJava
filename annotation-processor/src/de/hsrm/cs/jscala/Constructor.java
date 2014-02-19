package de.hsrm.cs.jscala;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.hsrm.cs.jscala.helpers.Dbg;

import java.io.IOException;
import java.io.StringWriter;
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
        out.write(theType.getParamList(true));
        out.write(" extends ");
        out.write(theType.getSimpleName() + "Adt" + theType.getParamList(false));
        out.write("{\n");

        mkFields(out);
        mkConstructor(out);
        mkGetterMethods(out);
        mkSetterMethods(out);
        mkWelcomeMethod(out, theType);
        mkToStringMethod(out);
        mkEqualsMethod(out, theType);
        mkIteratorMethod(out);
        out.write("}\n");
        out.close();
        
        // Experimental Apache Velocity stuff
        VelocityContext context = new VelocityContext();
        context.put("parentName", theType.fullName);
        context.put("typeParamDeclaration", theType.getParamList(true));
        context.put("typeParamUsage", theType.getParamList(false));
        context.put("name", this.name);
        context.put("fields", this.params);
        Template template = Velocity.getTemplate("templates/CaseClass.vm");
        Writer w = filer.createSourceFile(theType.thePackage + "." + name + "aux").openWriter();
        template.merge(context, w);        
    }

    public static String genCaseHeader(String typeParamsShort, String typeParamsLong, String parentName, String caseName, List<? extends Element> params) {
        return genCaseHeader(typeParamsShort, typeParamsLong, parentName, caseName, params, true);
    }

    // TODO: outta here
    public static String genCaseHeader(String typeParamsShort, String typeParamsLong, String parentName, String caseName, List<? extends Element> params, boolean includeB) {
        String wrappedTypeParamsShort = typeParamsShort.length() == 0 ? "" : "<" + typeParamsShort + ">";
        String wrappedTypeParamsLong = typeParamsLong.length() == 0 ? "" : "<" + typeParamsLong + ">";
        StringBuilder sb = new StringBuilder();
        if(includeB) {
            sb.append("public static" + "<" + typeParamsLong + (typeParamsLong.length() > 0 ? ", " : "") + "B> ");
            sb.append("Function1<" + parentName + wrappedTypeParamsShort + ", Optional<B>> " + caseName + "(Function" + params.size());
        }
        else {
            sb.append("public static" + wrappedTypeParamsLong + " ");
            sb.append("Function1<" + parentName + wrappedTypeParamsShort + ", Optional<Nothing>> " + caseName + "(Consumer" + params.size());
        }

        if(params.size() > 0) {
            sb.append("<");
            boolean first = true;
            for(Element param : params) {
                if(first) {
                    first = false;
                }
                else {
                    sb.append(", ");
                }
                sb.append(param.asType().toString());
            }

            if(includeB) {
                sb.append(", B>");
            }
            else {
                sb.append(">");
            }
        }
        else {
            if(includeB) {
                sb.append("<B>");
            }
        }
        sb.append(" theCase) {\n");
        return sb.toString();
    }

    public static String genGetters(List<? extends VariableElement> params, String varName) {
        StringBuilder getters = new StringBuilder();
        boolean fg = true;
        for(VariableElement ve : params) {
            if(fg) {
                fg = false;
            }
            else {
                getters.append(", ");
            }

            String sn = ve.getSimpleName().toString();
            getters.append(varName + ".get" + Character.toUpperCase(sn.charAt(0)) + sn.substring(1) + "()");
        }

        return getters.toString();
    }

    /**
     * @param adt The ADT we're generating the case classes for
     * @return
     */
    public String genStaticCaseMethod(ADT adt) {
        StringBuilder sb = new StringBuilder();
        String typeParamsShort = adt.commaSeparatedTypeParams(false);
        String typeParamsLong = adt.commaSeparatedTypeParams(true);
        String wrappedTypeParamsShort = typeParamsShort.length() == 0 ? "" : "<" + typeParamsShort + ">";
        sb.append(genCaseHeader(typeParamsShort, typeParamsLong, adt.getSimpleName(), "case" + this.name, this.params));
        sb.append("\treturn (self) -> {\n");
        sb.append("\t\tif(! (self instanceof " + name + ")) return Optional.empty();\n");
        sb.append("\t\t" + name + wrappedTypeParamsShort + " matchedBranch = (" + name + wrappedTypeParamsShort + ") self;\n");
        sb.append("\t\treturn Optional.of(theCase.apply(" + genGetters(this.params, "matchedBranch") + "));\n");
        sb.append("\t};\n");
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Generates overloaded methods to allow void cases to be matched (think caseSomething(el -> println(el)))
     * @param adt The ADT we're generating the case classes for
     * @return
     */
    public String genStaticCaseVoidMethod(ADT adt) {
        StringBuilder sb = new StringBuilder();
        String typeParamsShort = adt.commaSeparatedTypeParams(false);
        String typeParamsLong = adt.commaSeparatedTypeParams(true);
        String wrappedTypeParamsShort = typeParamsShort.length() == 0 ? "" : "<" + typeParamsShort + ">";
        sb.append(genCaseHeader(typeParamsShort, typeParamsLong, adt.getSimpleName(), "case" + this.name + "V", this.params, false));
        sb.append("\treturn (self) -> {\n");
        sb.append("\t\tif(! (self instanceof " + name + ")) return Optional.empty();\n");
        sb.append("\t\t" + name + wrappedTypeParamsShort + " matchedBranch = (" + name + wrappedTypeParamsShort + ") self;\n");
        sb.append("\t\ttheCase.apply(" + genGetters(this.params, "matchedBranch") + "); return Optional.of(Nothing.VAL);\n");
        sb.append("\t};\n");
        sb.append("}\n");

        return sb.toString();
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

    private void mkWelcomeMethod(Writer out, ADT theType) throws IOException {
        out.write("  public <_b> _b welcome("
                + theType.simpleName + "Visitor<" + theType.commaSeparatedTypeParams(false)
                + (theType.commaSeparatedTypeParams(false).length() == 0 ? "" : ",")
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

    private void mkEqualsMethod(Writer out, ADT adt) throws IOException {
    	out.write("  @SuppressWarnings({ \"unchecked\", \"unused\" })\n");
        out.write("  public boolean equals(Object other){\n");
        out.write("    if (!(other instanceof " + name + ")) return false;\n");
        out.write("    if (null == other) return false;\n");
        out.write("    if (this == other) return true;\n");
        out.write("    final " + name + adt.getParamList(false) + " o = (" + name + adt.getParamList(false) + ") other;\n");
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
                + name + theType.getParamList(false) + " _ignore);";
    }
}
