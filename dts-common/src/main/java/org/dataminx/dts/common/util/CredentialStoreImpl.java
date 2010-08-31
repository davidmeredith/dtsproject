/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.common.util;

import java.util.HashMap;
import java.util.Map;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CredentialType;

//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;

/**
 * The implementation of the CredentialStore.
 *
 * @author Gerson Galang
 */
public class CredentialStoreImpl implements CredentialStore {

    /**
     * A container for credentials that users want to be saved only in memory.
     */
    private final Map<String, CredentialType> mInMemoryCredentialMap = new HashMap<String, CredentialType>();

    /**
     * {@inheritDoc}
     */
    public CredentialType getCredential(final String credUUID) {
        // TODO implement getting of credentials from database later on
        return mInMemoryCredentialMap.get(credUUID);
    }

    /**
     * {@inheritDoc}
     */
    public void writeToDatabase(final String credUUID,
        final CredentialType credential) {
        // TODO implement this later on
        throw new UnsupportedOperationException(
            "Writing of credentials to database is currently not supported.");

    }

    /**
     * {@inheritDoc}
     */
    public void writeToMemory(final String credUUID,
        final CredentialType credential) {
        mInMemoryCredentialMap.put(credUUID, credential);
    }

}
