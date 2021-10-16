package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import com.template.states.RedemptionState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.ArrayList;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferMetal extends FlowLogic<SignedTransaction> {

    private String voucher;
    private String customer;
    private int point;
    private Party newOwner;
    private int input = 0;

    public TransferMetal(String voucher, String customer, int point, Party newOwner) {
        this.voucher = voucher;
        this.customer = customer;
        this.point = point;
        this.newOwner = newOwner;
        //O=Merchant,L=New York,C=US
        /*
        this.newOwner= getServiceHub().getNetworkMapCache()
                .getNodeByLegalName(new CordaX500Name("Merchant","New York","US"))
                .getLegalIdentities().get(0);*/
    }

    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary.");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty.");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");

    private final ProgressTracker progressTracker = new ProgressTracker(
            RETRIEVING_NOTARY,
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            COUNTERPARTY_SESSION,
            FINALISING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //    ----------------------------------------------- Check for Metal States Starts-----------------------------------------------

    List<StateAndRef<MetalState>> checkForMetalStates() throws FlowException {

        //Ok for gradle compilation, why IntelliJ complain?
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        /*TODO: Get Party
        Party p= getServiceHub().getNetworkMapCache()
                .getNodeByLegalName(new CordaX500Name("mint","","GB"))
                .getLegalIdentities().get(0);
        */
        List<StateAndRef<MetalState>> MetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();
        List<StateAndRef<MetalState>> result = new ArrayList<>();

        boolean inputFound = false;
        int size = MetalStates.size();
        int totalPoint = 0;
        for (int i = 0; i < size; i++) {
            if (MetalStates.get(i).getState().getData().getCustomer().equals(customer)
            && totalPoint < point) { //burn the point for input
                result.add(MetalStates.get(i));
                totalPoint += MetalStates.get(i).getState().getData().getPoint();
            }
        }


        if (result.size()==0) {
            System.out.println("\n Input not found");
            throw new FlowException();
        }

        return result;
    }

    int findUnBurnedPoint(List<StateAndRef<MetalState>> input, int pointToRedeem) {
        int totalInputPoint = 0;
        for (int i=0; i<input.size(); i++) {
            totalInputPoint += input.get(i).getState().getData().getPoint();
        }
        return (totalInputPoint - pointToRedeem);
    }

    //    ----------------------------------------------- Check for Metal States Ends-----------------------------------------------

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        // Retrieve Notary Identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        List<StateAndRef<MetalState>> inputStates = null;

        inputStates = checkForMetalStates();

        Party issuer = inputStates.get(0).getState().getData().getIssuer();


        //Output State will be the redemption + unburned points for last reward
         int unburnedPoint = findUnBurnedPoint(inputStates, point);
         MetalState outputState = null;
         if (unburnedPoint>0) {
             outputState = new MetalState(customer, unburnedPoint, issuer, getOurIdentity()); //unburned points
         }

        //Create transaction components
        //MetalState outputState = new MetalState(metalName, weight, issuer, newOwner);
        Command cmd = new Command(new MetalContract.Transfer(), getOurIdentity().getOwningKey());


        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txB = new TransactionBuilder(notary);
        //outputState for unburned point for User
        if (outputState!=null) txB.addOutputState(outputState, MetalContract.CID);

        //outputState for Redemption
        RedemptionState outputState2 = new RedemptionState(voucher, customer, point, issuer, newOwner);
        txB.addOutputState(outputState2);

        txB.addCommand(cmd);

        for (int i=0; i<inputStates.size(); i++) {
            txB.addInputState(inputStates.get(i));
        }


        // Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txB);


        // Create session with CounterParty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession = initiateFlow(issuer);


        // Finalize and send to CounterParty
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession, mintPartySession));


    }
}
