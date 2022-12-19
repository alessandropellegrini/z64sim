package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.UpdateController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateCheckTest {

    @Test
    @DisplayName("Testing Disassembler")
    public void testUpdateCheck() {
        UpdateController.init();
        while(!UpdateController.isCheckCompleted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
