package org.z64sim.iodevice;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class ioDeviceTest {

    public ioDeviceTest() {
    }

    @org.testng.annotations.Test
    public void testLuaInterpreter() throws FileNotFoundException {
        String script = ioDeviceTest.class.getResource("test.lua").getPath();
        System.out.println("Running " + script);
        System.out.println("Waiting for 4 seconds before quitting...");

        // create an environment to run in
        Globals globals = JsePlatform.standardGlobals();

        // Use the convenience function on Globals to load a chunk.
        LuaValue chunk = globals.loadfile(script);

        // Use any of the "call()" or "invoke()" functions directly on the chunk.
        chunk.call(LuaValue.valueOf(script));

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ioDeviceTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
