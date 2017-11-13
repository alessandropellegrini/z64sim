/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.z64sim.assembler.Assembler;
import org.z64sim.assembler.ParseException;
import org.z64sim.assembler.Token;

public class z64Parser extends Parser {

    private Snapshot snapshot;
    private Assembler assembler;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        Reader reader = new StringReader(snapshot.getText().toString());
        assembler = new Assembler(reader);
    }

    @Override
    public Result getResult(Task task) {
        return new z64ParserResult(snapshot, assembler);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class z64ParserResult extends ParserResult {

        private final Assembler assembler;
        private final AtomicBoolean valid = new AtomicBoolean(true);
        private final Snapshot snapshot;
        private List<ErrorDescription> errors;

        z64ParserResult(Snapshot snapshot, Assembler assembler) {
            super(snapshot);
            this.snapshot = snapshot;
            this.assembler = assembler;
        }

        public Assembler getAssembler() throws org.netbeans.modules.parsing.spi.ParseException {
            if (!valid.get()) {
                throw new org.netbeans.modules.parsing.spi.ParseException();
            }
            return assembler;
        }

        @Override
        protected void invalidate() {
            valid.set(false);
        }

        public boolean getResult() throws org.netbeans.modules.parsing.spi.ParseException {
            if (!valid.get()) {
                throw new org.netbeans.modules.parsing.spi.ParseException();
            }
            return true;
        }

        public List<ErrorDescription> getErrors() {
            return this.errors;
        }

        @Override
        public List<? extends Error> getDiagnostics() {

            try {
                List<ParseException> syntaxErrors = this.assembler.getSyntaxErrors();
                Document document = this.snapshot.getSource().getDocument(false);
                this.errors = new ArrayList<ErrorDescription>();
                for (ParseException syntaxError : syntaxErrors) {
                    Token token = syntaxError.currentToken;
                    int start = NbDocument.findLineOffset((StyledDocument) document, token.beginLine - 1) + token.beginColumn - 1;
                    int end = NbDocument.findLineOffset((StyledDocument) document, token.endLine - 1) + token.endColumn;
                    ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(
                            org.netbeans.spi.editor.hints.Severity.ERROR,
                            syntaxError.getMessage(),
                            document,
                            document.createPosition(start),
                            document.createPosition(end)
                    );
                    this.errors.add(errorDescription);
                }
            } catch (BadLocationException ex1) {
                Exceptions.printStackTrace(ex1);
            }

            return Collections.EMPTY_LIST;
        }

        @Override
        public Snapshot getSnapshot() {
            return this.snapshot;
        }

    }

    public static class z64ParseError implements Error {

        private final String description;
        private final int start;
        private final int end;

        z64ParseError(String description, FileObject file, int start, int end) {
            this.description = description;
            this.start = start;
            this.end = end;
        }

        @Override
        public String getDisplayName() {
            return description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getKey() {
            return description;
        }

        @Override
        public FileObject getFile() {
            return null;
        }

        @Override
        public int getStartPosition() {
            return start;
        }

        @Override
        public int getEndPosition() {
            return end;
        }

        @Override
        public boolean isLineError() {
            return false;
        }

        @Override
        public Severity getSeverity() {
            return Severity.ERROR;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }

    }

}
