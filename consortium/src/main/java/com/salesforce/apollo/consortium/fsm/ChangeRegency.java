/*
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.apollo.consortium.fsm;

import java.util.List;

import com.chiralbehaviors.tron.Entry;
import com.salesfoce.apollo.consortium.proto.ReplicateTransactions;
import com.salesfoce.apollo.consortium.proto.Stop;
import com.salesfoce.apollo.consortium.proto.StopData;
import com.salesfoce.apollo.consortium.proto.Sync;
import com.salesfoce.apollo.consortium.proto.Transaction;
import com.salesfoce.apollo.consortium.proto.Validate;
import com.salesforce.apollo.consortium.EnqueuedTransaction;
import com.salesforce.apollo.membership.Member;

/**
 * @author hal.hildebrand
 *
 */
public enum ChangeRegency implements Transitions {
    INITIAL {
        @Override
        public Transitions establishNextRegent() {
            return AWAIT_SYNCHRONIZATION;
        }

        @Override
        public Transitions startRegencyChange(List<EnqueuedTransaction> transactions) {
            return null;
        }

        @Override
        public Transitions deliverSync(Sync syncData, Member from) {
            context().establishNextRegent();
            context().deliverSync(syncData, from);
            return SYNCHRONIZED;
        }

        @Override
        public Transitions deliverStop(Stop stop, Member from) {
            context().deliverStop(stop, from);
            return null;
        }

        @Override
        public Transitions deliverStopData(StopData stopData, Member from) {
            context().deliverStopData(stopData, from);
            return null;
        }
    },
    AWAIT_SYNCHRONIZATION {

        @Override
        public Transitions startRegencyChange(List<EnqueuedTransaction> transactions) {
            return null;
        }

        @Entry
        public void regentElected() {
            context().establishNextRegent();
        }

        @Override
        public Transitions deliverSync(Sync syncData, Member from) {
            context().deliverSync(syncData, from);
            return SYNCHRONIZED;
        }
        @Override
        public Transitions deliverStop(Stop stop, Member from) {
            context().deliverStop(stop, from);
            return null;
        }

        @Override
        public Transitions deliverStopData(StopData stopData, Member from) {
            context().deliverStopData(stopData, from);
            return null;
        }

        @Override
        public Transitions syncd() {
            return SYNCHRONIZED;
        }
    },
    SYNCHRONIZED {
        
        @Entry
        public void resolveStatus() {
            context().resolveStatus();
        }

        @Override
        public Transitions becomeFollower() {
            fsm().pop().becomeFollower();
            return null;
        }

        @Override
        public Transitions becomeLeader() {
            fsm().pop().becomeLeader();
            return null;
        }

        @Override
        public Transitions deliverStop(Stop stop, Member from) {
            return null;
        }

        @Override
        public Transitions deliverStopData(StopData stopData, Member from) {
            return null;
        }
    };

    @Override
    public Transitions deliverTransaction(Transaction txn, Member from) {
        context().receive(txn);
        return null;
    }

    @Override
    public Transitions deliverValidate(Validate validation) {
        context().validate(validation);
        return null;
    }

    @Override
    public Transitions receive(Transaction transacton, Member from) {
        context().receive(transacton);
        return null;
    }

    @Override
    public Transitions deliverTransactions(ReplicateTransactions txns, Member from) {
        for (Transaction txn : txns.getTransactionsList()) {
            context().receive(txn);
        }
        return null;
    }
}
