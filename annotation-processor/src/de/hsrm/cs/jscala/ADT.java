package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.helpers.Dbg;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
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

    private String getFullName() {
        return fullName;
    }

    private String getSimpleName() {
        return simpleName;
    }

    private String commaSepPs(){
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
                sb.append(e.asType().toString());
            }

            return sb.toString();
        }
    }

    private String getParamList() {
        String pars = commaSepPs();
        return pars.length() == 0 ? "" : "<" + pars + ">";
    }

    public String getPackageDef() {
        return thePackage.length() == 0 ? "" : "package " + thePackage + ";\n\n";
    }

    public void addConstr(String name, List<? extends VariableElement> parameters) {
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
                c.generateClass(this);
            }
            env.getMessager().printMessage(Diagnostic.Kind.NOTE, "Done generating!");
        } catch (Exception e) {
            Dbg.printException(env, e);
        }
    }

    private void generateVisitorClass() throws Exception {
        final String csName = simpleName + "Visitor";
        final String fullName = csName + "<" + commaSepPs() + (commaSepPs().length() == 0 ? "" : ",") +"result>";

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
        out.write(getSimpleName()+"Adt"+getParamList());
        out.write(" extends "+fullName+"\n");
        out.write(" implements Iterable<Object>{\n");

        out.write("  abstract public <b_> b_ welcome("
                + simpleName + "Visitor<" + commaSepPs()
                +(commaSepPs().length()==0?"":",")
                +"b_> visitor);\n");
        out.write("}");
        out.close();
    }

    /**
     * Responsible for the generation of individual classes for each case.
     */
    private class Constructor {

        String name;
        List<? extends VariableElement> params;

        public Constructor(String name, List<? extends VariableElement> params){
            this.name = name;
            this.params = params;
        }

        public void generateClass(ADT theType){
            try{
                Writer out = filer.createSourceFile(theType.thePackage+"."+name).openWriter();
                out.write(theType.getPackageDef());
                out.write("public class ");
                out.write(name);
                out.write(theType.getParamList());
                out.write(" extends ");
                out.write(theType.getSimpleName()+"Adt"+theType.getParamList());
                out.write("{\n");

                mkFields(out);
                mkConstructor(out);
                mkGetterMethods( out);
                mkSetterMethods( out);
                mkWelcomeMethod(theType, out);
                mkToStringMethod(out);
                mkEqualsMethod(out);
                mkIteratorMethod(out);
                out.write("}\n");
                out.close();
            }catch (Exception e){}
        }

        private void mkFields(Writer out)throws IOException {
            for (VariableElement param : params){
                out.write("  private ");
                out.write(param.asType().toString()+" ");
                out.write(param.getSimpleName().toString());
                out.write(";\n");
            }
        }

        private void mkConstructor(Writer out)throws IOException{
            out.write("\n  public "+name+"(");
            boolean first = true;
            for (VariableElement p : params){
                if (!first){out.write(",");}
                out.write(p.asType().toString()+" ");
                out.write(p.getSimpleName().toString());
                first=false;
            }
            out.write("){\n");
            for (VariableElement p : params){
                out.write("    this."+p.getSimpleName()+" = ");
                out.write(p.getSimpleName()+";\n");
            }
            out.write("  }\n\n");
        }

        private void mkGetterMethods(Writer out)throws IOException{
            for (VariableElement p:params){
                out.write("  public ");
                out.write(p.asType().toString());
                out.write(" get");
                out.write(
                        Character.toUpperCase(p.getSimpleName().charAt(0)));
                out.write(p.getSimpleName().toString().substring(1));
                out.write("(){return "+p.getSimpleName() +";}\n");
            }
        }

        private void mkSetterMethods(Writer out)throws IOException{
            for (VariableElement p : params){
                out.write("  public void set");
                out.write(
                        Character.toUpperCase(p.getSimpleName().charAt(0)));
                out.write(p.getSimpleName().toString().substring(1));
                out.write("(");
                out.write(p.asType().toString());
                out.write(" ");
                out.write(p.getSimpleName().toString());
                out.write("){this."+p.getSimpleName());
                out.write("= "+p.getSimpleName()+";}\n");
            }
        }

        private void mkWelcomeMethod(ADT theType,Writer out)
                throws IOException{
            out.write("  public <_b> _b welcome("
                    +theType.simpleName +"Visitor<"+theType.commaSepPs()
                    +(theType.commaSepPs().length()==0?"":",")
                    +"_b> visitor){"
                    +"\n    return visitor.visit(this);\n  }\n");
        }

        private void mkToStringMethod(Writer out) throws IOException{
            out.write("  public String toString(){\n");
            out.write("    return \""+name+"(\"");
            boolean first=true;
            for (VariableElement p : params){
                if (first){first=false;}
                else out.write("+\",\"");
                out.write("+"+p.getSimpleName().toString());
            }
            out.write("+\")\";\n  }\n");
        }

        private void mkEqualsMethod(Writer out) throws IOException{
            out.write("  public boolean equals(Object other){\n");
            out.write("    if (!(other instanceof "+name+")) ");
            out.write("return false;\n");
            out.write("    final "+name+" o= ("+name+") other;\n");
            out.write("    return true  ");
            for (VariableElement p : params) {
                out.write("&& "+p.getSimpleName()
                        +".equals(o."+p.getSimpleName()+")");
            }
            out.write(";\n  }\n");
        }

        private void mkIteratorMethod(Writer out) throws IOException {
            out.write("  public java.util.Iterator<Object> iterator(){");
            out.write("\n    java.util.List<Object> res\n");
            out.write("         =new java.util.ArrayList<Object>();\n");
            for (VariableElement p : params){
                out.write("    res.add("+p.getSimpleName()+");\n");
            }
            out.write("    return res.iterator();\n");
            out.write("  }\n");
        }

        public String mkVisitMethod(ADT theType){
            return "public abstract result visit("
                    +name+theType.getParamList()+" _);";
        }
    }
}

