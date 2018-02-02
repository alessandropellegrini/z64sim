/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.actions.Openable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "org.z64sim.editor.NewFileAction"
)
@ActionRegistration(
        iconBase = "org/z64sim/editor/new_icon.png",
        displayName = "#CTL_NewFileAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 0),
    @ActionReference(path = "Toolbars/File", position = 0),
    @ActionReference(path = "Shortcuts", name = "D-N")
})
@Messages("CTL_NewFileAction=New")
public final class NewFileAction implements ActionListener {

    private static final AtomicInteger _integer = new AtomicInteger(0);

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            DataObject gdo = getDataObject();
            Openable openable = gdo.getLookup().lookup(Openable.class);
            openable.open();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected DataObject getDataObject() throws DataObjectNotFoundException, IOException {
        String templateName = getTemplate();
        FileObject fo = FileUtil.getConfigRoot().getFileObject(templateName);
        DataObject template = DataObject.find(fo);
        FileSystem memFS = FileUtil.createMemoryFileSystem();
        FileObject root = memFS.getRoot();
        DataFolder dataFolder = DataFolder.findFolder(root);
        DataObject gdo = template.createFromTemplate(
               dataFolder,
               "New Document" + " (" + getNextCount() + ")");
        return gdo;
    }

    protected String getTemplate() {
        return "Templates/Other/z64Template.z64";
    }

    private static int getNextCount() {
        return _integer.incrementAndGet();
    }

}
