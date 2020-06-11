package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.ui.setVisibleOrInvisible
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import javax.inject.Inject

class GameMenuFragment : Fragment() {

    @Inject lateinit var savesManager: SavesManager

    private lateinit var game: Game
    private lateinit var system: GameSystem

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        game = arguments?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
                ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_ID")
        system = GameSystem.findById(game.systemId)
        return inflater.inflate(R.layout.layout_game_menu, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupViews()
                .autoDispose(scope())
                .subscribe()
    }

    private fun setupViews(): Completable {
        return Single.just(game)
                .flatMap { savesManager.getSavedSlotsInfo(it, system.coreName) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { presentViews(it) }
                .ignoreElement()
    }

    private fun presentViews(infos: List<SavesManager.SaveInfos>) {
        val slot1SaveView = view!!.findViewById<View>(R.id.save_entry_slot1)
        val slot2SaveView = view!!.findViewById<View>(R.id.save_entry_slot2)
        val slot3SaveView = view!!.findViewById<View>(R.id.save_entry_slot3)
        val slot4SaveView = view!!.findViewById<View>(R.id.save_entry_slot4)

        setupQuickSaveView(slot1SaveView, 0, infos[0])
        setupQuickSaveView(slot2SaveView, 1, infos[1])
        setupQuickSaveView(slot3SaveView, 2, infos[2])
        setupQuickSaveView(slot4SaveView, 3, infos[3])

        view!!.findViewById<Button>(R.id.menu_change_disk).apply {
            val numDisks = activity?.intent?.getIntExtra(GameMenuContract.EXTRA_DISKS, 0) ?: 0
            this.setVisibleOrGone(numDisks > 1)
            this.setOnClickListener {
                displayChangeDiskDialog(numDisks)
            }
        }

        view!!.findViewById<Button>(R.id.menu_edit_touch_controls).apply {
            this.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true)
                }
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
        }

        view!!.findViewById<Button>(R.id.save_entry_reset).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra(GameMenuContract.RESULT_RESET, true)
            }
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }

        view!!.findViewById<Button>(R.id.save_entry_settings).isEnabled = system.exposedSettings.isNotEmpty()
        view!!.findViewById<Button>(R.id.save_entry_settings).setOnClickListener {
            displayAdvancedSettings()
        }

        view!!.findViewById<Button>(R.id.save_entry_close).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra(GameMenuContract.RESULT_QUIT, true)
            }
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }

        view!!.setVisibleOrInvisible(true)
    }

    private fun setupQuickSaveView(quickSaveView: View, index: Int, saveInfo: SavesManager.SaveInfos) {
        val title = getString(R.string.game_menu_state, (index + 1).toString())

        quickSaveView.findViewById<TextView>(R.id.game_dialog_entry_subtext).apply {
            this.text = getDateString(saveInfo)
            this.setVisibleOrInvisible(saveInfo.exists)
        }
        quickSaveView.findViewById<TextView>(R.id.game_dialog_entry_text).text = title
        quickSaveView.findViewById<Button>(R.id.game_dialog_entry_load).apply {
            this.isEnabled = saveInfo.exists
            this.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra(GameMenuContract.RESULT_LOAD, index)
                }
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
        }

        quickSaveView.findViewById<Button>(R.id.game_dialog_entry_save).apply {
            this.setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra(GameMenuContract.RESULT_SAVE, index)
                }
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
        }
    }

    private fun displayAdvancedSettings() {
        findNavController().navigate(R.id.core_options, arguments)
    }

    private fun displayChangeDiskDialog(numDisks: Int) {
        val builder = AlertDialog.Builder(requireContext())

        val values = (0 until numDisks)
                .map { resources.getString(R.string.game_menu_change_disk_disk, (it + 1).toString()) }
                .toTypedArray()

        builder.setItems(values) { _, index ->
            handleDiskChange(index)
        }

        builder.create().show()
    }

    private fun handleDiskChange(index: Int) {
        val resultIntent = Intent().apply {
            putExtra(GameMenuContract.RESULT_CHANGE_DISK, index)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    /** We still return a string even if we don't show it to ensure dialog doesn't change size.*/
    private fun getDateString(saveInfo: SavesManager.SaveInfos): String {
        val formatter = SimpleDateFormat.getDateTimeInstance()
        val date = if (saveInfo.exists) {
            saveInfo.date
        } else {
            System.currentTimeMillis()
        }
        return formatter.format(date)
    }

    @dagger.Module
    class Module
}
