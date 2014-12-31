/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "AsmFile",
        id = "org.z64sim.editor.OpenAsmFileActionListener"
)
@ActionRegistration(
        iconBase = "org/z64sim/editor/open.png",
        displayName = "#CTL_OpenAsmFileActionListener"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = -90),
    @ActionReference(path = "Toolbars/File", position = 300),
    @ActionReference(path = "Shortcuts", name = "D-O")
})
@Messages("CTL_OpenAsmFileActionListener=Open Asm File")
public final class OpenAsmFileAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooserBuilder fcb = new FileChooserBuilder(org.z64sim.editor.OpenAsmFileAction.class);
        fcb.setApproveText("Open");
        fcb.setFileFilter(new z64FileFilter());

        JFileChooser jfc = fcb.createFileChooser();

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = jfc.getSelectedFile();
                FileObject foSelectedFile = FileUtil.toFileObject(file);

                DataObject obj = DataObject.find(foSelectedFile);
                EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);

                if (ec != null) {
                    ec.open();
                }
            } catch (DataObjectNotFoundException donfe) {
            }
        }
    }

    private final class z64FileFilter extends FileFilter {

        @Override
        public boolean accept(File pathname) {

            if (pathname.isDirectory()) {
                return true;
            }

            String[] path = pathname.getPath().split("\\.");
            return path[path.length - 1].equalsIgnoreCase("z64");
        }

        @Override
        public String getDescription() {
            return "z64 Assembly Files";
        }

    }
    
}
