/*
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.apollo.consortium.comms;

import java.util.concurrent.ExecutionException;

import com.salesfoce.apollo.consortium.proto.Join;
import com.salesfoce.apollo.consortium.proto.JoinResult;
import com.salesfoce.apollo.consortium.proto.OrderingServiceGrpc;
import com.salesfoce.apollo.consortium.proto.OrderingServiceGrpc.OrderingServiceFutureStub;
import com.salesfoce.apollo.consortium.proto.ReplicateTransactions;
import com.salesfoce.apollo.consortium.proto.Stop;
import com.salesfoce.apollo.consortium.proto.StopData;
import com.salesfoce.apollo.consortium.proto.SubmitTransaction;
import com.salesfoce.apollo.consortium.proto.Sync;
import com.salesfoce.apollo.consortium.proto.TransactionResult;
import com.salesforce.apollo.comm.ServerConnectionCache.CreateClientCommunications;
import com.salesforce.apollo.comm.ServerConnectionCache.ManagedServerConnection;
import com.salesforce.apollo.consortium.ConsortiumMetrics;
import com.salesforce.apollo.consortium.OrderingService;
import com.salesforce.apollo.membership.Member;

/**
 * @author hal.hildebrand
 *
 */
public class ConsortiumClientCommunications implements OrderingService {

    public static CreateClientCommunications<ConsortiumClientCommunications> getCreate(ConsortiumMetrics metrics) {
        return (t, f, c) -> new ConsortiumClientCommunications(c, t, metrics);

    }

    private final ManagedServerConnection   channel;
    private final OrderingServiceFutureStub client;
    private final Member                    member;
    @SuppressWarnings("unused")
    private final ConsortiumMetrics         metrics;

    public ConsortiumClientCommunications(ManagedServerConnection channel, Member member, ConsortiumMetrics metrics) {
        this.member = member;
        this.channel = channel;
        this.client = OrderingServiceGrpc.newFutureStub(channel.channel).withCompression("gzip");
        this.metrics = metrics;
    }

    @Override
    public TransactionResult clientSubmit(SubmitTransaction txn) {
        try {
            return client.submit(txn).get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("error communicating with: " + member, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("error communicating with: " + member, e);
        }
    }

    public Member getMember() {
        return member;
    }

    @Override
    public JoinResult join(Join join) {
        try {
            return client.join(join).get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("error communicating with: " + member, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("error communicating with: " + member, e);
        }
    }

    public void release() {
        channel.release();
    }

    @Override
    public void replicate(ReplicateTransactions transactions) {
        client.replicate(transactions);
    }

    @Override
    public void stop(Stop stop) {
        client.stop(stop);
    }

    @Override
    public void stopData(StopData stopData) {
        client.stopData(stopData);
    }

    @Override
    public void sync(Sync sync) {
        client.sync(sync);
    }
}
