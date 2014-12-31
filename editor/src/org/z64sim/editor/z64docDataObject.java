/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;

@Messages({
    "LBL_z64doc_LOADER=z64 Assembly File"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_z64doc_LOADER",
        mimeType = "text/z64asm",
        extension = {"z64", "Z64", "asm"}
)
@DataObject.Registration(
        mimeType = "text/z64asm",
        iconBase = "org/z64sim/editor/z64doc.png",
        displayName = "#LBL_z64doc_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.z64sim.editor.OpenAsmFileAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "Edit", id = "org.z64sim.editor.OpenAsmFileAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/z64asm/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 * @date December 29, 2014
 */
public class z64docDataObject extends MultiDataObject {

    public z64docDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/z64asm", false);
        getCookieSet().add(new z64DataEditor());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }
    
    
    
    private class z64DataEditor extends DataEditorSupport implements
                                                                EditorCookie.Observable,
                                                                OpenCookie,
                                                                EditCookie,
                                                                PrintCookie,
                                                                CloseCookie {

        private final SaveCookie save;
        private final FileChangeListener listener;

        public z64DataEditor() {
            super(z64docDataObject.this, null, new GradleEnv(z64docDataObject.this));

            save = new SaveCookie() {
                @Override
                public void save() throws IOException {
                    saveDocument();
                }

                @Override
                public String toString() {
                    return getPrimaryFile().getNameExt();
                }
            };
            listener = new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    updateTitles();
                }
            };

            getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, getPrimaryFile()));
            setMIMEType("text/z64asm");
        }

//        @Override
//        protected Pane createPane() {
//            return (CloneableEditorSupport.Pane)MultiViews.createCloneableMultiView("text/z64asm", getDataObject());
//        }

        @Override
        protected boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        @Override
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }
/*
        @Override
        protected String messageName() {
            return super.messageName();
        }
*/
/*        
        @Override
        protected String messageHtmlName() {
            String name = super.messageHtmlName();
            return name != null ? name : null;
        }
*/
/*
        private String annotateWithFolder(String name) {
            if (GradleProjectConstants.BUILD_FILE_NAME.equals(getPrimaryFile().getNameExt())) {
                FileObject parent = getPrimaryFile().getParent();
                if (parent != null) {
                    String folderName = parent.getNameExt();
                    return name + " [" + folderName + "]";
                }
            }

            return name;
        }
*/
        @Override
        protected boolean asynchronousOpen() {
            return true;
        }
    }

    private static class GradleEnv extends DataEditorSupport.Env {
        private static final long serialVersionUID = 136529845402150749L;

        public GradleEnv(MultiDataObject dataObject) {
            super(dataObject);
        }

        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(z64DataEditor.class);
        }
    }

}
