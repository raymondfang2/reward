package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(IssueMetal.class)
public class IssueMetalResponder extends FlowLogic<SignedTransaction> {
    private FlowSession otherPartySession;

    public IssueMetalResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.

        System.out.println("Received precious Metal.");

        return subFlow(new ReceiveFinalityFlow(otherPartySession));


    }
}
