package app.tada.manager.di

import app.tada.manager.network.service.AssetDownloader
import app.tada.manager.network.service.HttpService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val serviceModule = module {
    singleOf(::HttpService)
    singleOf(::AssetDownloader)
}