package com.ordy.app.ui.orders.overview.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ordy.app.R
import com.ordy.app.databinding.FragmentOrderUsersBinding
import com.ordy.app.ui.orders.overview.OverviewOrderViewModel

class OrderUsersFragment : Fragment() {

    private val viewModel: OverviewOrderViewModel by activityViewModels()

    private lateinit var listAdapter: OrderUsersListAdapter

    /**
     * Called when view is created.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_order_users, container, false)

        // Create binding for the fragment.
        val binding = FragmentOrderUsersBinding.inflate(inflater, container, false)
        binding.handlers = OrderUsersHandlers(this, viewModel)

        // Create the list view adapter
        listAdapter = OrderUsersListAdapter(requireContext(), viewModel)
        binding.root.findViewById<ListView>(R.id.order_items).apply {
            adapter = listAdapter
            emptyView = binding.root.findViewById(R.id.order_items_empty)
        }

        // Update the list adapter when the "order" query updates
        viewModel.getOrderMLD().observe(viewLifecycleOwner, Observer {

            // Update the list view
            listAdapter.update()
        })

        return binding.root
    }
}