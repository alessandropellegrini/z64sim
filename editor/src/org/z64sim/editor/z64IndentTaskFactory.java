/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
@MimeRegistration(mimeType = "text/z64asm", service = IndentTask.Factory.class)
public class z64IndentTaskFactory implements IndentTask.Factory {

    @Override
    public IndentTask createTask(Context context) {
        return new z64IndentTask(context);
    }
}
