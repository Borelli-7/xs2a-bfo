/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aPisCommonPaymentMapperTest {
    private final static JsonReader jsonReader = new JsonReader();
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static PsuIdData psuData;
    private static CreatePisAuthorisationResponse createPisAuthorisationResponse;

    @InjectMocks
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;

    @Before
    public void setUp() {
        psuData =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/psu-data.json", PsuIdData.class);
        createPisAuthorisationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-pis-authorisation-response.json",
                                         CreatePisAuthorisationResponse.class);
    }

    @Test
    public void mapToCmsUpdateCommonPaymentPsuDataReq() {
        UpdatePisCommonPaymentPsuDataRequest expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/update-pis-common-payment-psu-data-request.json",
                                         UpdatePisCommonPaymentPsuDataRequest.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse inputData =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/update-pis-common-payment-psu-data-response.json",
                                         Xs2aUpdatePisCommonPaymentPsuDataResponse.class);

        UpdatePisCommonPaymentPsuDataRequest actual = xs2aPisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(inputData);

        assertEquals(expected, actual);
    }

    @Test
    public void buildSpiScaConfirmation() {
        SpiScaConfirmation expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-sca-confirmation.json",
                                         SpiScaConfirmation.class);

        Xs2aUpdatePisCommonPaymentPsuDataRequest request =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-update-pis-common-payment-psu-data-request.json",
                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);
        String consentId = "consentId";
        String paymentId = "paymentId";

        SpiScaConfirmation actual = xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, consentId, paymentId, psuData);

        assertEquals(expected, actual);
    }

    @Test
    public void mapToXs2aPisCommonPayment() {
        Xs2aPisCommonPayment expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-pis-common-payment.json",
                                         Xs2aPisCommonPayment.class);

        CreatePisCommonPaymentResponse input =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/create-pis-common-payment-response.json",
                                         CreatePisCommonPaymentResponse.class);

        Xs2aPisCommonPayment actual = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(input, psuData);

        assertEquals(expected, actual);
    }

    @Test
    public void mapToXs2aCreatePisCancellationAuthorisationResponse() {
        Xs2aCreatePisCancellationAuthorisationResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-create-pis-cancellation-authorisation-response.json",
                                         Xs2aCreatePisCancellationAuthorisationResponse.class);

        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualOptional =
            xs2aPisCommonPaymentMapper
                .mapToXs2aCreatePisCancellationAuthorisationResponse(createPisAuthorisationResponse, PAYMENT_TYPE);

        assertEquals(expected, actualOptional.get());
    }

    @Test
    public void mapToXs2aCreatePisCancellationAuthorisationResponse_ShouldReturnEmpty() {
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> expected = Optional.empty();
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actual = xs2aPisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(null, null);
        assertEquals(expected, actual);
    }

    @Test
    public void mapToXsa2CreatePisAuthorisationResponse() {
        Xs2aCreatePisAuthorisationResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-create-pis-authorisation-response.json",
                                         Xs2aCreatePisAuthorisationResponse.class);

        Optional<Xs2aCreatePisAuthorisationResponse> actualOptional =
            xs2aPisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(createPisAuthorisationResponse, PAYMENT_TYPE);

        assertEquals(expected, actualOptional.get());
    }

    @Test
    public void mapToXsa2CreatePisAuthorisationResponse_ShouldReturnEmpty() {
        Optional<Xs2aCreatePisAuthorisationResponse> expected = Optional.empty();
        Optional<Xs2aCreatePisAuthorisationResponse> actual = xs2aPisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(null, null);
        assertEquals(expected, actual);
    }
}