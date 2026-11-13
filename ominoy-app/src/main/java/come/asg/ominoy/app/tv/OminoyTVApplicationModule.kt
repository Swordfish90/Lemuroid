package come.asg.ominoy.app.tv

import come.asg.ominoy.app.tv.folderpicker.TVFolderPickerActivity
import come.asg.ominoy.app.tv.folderpicker.TVFolderPickerLauncher
import come.asg.ominoy.app.tv.game.TVGameActivity
import come.asg.ominoy.app.tv.gamemenu.TVGameMenuActivity
import come.asg.ominoy.app.tv.input.TVGamePadBindingActivity
import come.asg.ominoy.app.tv.input.TVGamePadShortcutBindingActivity
import come.asg.ominoy.app.tv.main.MainTVActivity
import come.asg.ominoy.app.tv.settings.TVSettingsActivity
import come.asg.ominoy.lib.injection.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OminoyTVApplicationModule {
    @PerActivity
    @ContributesAndroidInjector(modules = [MainTVActivity.Module::class])
    abstract fun tvMainActivity(): MainTVActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGameActivity(): TVGameActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGameMenuActivity(): TVGameMenuActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvFolderPickerLauncher(): TVFolderPickerLauncher

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvFolderPickerActivity(): TVFolderPickerActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGamePadBindingActivity(): TVGamePadBindingActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGamePadShortcutBindingActivity(): TVGamePadShortcutBindingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [TVSettingsActivity.Module::class])
    abstract fun tvSettingsActivity(): TVSettingsActivity
}
