/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.assembler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class AssemblerNGTest {

    public AssemblerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testLexer() throws FileNotFoundException {
        InputStream is = getClass().getResourceAsStream("test.asm");
        JavaCharStream stream = new JavaCharStream(is);
        AssemblerTokenManager manager = new AssemblerTokenManager(stream);
        Token token = manager.getNextToken();

        while (token != null && token.kind != AssemblerConstants.EOF) {
            System.out.println(token.toString() + "[" + token.kind + "]");
            token = manager.getNextToken();
        }
    }

    /*
    @Test
    public void testParser() throws Exception {
        System.out.println("Program");
        Assembler instance = null;
        instance.Program();
        fail("The test case is a prototype.");
    }
    */
}
