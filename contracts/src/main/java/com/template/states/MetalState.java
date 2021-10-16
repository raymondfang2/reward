package com.template.states;

import com.template.contracts.MetalContract;
import com.template.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(MetalContract.class)
public class MetalState implements ContractState {

    private String customer;
    private int point;
    private Party issuer; //Bank
    private Party owner; //User

    public MetalState(String customer, int point, Party issuer, Party owner) {
        this.customer = customer;
        this.point = point;
        this.issuer = issuer;
        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }

    public String getCustomer() {
        return customer;
    }

    public int getPoint() {
        return point;
    }

    public Party getIssuer() {return issuer;}
    public Party getOwner() {return owner;}

}