/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.query;

import de.adorsys.psd2.xs2a.core.error.MessageError;

import java.util.List;
import java.util.Map;

public interface QueryParameterValidator {
    /**
     * Validates query parameters from the request and populates given error with error text if parameters are invalid
     *
     * @param queryParameterMap query parameters from the request, with query parameter names acting as keys
     * @param messageError      error to be populated
     * @return {@link MessageError} object, enriched or not.
     */
    MessageError validate(Map<String, List<String>> queryParameterMap, MessageError messageError);
}
