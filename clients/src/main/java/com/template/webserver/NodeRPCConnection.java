package com.template.webserver;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 */
@Component
public class NodeRPCConnection implements AutoCloseable {
    // The host of the node we are connecting to.
    @Value("${config.rpc.host}")
    private String host;
    // The RPC port of the node we are connecting to.
    @Value("${config.rpc.username}")
    private String username;
    // The username for logging into the RPC client.
    @Value("${config.rpc.password}")
    private String password;
    // The password for logging into the RPC client.
    @Value("${config.bank.rpc.port}")
    private int bankRpcPort;

    @Value("${config.user.rpc.port}")
    private int userRpcPort;

    private CordaRPCConnection bankRpcConnection;
    CordaRPCOps bankProxy;

    private CordaRPCConnection userRpcConnection;
    CordaRPCOps userProxy;

    public Party bankParty;
    public Party userParty;
    public Party merchantParty;

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, bankRpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        bankRpcConnection = rpcClient.start(username, password);
        bankProxy = bankRpcConnection.getProxy();

        rpcAddress = new NetworkHostAndPort(host, userRpcPort);
        rpcClient = new CordaRPCClient(rpcAddress);
        userRpcConnection = rpcClient.start(username, password);
        userProxy = userRpcConnection.getProxy();

        List<NodeInfo> nodes = bankProxy.networkMapSnapshot();
        for (int i=0; i<nodes.size(); i++) {
            Party currentParty = nodes.get(i).getLegalIdentities().get(0);
            String partyName = currentParty.getName().getOrganisation();
            if (partyName.equals("Bank")) {
                bankParty = currentParty;
            }
            else if (partyName.equals("User")) {
                userParty = currentParty;
            }
            else if (partyName.equals("Merchant")) {
                merchantParty = currentParty;
            }
        }

    }

    @PreDestroy
    public void close() {
        bankRpcConnection.notifyServerAndClose();
        userRpcConnection.notifyServerAndClose();
    }
}