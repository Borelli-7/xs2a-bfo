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

package de.adorsys.psd2.xs2a.web.validator.body.cancelpayment;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class CancelPaymentBodyValidatorImpl implements CancelPaymentBodyValidator {

    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;

    @Autowired
    public CancelPaymentBodyValidatorImpl(TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator) {
        this.tppRedirectUriBodyValidator = tppRedirectUriBodyValidator;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        return tppRedirectUriBodyValidator.validate(request, messageError);
    }
}
