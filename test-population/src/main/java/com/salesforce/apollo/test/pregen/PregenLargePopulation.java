/*
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.apollo.test.pregen;

import static io.github.olivierlemasle.ca.CA.createCsr;
import static io.github.olivierlemasle.ca.CA.dn;

import java.io.File;

import com.salesforce.apollo.fireflies.ca.CertificateAuthority;

import io.github.olivierlemasle.ca.CA;
import io.github.olivierlemasle.ca.CertificateWithPrivateKey;
import io.github.olivierlemasle.ca.CsrWithPrivateKey;
import io.github.olivierlemasle.ca.RootCertificate;

/**
 * A utility to pre generate CA and member Cert/Key pairs for testing.
 * 
 * @author hal.hildebrand
 * @since 220
 */
public class PregenLargePopulation {
    private static final String alias                = "foo";
    private static final File   caDir                = new File("src/main/resources/large/ca");
    private static final String caKeystoreFile       = "ca.p12";
    public static final int     cardinality          = 1000;
    private static final String crlUri               = null;
    private static final double faultTolerance       = 0.0009;
    private static final char[] keystorePassword     = "".toCharArray();
    private static final String MEMBER_P12_TEMPLATE  = "member-%s.p12";
    private static final File   memberDir            = new File("src/main/resources/large/members");
    private static final double probabilityByzantine = .25;

    public static RootCertificate getCa() {
        return CA.loadRootCertificate(PregenLargePopulation.class.getResourceAsStream("/large/ca/" + caKeystoreFile),
                                      keystorePassword, alias);
    }

    public static CertificateWithPrivateKey getMember(int index) {
        return Util.loadFrom(PregenLargePopulation.class.getResourceAsStream("/large/members/"
                + memberKeystoreFile(index)), keystorePassword, alias);
    }

    public static void main(String[] argv) {
        caDir.mkdirs();
        memberDir.mkdirs();
        RootCertificate root = CertificateAuthority.mint(dn().setCn("test-ca.com")
                                                             .setO("World Company")
                                                             .setOu("IT dep")
                                                             .setSt("CA")
                                                             .setC("US")
                                                             .build(),
                                                         cardinality, probabilityByzantine, faultTolerance, crlUri);
        root.exportPkcs12(new File(caDir, "ca.p12").getAbsolutePath(), keystorePassword, alias);

        CertificateAuthority ca = new CertificateAuthority(root);

        int startPort = 49151 - 1;
        String host = "localhost";
        for (int i = 1; i <= cardinality; i++) {
            if (i % 10 == 0) {
                System.out.print(".");
            }
            if (i % 100 == 0) {
                System.out.println(" - " + i);
            }
            int ffPort = startPort--;
            CsrWithPrivateKey request = createCsr().generateRequest(dn().setCn(host)
                                                                        .setL(Integer.toString(ffPort))
                                                                        .setO("World Company")
                                                                        .setOu("IT dep")
                                                                        .setSt("CA")
                                                                        .setC("US")
                                                                        .build());
            CertificateWithPrivateKey cert = ca.mintNode(request).attachPrivateKey(request.getPrivateKey());
            cert.exportPkcs12(new File(memberDir, memberKeystoreFile(i)), keystorePassword, alias);
        }
    }

    public static String memberKeystoreFile(int index) {
        return String.format(MEMBER_P12_TEMPLATE, index);
    }
}
