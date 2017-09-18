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
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.codebutler.odyssey.R
import com.codebutler.odyssey.core.http.OdysseyHttp
import com.codebutler.odyssey.core.kotlin.bindView
import com.codebutler.odyssey.core.kotlin.inflate
import com.codebutler.odyssey.feature.core.CoreManager
import com.codebutler.odyssey.feature.core.model.CoreInfo
import com.codebutler.odyssey.feature.game.GameActivity
import okhttp3.OkHttpClient
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
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
        val coreManager = CoreManager(OdysseyHttp(OkHttpClient()), File(cacheDir, "cores"))

        val files = Environment.getExternalStorageDirectory()
                .listFiles()
                .filter { it.isFile }.toList()

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // FIXME: This disaster is temporary
        recycler.adapter = RomFilesAdapter(files, { romFile ->
            recycler.post {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage(getString(R.string.loading))
                progressDialog.show()

                coreManager.downloadAllCoreInfo { response ->
                    recycler.post {
                        if (!progressDialog.isShowing) {
                            return@post
                        }
                        progressDialog.cancel()

                        when (response) {
                            is OdysseyHttp.Response.Success -> {
                                val coreInfos = response.body

                                val cores = coreInfos.filter { info ->
                                    info.metadata.supportedExtensions?.contains(romFile.extension) ?: false
                                }

                                Log.d(TAG, "Got matching cores: $cores")

                                AlertDialog.Builder(this@MainActivity)
                                        .setAdapter(CoreAdapter(cores)) { dialog, which ->
                                            val core = cores[which]
                                            val progressDialog = ProgressDialog(this)
                                            progressDialog.setMessage(getString(R.string.loading_x, core.fileInfo.coreName))
                                            progressDialog.show()
                                            coreManager.downloadCore(core.fileInfo.fileName, { response ->
                                                recycler.post {
                                                    if (!progressDialog.isShowing) {
                                                        return@post
                                                    }
                                                    progressDialog.cancel()
                                                    when (response) {
                                                        is OdysseyHttp.Response.Success -> {
                                                            val coreFile = response.body
                                                            startActivity(GameActivity.newIntent(
                                                                    context = this,
                                                                    coreFilePath = coreFile.absolutePath,
                                                                    gameFilePath = romFile.absolutePath))
                                                        }
                                                        is OdysseyHttp.Response.Failure -> {
                                                            Toast.makeText(
                                                                    this@MainActivity,
                                                                    "Failed to download core: ${response.error}",
                                                                    Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                        .show()
                            }
                            is OdysseyHttp.Response.Failure -> Log.e(TAG, "Failed to get cores: ${response.error}")
                        }
                    }
                }
            }
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

class CoreAdapter(private val cores: List<CoreInfo>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: parent.inflate(android.R.layout.simple_list_item_1, false)
        view.findViewById<TextView>(android.R.id.text1).text = cores[position].metadata.displayName
        return view
    }

    override fun getItem(position: Int): Any = cores[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = cores.size
}
