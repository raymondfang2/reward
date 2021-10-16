package com.template.states;

import com.template.contracts.MetalContract;
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
public class RedemptionState implements ContractState {

    private String voucher; //Description of Voucher e.g. NTUC $10
    private String customer;
    private int point;
    private Party issuer; //Bank  ==> It seems the issuer will store
    private Party owner;  //Merchant ==> It seems the owner will store

    public RedemptionState(String voucher, String customer, int point, Party issuer, Party owner) {
        this.voucher = voucher;
        this.customer = customer;
        this.point = point;
        this.issuer = issuer;
        this.owner = owner;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }

    public String getVoucher() {
        return voucher;
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