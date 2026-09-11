package page.ooooo.geoshare.data.di

import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import page.ooooo.geoshare.data.InputRepository
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.OutputRepository
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.billing.Billing
import page.ooooo.geoshare.lib.conversion.ConversionState
import page.ooooo.geoshare.lib.conversion.ConversionStateContext
import page.ooooo.geoshare.lib.conversion.ConversionStateLogItem
import page.ooooo.geoshare.lib.conversion.DefaultConversionStateContext
import page.ooooo.geoshare.lib.conversion.Initial
import page.ooooo.geoshare.lib.inputs.Input
import javax.inject.Singleton
import kotlin.time.ComparableTimeMark

@Module
@InstallIn(SingletonComponent::class)
object ConversionStateContextModule {

    @Provides
    @Singleton
    fun provideConversionStateContext(
        @ApplicationContext context: Context,
        billing: Billing,
        inputRepository: InputRepository,
        linkRepository: LinkRepository,
        log: Log,
        outputRepository: OutputRepository,
        uriQuote: UriQuote,
        userPreferencesRepository: UserPreferencesRepository,
    ): ConversionStateContext =
        DefaultConversionStateContext(
            inputs = inputRepository.all,
            linkRepository = linkRepository,
            outputRepository = outputRepository,
            resources = context.resources,
            userPreferencesRepository = userPreferencesRepository,
            log = log,
            billing = billing,
            uriQuote = uriQuote,
        )
}

/**
 * An implementation of [ConversionStateContext] that allows setting the state using a public method [setState].
 *
 * For testing purposes only.
 */
class FakeConversionStateContext(
    override val inputs: List<Input>,
    override val linkRepository: LinkRepository,
    override val outputRepository: OutputRepository,
    override val resources: Resources,
    override val userPreferencesRepository: UserPreferencesRepository,
    override val log: Log,
    override val billing: Billing,
    override val uriQuote: UriQuote,
) : ConversionStateContext {
    private var counter: Int = 0

    private var _currentState: MutableStateFlow<ConversionState> = MutableStateFlow(Initial)
    override val currentState: StateFlow<ConversionState> = _currentState.asStateFlow()

    private var _stateLog: MutableStateFlow<List<ConversionStateLogItem>> = MutableStateFlow(emptyList())
    override val stateLog: StateFlow<List<ConversionStateLogItem>> = _stateLog.asStateFlow()

    fun setState(newState: ConversionState, start: ComparableTimeMark, resetLog: Boolean = false) {
        _currentState.value = newState
        val newLogItem = ConversionStateLogItem(counter++, newState, start)
        if (resetLog) {
            _stateLog.value = listOf(newLogItem)
        } else {
            _stateLog.update { it + newLogItem }
        }
    }

    override suspend fun transition(newState: ConversionState, resetLog: Boolean) {
        throw NotImplementedError()
    }

    override fun setExceptionState(tr: Throwable, newState: ConversionState) {
        throw NotImplementedError()
    }
}
