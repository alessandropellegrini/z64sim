/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.view.MuOpOverlayMapper;
import it.uniroma2.pellegrini.z64sim.view.MuOpOverlayMapper.MuOpMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("µ-Op Overlay Mapper Tests")
public class MuOpOverlayMapperTest {

    @Test
    @DisplayName("movq %rax, %rcx step 7 (reg-to-reg): does NOT update flags")
    public void testMovRegToRegDoesNotUpdateFlags() {
        MuOpMapping mapping = MuOpOverlayMapper.map("RCX \u2190 SHIFTER_OUT[SHL, 0]");

        // Must not assert flags buffer or flags write
        assertFalse(mapping.signals.contains("B_FLAGS"), "Signals must NOT assert B_FLAGS for data movement: " + mapping.signals);
        assertFalse(mapping.signals.contains("B_FLAGSS"), "Signals must NOT contain B_FLAGSS: " + mapping.signals);
        assertFalse(mapping.signals.contains("W_FLAGS"), "Signals must NOT assert W_FLAGS for data movement: " + mapping.signals);

        // Must not highlight flags overlay
        assertFalse(mapping.overlays.contains("flags-from-shifter"), "Overlays must NOT highlight flags-from-shifter for data movement");

        // Must assert data path signals
        assertEquals("S_opcode = SHL, 0, B_S = 1, S_MUX = 2, REG_MUX = 1, W_M = 1", mapping.signals);
        assertTrue(mapping.overlays.contains("shifter"));
        assertTrue(mapping.overlays.contains("shifter-out"));
        assertTrue(mapping.overlays.contains("reg-write"));
    }

    @Test
    @DisplayName("Shift instruction (shl $1, %rcx): DOES update flags and asserts B_FLAGSS")
    public void testShiftUpdatesFlags() {
        MuOpMapping mapping = MuOpOverlayMapper.map("RCX \u2190 SHIFTER_OUT[SHL, 1]");

        assertTrue(mapping.signals.contains("B_FLAGSS = 1"), "Shift must assert B_FLAGSS = 1: " + mapping.signals);
        assertTrue(mapping.signals.contains("W_FLAGS = 1"), "Shift must assert W_FLAGS = 1: " + mapping.signals);
        assertTrue(mapping.overlays.contains("flags-from-shifter"), "Shift must highlight flags-from-shifter");
    }

    @Test
    @DisplayName("Arithmetic instruction (addq %rax, %rcx): DOES update flags via ALU")
    public void testAluArithmeticUpdatesFlags() {
        MuOpMapping mapping = MuOpOverlayMapper.map("RCX \u2190 ALU_OUT[ADD]");

        assertTrue(mapping.signals.contains("B_FLAGSA = 1"), "Arithmetic must assert B_FLAGSA = 1: " + mapping.signals);
        assertTrue(mapping.signals.contains("W_FLAGS = 1"), "Arithmetic must assert W_FLAGS = 1: " + mapping.signals);
        assertTrue(mapping.overlays.contains("flags-from-alu"), "Arithmetic must highlight flags-from-alu");
    }

    @Test
    @DisplayName("Data movement with extension (movsX / movzX): does NOT update flags")
    public void testMovExtendDoesNotUpdateFlags() {
        MuOpMapping mappingReg = MuOpOverlayMapper.map("RCX \u2190 ALU_OUT[Sign Extend, 0, 3]");
        assertFalse(mappingReg.signals.contains("B_FLAGSA"), "movsX must not assert B_FLAGSA: " + mappingReg.signals);
        assertFalse(mappingReg.signals.contains("W_FLAGS"), "movsX must not assert W_FLAGS: " + mappingReg.signals);
        assertFalse(mappingReg.overlays.contains("flags-from-alu"), "movsX must not highlight flags-from-alu");

        MuOpMapping mappingMdr = MuOpOverlayMapper.map("MDR \u2190 ALU_OUT[Zero Extend, 0, 1]");
        assertFalse(mappingMdr.signals.contains("B_FLAGSA"), "movzX must not assert B_FLAGSA: " + mappingMdr.signals);
        assertFalse(mappingMdr.signals.contains("W_FLAGS"), "movzX must not assert W_FLAGS: " + mappingMdr.signals);
        assertFalse(mappingMdr.overlays.contains("flags-from-alu"), "movzX must not highlight flags-from-alu");
    }

    @Test
    @DisplayName("Compare and test without writeback: DO update flags")
    public void testCmpAndTestUpdateFlags() {
        MuOpMapping mappingCmp = MuOpOverlayMapper.map("ALU_OUT[SUB]");
        assertTrue(mappingCmp.signals.contains("B_FLAGSA = 1"), "cmp must assert B_FLAGSA: " + mappingCmp.signals);
        assertTrue(mappingCmp.signals.contains("W_FLAGS = 1"), "cmp must assert W_FLAGS: " + mappingCmp.signals);
        assertTrue(mappingCmp.overlays.contains("flags-from-alu"), "cmp must highlight flags-from-alu");

        MuOpMapping mappingTest = MuOpOverlayMapper.map("ALU_OUT[AND]");
        assertTrue(mappingTest.signals.contains("B_FLAGSA = 1"), "test must assert B_FLAGSA: " + mappingTest.signals);
        assertTrue(mappingTest.signals.contains("W_FLAGS = 1"), "test must assert W_FLAGS: " + mappingTest.signals);
        assertTrue(mappingTest.overlays.contains("flags-from-alu"), "test must highlight flags-from-alu");

        MuOpMapping mappingBt = MuOpOverlayMapper.map("ALU_OUT[BIT_TEST]");
        assertTrue(mappingBt.signals.contains("B_FLAGSA = 1"), "bt must assert B_FLAGSA: " + mappingBt.signals);
        assertTrue(mappingBt.signals.contains("W_FLAGS = 1"), "bt must assert W_FLAGS: " + mappingBt.signals);
        assertTrue(mappingBt.overlays.contains("flags-from-alu"), "bt must highlight flags-from-alu");
    }

    @Test
    @DisplayName("TEMP1 and TEMP2 from RIP assert B_RIP = 1 and highlight rip-read")
    public void testTempFromRip() {
        MuOpMapping mapping1 = MuOpOverlayMapper.map("TEMP1 \u2190 RIP");
        assertEquals("B_RIP = 1, W_T1 = 1", mapping1.signals);
        assertTrue(mapping1.overlays.contains("rip-read"));
        assertTrue(mapping1.overlays.contains("t1-write"));
        assertTrue(mapping1.overlays.contains("data-bus"));

        MuOpMapping mapping2 = MuOpOverlayMapper.map("TEMP2 \u2190 RIP");
        assertEquals("B_RIP = 1, W_T2 = 1", mapping2.signals);
        assertTrue(mapping2.overlays.contains("rip-read"));
        assertTrue(mapping2.overlays.contains("t2-write"));
        assertTrue(mapping2.overlays.contains("data-bus"));
    }
}
