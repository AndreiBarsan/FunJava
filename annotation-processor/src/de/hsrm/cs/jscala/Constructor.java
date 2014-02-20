package de.hsrm.cs.jscala;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import de.hsrm.cs.jscala.helpers.StringUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.hsrm.cs.jscala.helpers.Dbg;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Responsible for the generation of individual classes for each case.
 */
public class Constructor {

    String name;
    List<? extends VariableElement> params;

    public Constructor(String name, List<? extends VariableElement> params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public List<? extends  VariableElement> getParams() {
        return params;
    }

    public void generateClass(ADT theType, Filer filer) throws IOException {
        VelocityContext context = theType.getTemplateContext();
        context.put("name", this.name);
        context.put("fields", this.params);

        CodeGen.generate(theType, filer, context, "CaseClass.vm", theType.thePackage + "." + this.name);
    }
}
