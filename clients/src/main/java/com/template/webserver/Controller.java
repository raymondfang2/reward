package com.template.webserver;

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
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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

    public Controller(NodeRPCConnection rpc) {
        this.bankProxy = rpc.bankProxy;
        this.userProxy = rpc.userProxy;
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

    @GetMapping("/issue")
    String startIssueFlow() {
        List<NodeInfo> nodes = bankProxy.networkMapSnapshot();
        NodeInfo spaceX = nodes.get(2);
        Party owner = spaceX.getLegalIdentities().get(0);
        for (int i=0; i< nodes.size(); i++) {
            System.out.println("==========>"+nodes.get(i).toString());
        }

       //proxy.startFlowDynamic(ShipmentFlow.class,"Cybertruck",owner);
        return "Start....";
    }

    @GetMapping("/redeem")
    String startRedemptionFlow() {
        List<NodeInfo> nodes = userProxy.networkMapSnapshot();
        NodeInfo spaceX = nodes.get(2);
        Party owner = spaceX.getLegalIdentities().get(0);
        for (int i=0; i< nodes.size(); i++) {
            System.out.println("==========>"+nodes.get(i).toString());
        }

        //proxy.startFlowDynamic(ShipmentFlow.class,"Cybertruck",owner);
        return "Start....";
    }

    @GetMapping("/getRewardState")
    List<String> getRewardState() {
        Vault.Page<MetalState> pages = bankProxy.vaultQuery(com.template.states.MetalState.class);
        List<StateAndRef<MetalState>> states = pages.getStates();
        List<String> result = new ArrayList<>(states.size());
        for (int i=0; i<states.size(); i++) {
            result.add(states.get(i).getState().getData().getCustomer()
                    +"=>"+states.get(i).getState().getData().getPoint());
        }
        return result;
    }


    @GetMapping("/getRedemptionState")
    List<String> getRedemptionState() {
        Vault.Page<RedemptionState> pages = bankProxy.vaultQuery(com.template.states.RedemptionState.class);
        List<StateAndRef<RedemptionState>> states = pages.getStates();
        List<String> result = new ArrayList<>(states.size());
        for (int i=0; i<states.size(); i++) {
            result.add(states.get(i).getState().getData().getCustomer()
                    +"=>"+states.get(i).getState().getData().getPoint());
        }
        return result;
    }
}