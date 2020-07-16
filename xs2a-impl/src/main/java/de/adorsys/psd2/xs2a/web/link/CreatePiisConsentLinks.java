/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;

public class CreatePiisConsentLinks extends AbstractLinks {

    public CreatePiisConsentLinks(String httpUrl, Xs2aConfirmationOfFundsResponse response) {
        super(httpUrl);

        String consentId = response.getConsentId();

        setSelf(buildPath(UrlHolder.PIIS_CONSENT_LINK_URL, consentId));
        setStatus(buildPath(UrlHolder.PIIS_CONSENT_STATUS_URL, consentId));

    }
}