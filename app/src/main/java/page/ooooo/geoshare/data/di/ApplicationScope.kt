package page.ooooo.geoshare.data.di

import javax.inject.Qualifier

/**
 * Disambiguate between multiple coroutine scopes, so that Hilt knows which one to inject.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
