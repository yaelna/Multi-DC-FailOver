package com.gigaspaces.app;


import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gateway.BootstrapResult;
import org.openspaces.admin.gateway.Gateway;
import org.openspaces.admin.gateway.GatewaySinkSource;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceReplicationManager;
import org.openspaces.core.gateway.GatewayTarget;

import java.util.concurrent.TimeUnit;

public class Main {
    
    private static final long DEFAULT_WAIT_TIMEOUT = 60;
    
    public static void main(String[] args) throws Exception {
        // Create an admin and connect to the grid
        AdminFactory factory = new AdminFactory();
        if (args.length > 0) {
            factory.addLocators(args[0]);
        }
        Admin admin = factory.createAdmin();
        admin.getGridServiceAgents().waitForAtLeastOne();
       
        Gateway targetGateway = admin.getGateways().waitFor("LONDON", DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS); // slave cluster
        if (targetGateway == null) {
            System.out.println("LONDON gateway has not been discovered. Exiting...");
            System.exit(1);
        }
        if (targetGateway.waitForDelegator("NEWYORK", DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS) == null) {
            System.out.println("NEWYORK delegator has not been discovered in LONDON gateway. Exiting...");
            System.exit(1);
        }
       
        Gateway sourceGateway = admin.getGateways().waitFor("NEWYORK", DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS); // master cluster
        if (sourceGateway == null) {
            System.out.println("NEWYORK gateway has not been discovered. Exiting...");
            System.exit(1);
        }
        GatewaySinkSource londonSinkSource = sourceGateway.waitForSinkSource("LONDON", DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
        if (londonSinkSource == null) {
            System.out.println("LONDON sink source has not been discovered in NEWYORK gateway. Exiting...");
            System.exit(1);
        }
        Space targetSpace = admin.getSpaces().waitFor("target-space", DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
        if (targetSpace == null) {
            System.out.println("'target-space' has not been discovered. Exiting...");
            System.exit(1);
        }
        
        SpaceReplicationManager srm = targetSpace.getReplicationManager();
        GatewayTarget gatewayTarget = new GatewayTarget("NEWYORK");
        try {
            srm.addGatewayTarget(gatewayTarget);
            System.out.println("Bootstrapping 'source' space");
            BootstrapResult bootstrapResult = londonSinkSource.bootstrapFromGatewayAndWait(3600, TimeUnit.SECONDS);
            if (bootstrapResult.isSucceeded()) {
                System.out.println("NEWYORK space was bootstrapped successfully");
            } else {
                System.out.println("Error occuried while bootstrapping NEWYORK space : " + bootstrapResult.getFailureCause().getMessage());
            }
        } catch(Exception e) {
            System.err.println("Unexpected error occuried while bootstrapping NEWYORK space : " + e.getMessage());
            throw e;
        } finally {
            srm.removeGatewayTarget(gatewayTarget.getName());
        }
    }
}
