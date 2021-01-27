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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.validator.AisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisConsentTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.AIS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateConsentPsuDataValidatorTest {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private static final PsuIdData PSU_ID_DATA_1 = new PsuIdData("anton.brueckner", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("anton.nebrueckner", null, null, null, null);

    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String INVALID_AUTHORISATION_ID = "random but invalid";
    private static final String CONFIRMATION_CODE = "confirmation code";

    private static final MessageError AUTHORISATION_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_403, TppMessageInformation.of(RESOURCE_UNKNOWN_403));

    private static final MessageError STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_409, TppMessageInformation.of(STATUS_INVALID));

    private static final MessageError SCA_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(SCA_INVALID));

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError FORMAT_BOTH_PSUS_ABSENT_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));

    private static final MessageError CREDENTIALS_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, of(PSU_CREDENTIALS_INVALID));

    private static final MessageError AIS_SERVICE_INVALID =
        new MessageError(ErrorType.AIS_400, of(SERVICE_INVALID_400));

    private JsonReader jsonReader;

    @Mock
    private AisConsentTppInfoValidator aisConsentTppInfoValidator;
    @Mock
    private AisAuthorisationValidator aisAuthorisationValidator;
    @Mock
    private AisAuthorisationStatusValidator aisAuthorisationStatusValidator;
    @Mock
    private AisPsuDataUpdateAuthorisationCheckerValidator psuDataUpdateAuthorisationCheckerValidator;
    @Mock
    private AuthorisationStageCheckValidator authorisationStageCheckValidator;

    @InjectMocks
    private UpdateConsentPsuDataValidator updateConsentPsuDataValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        updateConsentPsuDataValidator.setAisConsentTppInfoValidator(aisConsentTppInfoValidator);
        jsonReader = new JsonReader();
    }

    @Test
    void validate_withValidConsentObjectAndValidId_shouldReturnValid() {
        // Given
        AisConsent accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, accountConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false))
            .thenReturn(ValidationResult.valid());
        when(aisConsentTppInfoValidator.validateTpp(accountConsent.getConsentTppInformation().getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(any(), any(), eq(AIS)))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withValidConsentObjectAndInvalidId_shouldReturnInvalid() {
        AisConsent accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);

        when(aisAuthorisationValidator.validate(INVALID_AUTHORISATION_ID, accountConsent))
            .thenReturn(ValidationResult.invalid(AUTHORISATION_VALIDATION_ERROR));
        when(aisConsentTppInfoValidator.validateTpp(accountConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());

        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, buildUpdateRequest(INVALID_AUTHORISATION_ID, PSU_ID_DATA_1)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AUTHORISATION_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatus_shouldReturnInvalid() {
        // Given
        AisConsent accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent-failed-status.json", AisConsent.class);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, accountConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.FAILED, false))
            .thenReturn(ValidationResult.invalid(STATUS_VALIDATION_ERROR));
        when(aisConsentTppInfoValidator.validateTpp(accountConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatusInConfirmationAuthorisationFlow_shouldReturnScaInvalid() {
        // Given
        AisConsent accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent-failed-status.json", AisConsent.class);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, accountConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.FAILED, true))
            .thenReturn(ValidationResult.invalid(SCA_VALIDATION_ERROR));
        when(aisConsentTppInfoValidator.validateTpp(accountConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1, CONFIRMATION_CODE)));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent accountConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);

        when(aisConsentTppInfoValidator.validateTpp(accountConsent.getTppInfo()))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(accountConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(accountConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNoAuthorisation_shouldReturnAuthorisationValidationError() {
        //Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);
        aisConsent.getAuthorisations().get(0).setId(INVALID_AUTHORISATION_ID);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());

        //When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AUTHORISATION_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withBothPsusAbsent_shouldReturnFormatError() {
        //Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, null))
            .thenReturn(ValidationResult.invalid(FORMAT_BOTH_PSUS_ABSENT_ERROR));
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, buildUpdateRequest(AUTHORISATION_ID, psuIdData)));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORMAT_BOTH_PSUS_ABSENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_cantPsuUpdateAuthorisation_shouldReturnCredentialsInvalidError() {
        //Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);

        List<ConsentAuthorization> consentAuthorization = aisConsent.getAuthorisations();
        ConsentAuthorization authorization = consentAuthorization.get(0);
        authorization.setPsuIdData(PSU_ID_DATA_2);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_2))
            .thenReturn(ValidationResult.invalid(CREDENTIALS_INVALID_ERROR));
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_received() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-received-status.json", AisConsent.class);
        UpdateConsentPsuDataReq updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, ScaStatus.RECEIVED, AIS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, updateRequest));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuidentified() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-psu-identified-status.json", AisConsent.class);

        UpdateConsentPsuDataReq updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.PSUIDENTIFIED, false))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, ScaStatus.PSUIDENTIFIED, AIS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, updateRequest));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuauthenticated() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-psu-authenticated-status.json", AisConsent.class);

        UpdateConsentPsuDataReq updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.PSUAUTHENTICATED, false))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, ScaStatus.PSUAUTHENTICATED, AIS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));
        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo()))
            .thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, updateRequest));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_scamethodselected() {
        // Given
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent-sca-method-selected-status.json", AisConsent.class);

        UpdateConsentPsuDataReq updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);

        when(aisAuthorisationValidator.validate(AUTHORISATION_ID, aisConsent))
            .thenReturn(ValidationResult.valid());
        when(aisAuthorisationStatusValidator.validate(ScaStatus.SCAMETHODSELECTED, false))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, ScaStatus.SCAMETHODSELECTED, AIS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_400, SERVICE_INVALID_400));

        when(aisConsentTppInfoValidator.validateTpp(aisConsent.getTppInfo())).thenReturn(ValidationResult.valid());
        when(psuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, null)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new UpdateConsentPsuDataRequestObject(aisConsent, updateRequest));

        // Then
        verify(aisConsentTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(AIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    private UpdateConsentPsuDataReq buildUpdateRequest(String authorisationId, PsuIdData psuIdData) {
        UpdateConsentPsuDataReq result = new UpdateConsentPsuDataReq();
        result.setAuthorizationId(authorisationId);
        result.setPsuData(psuIdData);
        return result;
    }

    private UpdateConsentPsuDataReq buildUpdateRequest(String authorisationId, PsuIdData psuIdData, String confirmationCode) {
        UpdateConsentPsuDataReq result = buildUpdateRequest(authorisationId, psuIdData);
        result.setConfirmationCode(confirmationCode);
        return result;
    }
}
