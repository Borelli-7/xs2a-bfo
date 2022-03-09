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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aFundsConfirmationMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.piis.PiisConsentValidation;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationServiceTest {
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String IBAN = "DE69760700240340283600";
    private static final AccountReferenceType SECOND_ACCOUNT_REFERENCE_TYPE = AccountReferenceType.BBAN;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final PiisConsent piisConsent = new PiisConsent();

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    @Mock
    private SpiToXs2aFundsConfirmationMapper spiToXs2aFundsConfirmationMapper;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private FundsConfirmationSpi fundsConfirmationSpi;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    @Mock
    private Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private SpiPiisConsent spiPiisConsent;
    @Mock
    private CmsConsent cmsConsent;
    @Mock
    private PiisConsentService piisConsentService;
    @Mock
    private PiisConsentValidation piisConsentValidation;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private FundsConfirmationService fundsConfirmationService;

    @Test
    void fundsConfirmation_AspspConsentSupported_Success_ShouldRecordEvent() {
        // Given
        AccountReference accountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        piisConsent.setAspspAccountAccesses(new AccountAccess(List.of(accountReference), null, null, null));
        CmsResponse<List<CmsConsent>> cmsResponse = CmsResponse.<List<CmsConsent>>builder().payload(List.of(cmsConsent)).build();
        when(xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(buildFundsConfirmationRequest(accountReference, null)))
            .thenReturn(buildSpiFundsConfirmationRequest());
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(buildSpiFundsConfirmationResponse()))
            .thenReturn(buildFundsConfirmationResponse());
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), any(), any(), any()))
            .thenReturn(buildSuccessSpiResponse());
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(any()))
            .thenReturn(null);
        when(piisConsentService.getPiisConsentListByAccountIdentifier(any(), any()))
            .thenReturn(cmsResponse);
        when(xs2aPiisConsentMapper.mapToPiisConsent(any()))
            .thenReturn(piisConsent);
        when(piisConsentValidation.validatePiisConsentData(anyList()))
            .thenReturn(new PiisConsentValidationResult(piisConsent));


        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(accountReference, null);

        // When
        fundsConfirmationService.fundsConfirmation(request);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED);
    }


    @Test
    void fundsConfirmation_AspspConsentSupportedConsentIdPresent_validationError() {
        // Given
        AccountReference accountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        piisConsent.setAspspAccountAccesses(new AccountAccess(List.of(accountReference), null, null, null));
        CmsResponse<List<CmsConsent>> cmsResponse = CmsResponse.<List<CmsConsent>>builder().payload(List.of(cmsConsent)).build();
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(accountReference, CONSENT_ID);

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(request);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        verifyNoInteractions(fundsConfirmationSpi);
    }

    @Test
    void fundsConfirmation_AspspConsentSupported_validationError() {
        // Given
        AccountReference accountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        piisConsent.setAspspAccountAccesses(new AccountAccess(List.of(accountReference), null, null, null));
        CmsResponse<List<CmsConsent>> cmsResponse = CmsResponse.<List<CmsConsent>>builder().payload(List.of(cmsConsent)).build();
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(accountReference, CONSENT_ID);

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(request);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        verifyNoInteractions(fundsConfirmationSpi);
    }

    @ParameterizedTest
    @EnumSource(AccountReferenceType.class)
    void fundsConfirmation_tppConsentSupported_withConsent(AccountReferenceType referenceType) {
        //Given
        AccountReference firstAccountReference = getAccountReference(referenceType, IBAN, CURRENCY);
        AccountReference secondAccountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        piisConsent.setTppAccountAccesses(new AccountAccess(List.of(firstAccountReference, secondAccountReference), null, null, null));
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), eq(spiPiisConsent), any(), any()))
            .thenReturn(buildSuccessSpiResponse());
        when(spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(buildSpiFundsConfirmationResponse()))
            .thenReturn(buildFundsConfirmationResponse());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(spiAspspConsentDataProvider);
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(firstAccountReference, CONSENT_ID);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);

        //When
        fundsConfirmationService.fundsConfirmation(request);
        //Then
        verify(xs2aPiisConsentService, atLeastOnce()).getPiisConsentById(CONSENT_ID);
        verify(fundsConfirmationSpi, atLeastOnce()).performFundsSufficientCheck(any(), eq(spiPiisConsent), any(), any());
    }

    @Test
    void fundsConfirmation_tppConsentSupportedConsentInvalid_fail() {
        //Given
        AccountReference firstAccountReference = getAccountReference(AccountReferenceType.BBAN, IBAN, CURRENCY);
        AccountReference secondAccountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        piisConsent.setTppAccountAccesses(new AccountAccess(List.of(firstAccountReference, secondAccountReference), null, null, null));
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(firstAccountReference, CONSENT_ID);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(request);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_401);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.CONSENT_INVALID);
        verifyNoInteractions(fundsConfirmationSpi);
    }


    @Test
    void fundsConfirmation_tppConsentSupported_withoutConsent_fail() {
        //Given
        AccountReference firstAccountReference = getAccountReference(AccountReferenceType.IBAN, IBAN, CURRENCY);
        AccountReference secondAccountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);

        FundsConfirmationRequest request = buildFundsConfirmationRequest(firstAccountReference, CONSENT_ID);

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(request);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_403);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.CONSENT_UNKNOWN_403);
        verifyNoInteractions(fundsConfirmationSpi);
    }

    @Test
    void fundsConfirmation_tppConsentSupported_withConsentWrongAccesses_fail() {
        //Given
        AccountReference firstAccountReference = getAccountReference(AccountReferenceType.IBAN, IBAN, CURRENCY);
        AccountReference secondAccountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setTppAccountAccesses(new AccountAccess(Collections.singletonList(secondAccountReference), null, null, null));
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(firstAccountReference, CONSENT_ID);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(request);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.NO_PIIS_ACTIVATION);
        verifyNoInteractions(fundsConfirmationSpi);
    }

    @Test
    void fundsConfirmation_tppConsentSupported_withoutConsent() {
        //Given
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), any(), any(), any()))
            .thenReturn(buildSuccessSpiResponse());
        when(spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(buildSpiFundsConfirmationResponse()))
            .thenReturn(buildFundsConfirmationResponse());
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        FundsConfirmationRequest request = buildFundsConfirmationRequest(null, null);

        //When
        fundsConfirmationService.fundsConfirmation(request);
        //Then
        verify(xs2aPiisConsentService, never()).getPiisConsentById(CONSENT_ID);
        verify(fundsConfirmationSpi, atLeastOnce()).performFundsSufficientCheck(any(), eq(null), any(), any());
    }

    @Test
    void fundsConfirmation_fundsConfirmationSpi_performFundsSufficientCheck_fail() {
        // Given
        AccountReference accountReference = getAccountReference(SECOND_ACCOUNT_REFERENCE_TYPE, IBAN, CURRENCY);
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        piisConsent.setTppAccountAccesses(new AccountAccess(List.of(accountReference), null, null, null));
        ErrorHolder errorHolder = ErrorHolder
                                      .builder(PIIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                      .build();
        when(xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(buildFundsConfirmationRequest(accountReference, CONSENT_ID)))
            .thenReturn(buildSpiFundsConfirmationRequest());
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIIS)))
            .thenReturn(errorHolder);
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.TPP_CONSENT_SUPPORTED);
        when(fundsConfirmationSpi.performFundsSufficientCheck(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiFundsConfirmationResponse>builder()
                            .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                            .build());
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(any()))
            .thenReturn(null);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(buildFundsConfirmationRequest(accountReference, CONSENT_ID));

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    void fundsConfirmation_serviceNotSupported() {
        // Given
        when(aspspProfileServiceWrapper.getPiisConsentSupported())
            .thenReturn(PiisConsentSupported.NOT_SUPPORTED);

        // When
        ResponseObject<FundsConfirmationResponse> response = fundsConfirmationService.fundsConfirmation(buildFundsConfirmationRequest(null, CONSENT_ID));

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIIS_406);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_NOT_SUPPORTED);
    }

    private FundsConfirmationRequest buildFundsConfirmationRequest(AccountReference accountReference, String consentId) {
        FundsConfirmationRequest confirmationRequest = new FundsConfirmationRequest();
        confirmationRequest.setConsentId(consentId);
        confirmationRequest.setPsuAccount(accountReference);
        return confirmationRequest;
    }

    private SpiFundsConfirmationRequest buildSpiFundsConfirmationRequest() {
        return new SpiFundsConfirmationRequest();
    }

    private SpiFundsConfirmationResponse buildSpiFundsConfirmationResponse() {
        SpiFundsConfirmationResponse response = new SpiFundsConfirmationResponse();
        response.setFundsAvailable(true);
        return response;
    }

    private FundsConfirmationResponse buildFundsConfirmationResponse() {
        FundsConfirmationResponse response = new FundsConfirmationResponse();
        response.setFundsAvailable(true);
        return response;
    }

    private SpiResponse<SpiFundsConfirmationResponse> buildSuccessSpiResponse() {
        return SpiResponse.<SpiFundsConfirmationResponse>builder()
                   .payload(buildSpiFundsConfirmationResponse())
                   .build();
    }

    private AccountReference getAccountReference(AccountReferenceType referenceType, String value, Currency currency) {
        return new AccountReference(referenceType, value, currency);
    }
}
