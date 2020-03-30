package com.ordy.app.ui.groups.overview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.ordy.app.R
import com.ordy.app.api.models.Group
import com.ordy.app.api.util.Query
import com.ordy.app.api.util.QueryStatus
import kotlinx.android.synthetic.main.list_group_member_card.view.*

class OverviewGroupListAdapter(
    val context: Context?,
    var viewModel: OverviewGroupViewModel
) :
    BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_group_member_card,
            parent,
            false
        )

        when (viewModel.getGroup().status) {
            QueryStatus.LOADING -> {

                // Start the shimmer effect & show
                view.member_loading.startShimmer()
                view.member_loading.visibility = View.VISIBLE
                view.member_data.visibility = View.GONE
            }

            QueryStatus.SUCCESS -> {
                val member = viewModel.getGroup().requireData().members[position]

                // Stop the shimmer effect & hide
                view.member_loading.stopShimmer()
                view.member_loading.visibility = View.GONE
                view.member_data.visibility = View.VISIBLE

                // Assign the data
                view.member_name.text = member.username
                view.member_email.text = member.email

                // Set click handler on remove button
                view.member_remove.setOnClickListener {
                    viewModel.removeMember(viewModel.getGroup().requireData().id, member.id)
                }
            }
        }

        return view
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return when (viewModel.getGroup().status) {
            QueryStatus.LOADING -> 6
            QueryStatus.SUCCESS -> viewModel.getGroup().requireData().members.size
            else -> 0
        }
    }

}