/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.aspsp.profile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AspspProfileRemoteUrls {

    @Value("${xs2a.cms.aspsp-profile.baseurl:http://localhost:48080/api/v1}")
    private String aspspProfileBaseUrl;

    /**
     * Returns URL-string in order to get sca approach
     *
     * @return String
     */
    public String getScaApproaches() {
        return aspspProfileBaseUrl + "/aspsp-profile/sca-approaches";
    }

    /**
     * Returns URL-string in order to get if tpp signature is required
     *
     * @return String
     */
    public String getAspspSettings() {
        return aspspProfileBaseUrl + "/aspsp-profile";
    }

    public String isMultitenancyEnabled() {
        return aspspProfileBaseUrl + "/aspsp-profile/multitenancy/enabled";
    }
}
