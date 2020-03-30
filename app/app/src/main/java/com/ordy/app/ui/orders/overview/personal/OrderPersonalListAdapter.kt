package com.ordy.app.ui.orders.overview.personal

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ordy.app.AppPreferences
import com.ordy.app.R
import com.ordy.app.api.models.Order
import com.ordy.app.api.models.OrderItem
import com.ordy.app.api.util.Query
import com.ordy.app.api.util.QueryStatus
import com.ordy.app.ui.orders.overview.OverviewOrderViewModel
import com.ordy.app.util.OrderUtil
import com.ordy.app.util.TimerUtil
import kotlinx.android.synthetic.main.fragment_order_personal.view.*
import kotlinx.android.synthetic.main.list_order_item.view.*

class OrderPersonalListAdapter(val context: Context, val view: View, val activity: Activity, val viewModel: OverviewOrderViewModel): BaseAdapter() {

    private var orderItems: List<OrderItem> = emptyList()
    private var showActions: Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_order_item, parent, false)

        when(viewModel.getOrder().status) {

            QueryStatus.LOADING -> {

                // Start the shimmer effect & show
                view.order_item_loading.startShimmer()
                view.order_item_loading.visibility = View.VISIBLE
                view.order_item_data.visibility = View.GONE
            }

            QueryStatus.SUCCESS -> {
                val orderItem = orderItems[position]

                // Stop the shimmer effect & hide.
                view.order_item_loading.stopShimmer()
                view.order_item_loading.visibility = View.GONE
                view.order_item_data.visibility = View.VISIBLE

                // Assign the data.
                view.order_item_quantity.text = "1x"
                view.order_item_name.text = orderItem.item.name
                view.order_item_comment.text = orderItem.comment

                // Hide the comment area when comment is empty.
                if(view.order_item_comment.text == "") {
                    view.order_item_comment.visibility = View.GONE
                }

                if(showActions) {
                    view.order_item_actions.visibility = View.VISIBLE
                } else {
                    view.order_item_actions.visibility = View.INVISIBLE
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
        return when(viewModel.getOrder().status) {
            QueryStatus.LOADING -> 4
            QueryStatus.SUCCESS -> orderItems.size
            else -> 0
        }
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    fun update() {
        // Update the order items, when the query succeeded.
        if(viewModel.getOrder().status == QueryStatus.SUCCESS) {

            // Only show the items with the same user id as the logged in user.
            orderItems = viewModel.getOrder().requireData().orderItems.filter {
                it.user.id == AppPreferences(context!!).userId
            }

            // Remove the action buttons when the order is closed.
            TimerUtil.updateUI(this.activity as AppCompatActivity, 0, 1000) {
                val closed = OrderUtil.timeLeft(viewModel.getOrder().requireData().deadline) <= 0

                // Update the list view only when necessary
                if(!closed != showActions) {
                    showActions = !closed

                    // Hide the "add item"-button
                    view.order_items_add.visibility = if (closed) View.INVISIBLE else View.VISIBLE

                    // Notify the changes to the list view (to re-render automatically)
                    notifyDataSetChanged()
                }
            }
        }

        // Notify the changes to the list view (to re-render automatically)
        notifyDataSetChanged()
    }
}