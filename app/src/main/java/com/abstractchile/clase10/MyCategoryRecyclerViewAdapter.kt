package com.abstractchile.clase10

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import com.abstractchile.clase10.CategoryFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_category.view.*

class MyCategoryRecyclerViewAdapter(
    private val mValues: MutableList<String>,
    private val mListener: OnListFragmentInteractionListener?
    //val mContext: Context
) : RecyclerView.Adapter<MyCategoryRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as String
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mContentView.text = item
        /**
        Glide.with(mContext)
            .load(item)
            .into(holder.mImageView)
        */
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)

        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mContentView: TextView = mView.content
        //val mImageView: ImageView = mView.imageView

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
