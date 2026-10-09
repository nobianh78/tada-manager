package app.tada.manager.di

import app.tada.manager.patcher.worker.PatcherWorker
import app.tada.manager.worker.UpdateCheckWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val workerModule = module {
    workerOf(::PatcherWorker)
    workerOf(::UpdateCheckWorker)
}
