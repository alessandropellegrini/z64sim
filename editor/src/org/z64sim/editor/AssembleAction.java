/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.z64sim.assembler.Assembler;
import org.z64sim.assembler.ParseException;
import org.z64sim.memory.Memory;

@ActionID(
        category = "AsmFile",
        id = "org.z64sim.editor.AssembleAction"
)
@ActionRegistration(
        iconBase = "org/z64sim/editor/assemble_icon.png",
        displayName = "#CTL_AssembleAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Run", position = -99),
    @ActionReference(path = "Toolbars/Build", position = -99),
    @ActionReference(path = "Editors/text/z64asm/Popup", position = 400),
    @ActionReference(path = "Shortcuts", name = "F6")
})
@Messages("CTL_AssembleAction=Assemble")
public final class AssembleAction implements ActionListener {

    private final EditorCookie context;

    public AssembleAction(EditorCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        boolean correct = true;

        // Get the document and the data object associated with the current context
        StyledDocument document = this.context.getDocument();
        DataObject dataObject = NbEditorUtilities.getDataObject(document);

        String text;
        try {
            text = document.getText(0, document.getEndPosition().getOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return; // TODO: forse qui si dovrebbe mostrare un errore all'utente
        }

        // Give a name to the tab
        String name = dataObject.getName();
        InputOutput io = IOProvider.getDefault().getIO ("Assemblying " + name, false);

        try {
            io.getOut().reset();
            io.getErr().reset();
        } catch (IOException ex) {
        }

        io.select();

        // Try to assemble the code
        Reader reader = new StringReader(text);
        Assembler assembler = new Assembler(reader);
        try {
            assembler.Program();
        } catch (ParseException ex) {
        }

        // Were there errors?
        if(!assembler.getSyntaxErrors().isEmpty()) {
            correct = false;

            // Show them all
            Iterator<ParseException> iterator = assembler.getSyntaxErrors().iterator();
            while(iterator.hasNext()) {
                io.getOut().println(iterator.next().getMessage());
            }
        }

        if(correct) {
            io.getOut().println("Program " + name + " has been assembled correctly.");

            // Show the memory map
            Memory.redrawMemory();

            // Bind the program to the simulators

        } else {
            io.getErr().println("Assemblying of " + name + " failed.");
            io.getErr().close();
        }

        io.getOut().close();
    }
}
