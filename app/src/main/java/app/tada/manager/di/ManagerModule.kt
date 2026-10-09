package app.tada.manager.di

import app.tada.manager.domain.apk.ApkSignatureCache
import app.tada.manager.domain.apk.LocalApkSources
import app.tada.manager.domain.batch.BatchPatchCoordinator
import app.tada.manager.domain.batch.BatchPlanResolver
import app.tada.manager.domain.bundles.AppVersionCatalog
import app.tada.manager.domain.installer.InstallerManager
import app.tada.manager.domain.installer.RootInstaller
import app.tada.manager.domain.installer.SessionInstaller
import app.tada.manager.domain.links.AppLinksManager
import app.tada.manager.domain.manager.*
import app.tada.manager.ui.screen.shared.ContentTranslation
import app.tada.manager.util.AppCoroutineScope
import app.tada.manager.util.ContentTranslator
import app.tada.manager.util.PM
import app.tada.manager.util.UpdateNotificationManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val managerModule = module {
    singleOf(::KeystoreManager)
    singleOf(::ApkSignatureCache)
    singleOf(::PM)
    singleOf(::RootInstaller)
    singleOf(::SessionInstaller)
    singleOf(::InstallerManager)
    singleOf(::PatchOptionsPreferencesManager)
    singleOf(::AppIconManager)
    singleOf(::UpdateNotificationManager)
    singleOf(::DownloadUrlResolver)
    singleOf(::AppVersionCatalog)
    singleOf(::LocalApkSources)
    singleOf(::HomeAppButtonPreferences)
    singleOf(::AppCoroutineScope)
    singleOf(::BatchPlanResolver)
    singleOf(::BatchPatchCoordinator)
    singleOf(::ContentTranslator)
    singleOf(::ContentTranslation)
    singleOf(::AppLinksManager)
}
