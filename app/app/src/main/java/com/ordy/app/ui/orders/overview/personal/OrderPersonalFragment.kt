package com.ordy.app.ui.orders.overview.personal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ordy.app.AppPreferences
import com.ordy.app.R
import com.ordy.app.api.util.Query
import com.ordy.app.api.util.QueryStatus
import com.ordy.app.databinding.FragmentOrderPersonalBinding
import com.ordy.app.ui.orders.overview.OverviewOrderViewModel
import com.ordy.app.util.OrderUtil
import com.ordy.app.util.TimerUtil
import kotlinx.android.synthetic.main.fragment_order_personal.*
import kotlinx.android.synthetic.main.fragment_order_personal.view.*

class OrderPersonalFragment : Fragment() {

    private val viewModel: OverviewOrderViewModel by activityViewModels()

    private var listAdapter: OrderPersonalListAdapter? = null

    /**
     * Called when view is created.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_order_personal, container, false)

        // Create binding for the fragment.
        val binding = FragmentOrderPersonalBinding.inflate(inflater, container, false)
        binding.handlers = OrderPersonalHandlers(this, viewModel)

        // Create the list view adapter
        listAdapter = OrderPersonalListAdapter(requireContext(), Query(QueryStatus.LOADING), true)
        binding.root.findViewById<ListView>(R.id.order_items).adapter = listAdapter

        // Update the list adapter when the "order" query updates
        viewModel.order.observe(viewLifecycleOwner, Observer {

            val listAdapter = this.listAdapter ?: throw IllegalStateException("List adapter should not be null")

            // Update the orders
            listAdapter.order = it

            // Update the order items, when the query succeeded.
            if(it.status == QueryStatus.SUCCESS) {

                // Only show the items with the same user id as the logged in user.
                val orderItems = it.requireData().orderItems.filter { it.user.id == AppPreferences(requireContext()).userId }

                // Remove the action buttons when the order is closed.
                TimerUtil.updateUI(this.activity as AppCompatActivity, 0, 1000) {
                    val closed = OrderUtil.timeLeft(it.requireData().deadline) <= 0

                    // Update the list view only when necessary
                    if(!closed != listAdapter.showActions) {
                        listAdapter.showActions = !closed

                        // Hide the "add item"-button
                        binding.root.order_items_add.visibility = if (closed) View.INVISIBLE else View.VISIBLE

                        // Notify the changes to the list view (to re-render automatically)
                        listAdapter.notifyDataSetChanged()
                    }
                }

                listAdapter.orderItems = orderItems
            }

            // Notify the changes to the list view (to re-render automatically)
            listAdapter.notifyDataSetChanged()
        })

        return binding.root
    }
}