package com.template.webserver;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

    @PostConstruct
    public void initialiseNodeRPCConnection() {
        NetworkHostAndPort rpcAddress = new NetworkHostAndPort(host, bankRpcPort);
        CordaRPCClient rpcClient = new CordaRPCClient(rpcAddress);
        bankRpcConnection = rpcClient.start(username, password);
        bankProxy = bankRpcConnection.getProxy();

        rpcAddress = new NetworkHostAndPort(host, userRpcPort);
        rpcClient = new CordaRPCClient(rpcAddress);
        userRpcConnection = rpcClient.start(username, password);
        userProxy = bankRpcConnection.getProxy();
    }

    @PreDestroy
    public void close() {
        bankRpcConnection.notifyServerAndClose();
        userRpcConnection.notifyServerAndClose();
    }
}