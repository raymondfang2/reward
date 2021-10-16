package com.template.webserver;

import com.template.flows.IssueMetal;
import com.template.flows.TransferMetal;
import com.template.states.MetalState;
import com.template.states.RedemptionState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
public class Controller {
    private final CordaRPCOps bankProxy;
    private final CordaRPCOps userProxy;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final NodeRPCConnection rpc;

    public Controller(NodeRPCConnection rpc) {
        this.bankProxy = rpc.bankProxy;
        this.userProxy = rpc.userProxy;
        this.rpc = rpc;
    }

    @GetMapping(value = "/templateendpoint", produces = "text/plain")
    private String templateendpoint() {
        return "Define an endpoint here.";
    }

    @GetMapping("/network")
    List<String> getNetwork() {
        List<NodeInfo> nodes = bankProxy.networkMapSnapshot();
        ArrayList<String> parties = new ArrayList(nodes.size());
        for (int i=0; i<nodes.size(); i++) {
            String partyName = nodes.get(i).getLegalIdentities().get(0).getName().getOrganisation();
            parties.add(partyName);
        }
        return parties;
    }

    //customer: Raymond, point: 10, owner: "O=User,L=New York,C=US"
    @GetMapping("/issue")
    String startIssueFlow(@RequestParam String customer, @RequestParam int point) {
        bankProxy.startFlowDynamic(IssueMetal.class,customer,point, rpc.userParty);

       //proxy.startFlowDynamic(ShipmentFlow.class,"Cybertruck",owner);
        return "Start Reward workflow: "+ customer + " ==> " + point;
    }

    //voucher: "NTUC $10", customer: Raymond, point: 10, newOwner: "O=Merchant,L=New York,C=US"
    @GetMapping("/redeem")
    String startRedemptionFlow(@RequestParam String voucher, @RequestParam String customer, @RequestParam int point) {
        userProxy.startFlowDynamic(TransferMetal.class,voucher, customer,point, rpc.merchantParty);
        //proxy.startFlowDynamic(ShipmentFlow.class,"Cybertruck",owner);
        return "Start Redemption Workflow: " + voucher + "Redeem using: " + customer + " ==> " + point;
    }

    /*
     private String customer;
    private int point;
     */

    @GetMapping("/getRewardState")
    List<HashMap<String, String>> getRewardState() {
        Vault.Page<MetalState> pages = bankProxy.vaultQuery(com.template.states.MetalState.class);
        List<StateAndRef<MetalState>> states = pages.getStates();
        List<HashMap<String, String>> result = new ArrayList<>(states.size());
        for (int i=0; i<states.size(); i++) {
            String customer = states.get(i).getState().getData().getCustomer();
            String point = ""+states.get(i).getState().getData().getPoint();
            HashMap<String, String> rewardState = new HashMap<>();
            rewardState.put("customer", customer);
            rewardState.put("point", point);
            result.add(rewardState);
        }
        return result;
    }

    //voucher
    @GetMapping("/getRedemptionState")
    List<HashMap<String, String>> getRedemptionState() {
        Vault.Page<RedemptionState> pages = bankProxy.vaultQuery(com.template.states.RedemptionState.class);
        List<StateAndRef<RedemptionState>> states = pages.getStates();
        List<HashMap<String, String>> result = new ArrayList<>(states.size());
        for (int i=0; i<states.size(); i++) {
            String voucher = states.get(i).getState().getData().getVoucher();
            String customer = states.get(i).getState().getData().getCustomer();
            String point = ""+states.get(i).getState().getData().getPoint();
            HashMap<String, String> rewardState = new HashMap<>();
            rewardState.put("voucher", voucher);
            rewardState.put("customer", customer);
            rewardState.put("point", point);
            result.add(rewardState);
        }
        return result;
    }
}