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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.link.PaymentAuthorisationCancellationAspectService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorisationCancellationAspect {

    private PaymentAuthorisationCancellationAspectService paymentAuthorisationCancellationAspectService;

    public CreatePisAuthorisationCancellationAspect(PaymentAuthorisationCancellationAspectService paymentAuthorisationCancellationAspectService) {
        this.paymentAuthorisationCancellationAspectService = paymentAuthorisationCancellationAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService.createPisCancellationAuthorisation(..)) && args(request)", returning = "result", argNames = "result,request")
    public ResponseObject<CancellationAuthorisationResponse> createPisAuthorisationAspect(ResponseObject<CancellationAuthorisationResponse> result, Xs2aCreatePisAuthorisationRequest request) {
        return paymentAuthorisationCancellationAspectService.createPisAuthorisationAspect(result, request);
    }


}
