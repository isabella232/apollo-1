/*
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.apollo.snow.engine.common;

import java.util.HashSet;
import java.util.Set;

import com.salesforce.apollo.snow.ids.ID;
import com.salesforce.apollo.snow.ids.ShortID;

/**
 * @author hal.hildebrand
 *
 */
public interface Sender {

    void getAccepted(HashSet<ShortID> vdrs, int requestID, Set<ID> acceptedFrontier);

    void accepted(ShortID validatorID, int requestID, Set<ID> filterAccepted);

    void acceptedFrontier(ShortID validatorID, int requestID, Set<ID> currentAcceptedFrontier);

    void getAcceptedFrontier(Set<ShortID> vdrs, int requestID);

}
