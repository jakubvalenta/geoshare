package page.ooooo.geoshare.lib.features

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import page.ooooo.geoshare.data.di.FakeUserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.NullableLongUserPreference
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class FeaturesTest {

    @Test
    fun validate_validationInfoIsNotFoundAndAutomationIsValid_savesAutomationFeatureValidatedAtAndReturnsTrue() =
        runTest {
            val feature = object : Feature {
                override suspend fun validate() = true
            }
            val mockUserPreference: NullableLongUserPreference = mock()
            val now = System.currentTimeMillis()
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(mockUserPreference) } doReturn null
                onBlocking { setValue(eq(mockUserPreference), any()) } doReturn Unit
            }
            assertEquals(Features(mockUserPreferencesRepository).validate(feature, mockUserPreference, now), true)
            verify(mockUserPreferencesRepository).setValue(
                eq(mockUserPreference),
                eq(now),
            )
        }

    @Test
    fun validate_validationInfoIsNotFoundAndAutomationIsNotValid_doesNotSaveAutomationFeatureValidatedAtAndReturnsFalse() =
        runTest {
            val feature = object : Feature {
                override suspend fun validate() = false
            }
            val mockUserPreference: NullableLongUserPreference = mock()
            val now = System.currentTimeMillis()
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(mockUserPreference) } doReturn null
                onBlocking { setValue(eq(mockUserPreference), any()) } doReturn Unit
            }
            assertEquals(Features(mockUserPreferencesRepository).validate(feature, mockUserPreference, now), false)
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(mockUserPreference),
                any(),
            )
        }

    @Test
    fun validate_validationInfoIsNotExpired_doesNotSaveValidationInfoAndReturnsTrue() = runTest {
        val feature = object : Feature {
            override suspend fun validate() = false
        }
        val mockUserPreference: NullableLongUserPreference = mock()
        val now = System.currentTimeMillis()
        val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
            onBlocking { getValue(mockUserPreference) } doReturn now - 60.minutes.inWholeMilliseconds
            onBlocking { setValue(eq(mockUserPreference), any()) } doReturn Unit
        }
        assertEquals(Features(mockUserPreferencesRepository).validate(feature, mockUserPreference, now), true)
        verify(mockUserPreferencesRepository, never()).setValue(
            eq(mockUserPreference),
            any(),
        )
    }

    @Test
    fun validate_validationInfoIsExpired_doesNotSaveValidationInfoAndStartsAsynchronousValidationAndReturnsTrue() =
        runTest {
            val feature = object : Feature {
                override suspend fun validate() = false
            }
            val mockUserPreference: NullableLongUserPreference = mock()
            val now = System.currentTimeMillis()
            val mockUserPreferencesRepository: FakeUserPreferencesRepository = mock {
                onBlocking { getValue(mockUserPreference) } doReturn now - 5.days.inWholeMilliseconds
                onBlocking { setValue(eq(mockUserPreference), any()) } doReturn Unit
            }
            assertEquals(Features(mockUserPreferencesRepository).validate(feature, mockUserPreference, now), true)
            verify(mockUserPreferencesRepository, never()).setValue(
                eq(mockUserPreference),
                any(),
            )
            // TODO Test asynchronous validation
        }

}
