/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import java.util.Set;
import javax.swing.JEditorPane;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall implements LookupListener {

    Result<z64docDataObject> asmFileResults;
    Set<z64docDataObject> uniqueAsmFiles = EditorUtilities.uniqueAsmFiles;

    @Override
    public void restored() {
        asmFileResults = Utilities.actionsGlobalContext().lookupResult(z64docDataObject.class);
        asmFileResults.addLookupListener(this);
        JEditorPane.registerEditorKitForContentType("text/z64asm", "org.z64sim.editor.highlighter.z64SyntaxHighlighter");
    }

    @Override
    public void resultChanged(LookupEvent le) {
        if (asmFileResults.allInstances().iterator().hasNext()) {
            z64docDataObject f = asmFileResults.allInstances().iterator().next();
            if (uniqueAsmFiles.add(f)) {
                EditorTopComponent cetc = new EditorTopComponent(f);
                cetc.open();
                cetc.requestActive();
            } else { // In this case, the TopComponent is already open, but needs to become active:
                for (TopComponent tc : WindowManager.getDefault().findMode("editor").getTopComponents()){
                    if (tc.getName().contains(f.getName())){
                        tc.requestActive();
                    }
                }
            }
        }
    }


}
