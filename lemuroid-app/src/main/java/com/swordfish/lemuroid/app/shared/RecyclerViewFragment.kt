package com.swordfish.lemuroid.app.shared

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R
import dagger.android.support.AndroidSupportInjection

open class RecyclerViewFragment : Fragment() {

    protected var recyclerView: RecyclerView? = null
    protected var emptyView: View? = null

    private var layoutManagerState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        recyclerView = root.findViewById(R.id.recycler_view)
        emptyView = root.findViewById(R.id.empty_view)
        return root
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val parcelable = recyclerView?.layoutManager?.onSaveInstanceState()
        outState.putParcelable("key", parcelable)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        layoutManagerState = savedInstanceState?.getParcelable("key")
    }

    protected fun restoreRecyclerViewState() {
        recyclerView?.layoutManager?.onRestoreInstanceState(layoutManagerState)
    }
}
