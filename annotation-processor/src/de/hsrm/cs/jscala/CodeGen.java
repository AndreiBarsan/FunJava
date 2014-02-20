package de.hsrm.cs.jscala;

import de.hsrm.cs.jscala.helpers.Dbg;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import javax.annotation.processing.Filer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

/**
 * Created by Andrei Barsan on 20.02.2014, based on code by Prof. Dr. Sven Eric Panitz.
 * Handles generic code generation routines.
 */
public class CodeGen {
    public static final String TEMPLATE_BASE = "/templates/";

    public static void generate(ADT theType, Filer filer, VelocityContext context, String templateName, String fileName) {
        try (Reader reader = new InputStreamReader(
                Constructor.class.getResourceAsStream(TEMPLATE_BASE + templateName)
        )) {
            Writer fileWriter = filer.createSourceFile(fileName).openWriter();
            Velocity.evaluate(context, fileWriter, "annotation-processing", reader);
            // IMPORTANT! Otherwise, it will not get parsed!
            fileWriter.close();
        } catch(Exception e) {
            // Something went wrong
            Dbg.print(theType.env, "Something went wrong working with Velocity...");
            Dbg.printException(theType.env, e);
        }
    }
}
