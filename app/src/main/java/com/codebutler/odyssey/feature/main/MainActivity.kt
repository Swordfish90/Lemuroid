/*
 * MainActivity.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.feature.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.codebutler.odyssey.R
import com.codebutler.odyssey.core.kotlin.bindView
import com.codebutler.odyssey.core.kotlin.inflate
import com.codebutler.odyssey.feature.game.GameActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION = 10001
    }

    private val recycler: RecyclerView by bindView(R.id.recycler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION)
        } else {
            loadGames()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadGames()
        } else {
            finish()
        }
    }

    private fun loadGames() {
        val sdcardDir = Environment.getExternalStorageDirectory()
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = RomFilesAdapter(sdcardDir.listFiles().filter { it.isFile }.toList(), { file ->
            startActivity(GameActivity.newIntent(
                    context = this,
                    coreFileName = "snes9x_libretro_android.so",
                    gameFileName = file.name))
        })
    }

    private class RomFilesAdapter(private val files: List<File>, private val clickListener: (file: File) -> Unit)
        : RecyclerView.Adapter<RomFileViewHolder>() {

        override fun onBindViewHolder(holder: RomFileViewHolder, position: Int) {
            holder.bind(files[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RomFileViewHolder
                = RomFileViewHolder(parent.inflate(R.layout.listitem_rom_file), { position ->
            clickListener(files[position])
        })

        override fun getItemCount(): Int = files.size
    }

    private class RomFileViewHolder(itemView: View, private val clickListener: (position: Int) -> Unit)
        : RecyclerView.ViewHolder(itemView) {

        private val textView: TextView by bindView(R.id.text)

        init {
            itemView.setOnClickListener {
                clickListener(adapterPosition)
            }
        }

        fun bind(file: File) {
            textView.text = file.name
        }
    }
}
