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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.ChosenScaMethod;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ChosenScaMethodMapperImpl.class})
class ChosenScaMethodMapperTest {
    @Autowired
    private ChosenScaMethodMapper chosenScaMethodMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToChosenScaMethod_withNull_shouldReturnNull() {
        // When
        ChosenScaMethod chosenScaMethod = chosenScaMethodMapper.mapToChosenScaMethod(null);

        // Then
        assertNull(chosenScaMethod);
    }

    @Test
    void mapToChosenScaMethod() {
        // Given
        de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject authenticationObject =
            jsonReader.getObjectFromFile("json/web/mapper/Xs2aAuthenticationObject.json", de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject.class);

        ChosenScaMethod expected =
            jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", ChosenScaMethod.class);

        // When
        ChosenScaMethod chosenScaMethod = chosenScaMethodMapper.mapToChosenScaMethod(authenticationObject);

        // Then
        assertNotNull(chosenScaMethod);
        assertEquals(expected, chosenScaMethod);
    }
}
