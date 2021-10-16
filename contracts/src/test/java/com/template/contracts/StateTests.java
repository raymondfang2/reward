package com.template.contracts;

import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;
import com.template.states.MetalState;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class StateTests {

//    private Party Mint = new TestIdentity(new CordaX500Name("mint","","GB")).getParty();
//    private Party Trader = new TestIdentity(new CordaX500Name("trader","","GB")).getParty();
//
//    @Test
//    public void metalStateImplementsContractState() {
//        assertTrue(new MetalState("Gold", 10, Mint, Trader) instanceof ContractState);
//    }
//
//    @Test
//    public void metalStateHasTwoParticipantsTheIssuerAndOwner() {
//        MetalState metalState = new MetalState("Gold", 10, Mint, Trader);
//        assertEquals(2, metalState.getParticipants().size());
//        assertTrue(metalState.getParticipants().contains(Mint));
//        assertTrue(metalState.getParticipants().contains(Trader));
//
//    }
//
//    @Test
//    public void metalStateHasGettersForAllFields() {
//        MetalState metalState = new MetalState("Gold", 10, Mint, Trader);
//
//        assertEquals("Gold", metalState.getMetalName());
//        assertEquals(10, metalState.getWeight());
//        assertEquals(Mint, metalState.getIssuer());
//        assertEquals(Trader, metalState.getOwner());
//
//
//    }
//
//



}
