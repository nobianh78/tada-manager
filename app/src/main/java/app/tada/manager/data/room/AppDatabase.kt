package app.tada.manager.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.tada.manager.data.room.apk.ApkSignature
import app.tada.manager.data.room.apk.ApkSignatureDao
import app.tada.manager.data.room.apps.installed.AppliedPatch
import app.tada.manager.data.room.apps.installed.InstalledApp
import app.tada.manager.data.room.apps.installed.InstalledAppDao
import app.tada.manager.data.room.apps.original.OriginalApk
import app.tada.manager.data.room.apps.original.OriginalApkDao
import app.tada.manager.data.room.bundles.PatchBundleDao
import app.tada.manager.data.room.bundles.PatchBundleEntity
import app.tada.manager.data.room.options.Option
import app.tada.manager.data.room.options.OptionDao
import app.tada.manager.data.room.options.OptionGroup
import app.tada.manager.data.room.selection.AppSourceMute
import app.tada.manager.data.room.selection.PatchSelection
import app.tada.manager.data.room.selection.SeenPatch
import app.tada.manager.data.room.selection.SelectedPatch
import app.tada.manager.data.room.selection.SelectionDao
import app.tada.manager.data.room.selection.SourceMuteDao
import kotlin.random.Random

@Database(
    entities = [
        PatchBundleEntity::class,
        PatchSelection::class,
        SelectedPatch::class,
        SeenPatch::class,
        AppSourceMute::class,
        InstalledApp::class,
        AppliedPatch::class,
        OptionGroup::class,
        Option::class,
        OriginalApk::class,
        ApkSignature::class
    ],
    version = 17
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patchBundleDao(): PatchBundleDao
    abstract fun selectionDao(): SelectionDao
    abstract fun sourceMuteDao(): SourceMuteDao
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun optionDao(): OptionDao
    abstract fun originalApkDao(): OriginalApkDao
    abstract fun apkSignatureDao(): ApkSignatureDao

    companion object {
        fun generateUid() = Random.nextInt()
    }
}
