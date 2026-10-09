package app.tada.manager.di

import app.tada.manager.data.platform.Filesystem
import app.tada.manager.data.platform.NetworkInfo
import app.tada.manager.domain.repository.*
import app.tada.manager.domain.worker.WorkerRepository
import app.tada.manager.network.api.TadaAPI
import app.tada.manager.util.AppDataResolver
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::TadaAPI)
    singleOf(::Filesystem) {
        createdAtStart()
    }
    singleOf(::NetworkInfo)
    singleOf(::ManagerUpdateRepository)
    singleOf(::PatchSelectionRepository)
    singleOf(::SourceMuteRepository)
    singleOf(::PatchOptionsRepository)
    singleOf(::BlocklistRepository)
    singleOf(::PatchBundleRepository)
    singleOf(::WorkerRepository)
    singleOf(::InstalledAppRepository)
    singleOf(::OriginalApkRepository)
    singleOf(::StorageStatsRepository)
    singleOf(::AppDataResolver)
}
